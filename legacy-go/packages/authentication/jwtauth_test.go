package auth

import (
	"net/http"
	"net/http/httptest"
	"os"
	"testing"
	"errors"
	"time"
	"github.com/golang-jwt/jwt/v5"
)

func TestGenerateToken(t *testing.T) {
	secretKey := "DiI0sUuhUX"
	os.Setenv("secret_key", secretKey)
	defer os.Unsetenv("secret_key")

	token, err := GenerateToken("admin")
	if err != nil {
		t.Fatalf("Failed to generate token: %v", err)
	}
	if token == "" {
		t.Fatalf("Generated token is empty")
	}
}
func TestGenerateTokenWithDifferentRoles(t *testing.T){
	secretKey := "xTFSslQRnH"
	os.Setenv("secret_key", secretKey)
	defer os.Unsetenv("secret_key")

	roles := []string{"admin", "employee", "customer"}

	for _, role := range roles {
		t.Run("role = "+ role, func(t *testing.T){
			tokenString, err := GenerateToken(role)
			if err != nil {
				t.Fatalf("Failed to generate token for role %s: %v", role, err)
			}
			token, err:= jwt.ParseWithClaims(tokenString, &CustomClaims{}, func(token *jwt.Token) (interface{}, error){
				return []byte(secretKey), nil
			})
			if err != nil {
				t.Fatalf("Failed to parse token: %v", err)
			}
			if claims, ok := token.Claims.(*CustomClaims); ok && token.Valid{
				if claims.Role != role {
					t.Errorf("Expected role %s, got %s", role, claims.Role)
				} 
			} else {
				t.Fatalf("Token parsing failed/Token is invalid")
			}
		})
	}
}

func TestTokenExpiry(t *testing.T){
	secretKey := "iJEwgdEGe2"
	t.Setenv("secret_key", secretKey)
	

	claims := CustomClaims{
		Role: "admin",
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(
				time.Now().Add(-time.Hour),
			),	
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims, )
	tokenString, err := token.SignedString([]byte(secretKey))
	if err != nil{
		t.Fatalf("Failed to sign token: %v", err)
	}
	

	_, err = jwt.ParseWithClaims(
		 tokenString,
		 &CustomClaims{}, 
		 func(token *jwt.Token) (any, error){
			return []byte(secretKey), nil
	},  
	jwt.WithValidMethods([]string{
		jwt.SigningMethodHS256.Alg(),
	}),
		jwt.WithExpirationRequired(),
	)
	if !errors.Is(err, jwt.ErrTokenExpired){
		t.Fatalf("Expected token expiration error, got %v", err)
	}
	
}

func TestRoleMiddleware(t *testing.T) {
	os.Setenv("secret_key", "testsecret")
	defer os.Unsetenv("secret_key")

	protectedHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("Access granted"))
	})

	testHandler := RoleMiddleware("admin")(protectedHandler)
	server := httptest.NewServer(testHandler)
	defer server.Close()

	adminToken, _ := GenerateToken("admin")
	costumerToken, _ := GenerateToken("costumer")

	testCases := []struct {
		name           string
		token          string
		expectedStatus int
	}{
		{"ValidAdminToken", adminToken, http.StatusOK},
		{"InvalidCostumerRoleToken", costumerToken, http.StatusForbidden},
		{"NoToken", "", http.StatusUnauthorized},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			req, _ := http.NewRequest("GET", server.URL, nil)
			if tc.token != "" {
				req.Header.Set("Authorization", "Bearer "+tc.token)
			}
			response, _ := http.DefaultClient.Do(req)
			if response.StatusCode != tc.expectedStatus {
				t.Errorf("Expected status %d, got %d", tc.expectedStatus, response.StatusCode)
			}
		})
	}
}

func TestAdminAuthorization(t *testing.T){
	t.Setenv("secret_key", "this-is-a-test-secret-with-32-plus-bytes")

	token, err := GenerateToken("admin")
	if err != nil {
		t.Fatalf("Failed to generate token: %v", err)
	}

	handler := RoleMiddleware("admin")(
		func(w http.ResponseWriter, r *http.Request){
			w.WriteHeader(http.StatusOK)
		},
	)

	req := httptest.NewRequest(
		http.MethodGet,
		"/protected",
		nil,
	)

	req.Header.Set("Authorization", "Bearer "+token)

	recorder := httptest.NewRecorder()

	handler(recorder, req)

	if recorder.Code != http.StatusOK {
		t.Fatalf(
			"Expected 200 OK, got %d",
			recorder.Code,
		)
	}
}