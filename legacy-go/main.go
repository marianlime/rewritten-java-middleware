package main

import (
	"log"
	"time"
	"net/http"
	auth "middleware/packages/authentication"
)

func main(){
	

	mux := http.NewServeMux()
	mux.HandleFunc("/health", func(w http.ResponseWriter, r *http.Request){
		w.WriteHeader(http.StatusOK)
	})
	mux.HandleFunc("/protected",
		auth.RoleMiddleware("admin")(
			func(w http.ResponseWriter, r *http.Request){
				w.WriteHeader(http.StatusOK)
				w.Write([]byte("Authorized access"))
			},
		),
	)
	
	server := &http.Server{
		Addr: ":8080",
		Handler: mux,
		ReadHeaderTimeout: 5 * time.Second,
	}

	log.Println("OpenSecure is listening on :8080")
	log.Fatal(server.ListenAndServe())
	}