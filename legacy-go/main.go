package main

import (
	"log"
	auth "middleware/packages/authentication"
	"net/http"
	"os"
	"time"
)

func main() {
	jwtSecret := os.Getenv("secret_key")
	if len(jwtSecret) < 32 {
		log.Fatal("Invalid jwt secret")
	}

	mux := http.NewServeMux()
	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
	})
	mux.HandleFunc("/protected",
		auth.RoleMiddleware("admin")(
			func(w http.ResponseWriter, r *http.Request) {
				w.WriteHeader(http.StatusOK)
				w.Write([]byte("Authorized access"))
			},
		),
	)

	server := &http.Server{
		Addr:              ":8080",
		Handler:           mux,
		ReadHeaderTimeout: 5 * time.Second,
	}

	log.Println("OpenSecure is listening on :8080")
	log.Fatal(server.ListenAndServe())
}
