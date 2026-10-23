
package auth

import (
	"net/http"
	"os"
	"strings"
	"time"
	"github.com/golang-jwt/jwt/v5"
	"errors"
)

type CustomClaims struct {
	Role string `json:"role"`
	jwt.RegisteredClaims
}

func GenerateToken(role string) (string, error){
	secretKey := os.Getenv("secret_key")
	if secretKey == "" {
		return "", errors.New("secret key for JWT is not set")
	}
	now := time.Now()

	claims := CustomClaims{
		Role: role, 
		RegisteredClaims: jwt.RegisteredClaims{
			Issuer: "opensecure",
			Audience: jwt.ClaimStrings{"opensecure-api"},
			IssuedAt: jwt.NewNumericDate(now),
			ExpiresAt: jwt.NewNumericDate(now.Add(3 * time.Hour)),
		},
	}
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)

	return token.SignedString([]byte(secretKey))
}

func RoleMiddleware(requiredRole string) func(http.HandlerFunc) http.HandlerFunc {
	return func(next http.HandlerFunc) http.HandlerFunc {
		return func(w http.ResponseWriter, r *http.Request) {
			parts := strings.Fields(r.Header.Get("Authorization"))
			if len(parts) != 2 || !strings.EqualFold(parts[0], "Bearer"){
				http.Error(w, "Unauthorized", http.StatusUnauthorized)
				return
			}
			tokenString := parts[1]
			secretKey := os.Getenv("secret_key")
			if secretKey == "" {
				http.Error(w, "Internal Server Error", http.StatusInternalServerError)
				return
			}
			token, err := jwt.ParseWithClaims(tokenString, &CustomClaims{}, 
				func(token *jwt.Token) (any, error) {return []byte(secretKey), nil}, 
				jwt.WithValidMethods([]string{jwt.SigningMethodHS256.Alg(),}), 
				jwt.WithExpirationRequired(), 
				jwt.WithIssuer("opensecure"), 
				jwt.WithAudience("opensecure-api"),
			)
			if err != nil{
				http.Error(w, "Unautharized", http.StatusUnauthorized)
				return
			}

			if claims, ok := token.Claims.(*CustomClaims); ok && token.Valid {
				if claims.Role == requiredRole{
					next.ServeHTTP(w, r)
					return
				}
			}
			http.Error(w, "Forbidden", http.StatusForbidden)
		}
	}
}