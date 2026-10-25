package api

import (
	"encoding/json"
	"errors"
	"io"
	"net/http"

	"middleware/internal/policy"
)

func TransferEvaluationHandler(p policy.TransferPolicy) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if r.Method != http.MethodPost {
			w.Header().Set("Allow", "POST")

			http.Error(
				w,
				"Mehtod not allowed",
				http.StatusMethodNotAllowed,
			)
			return
		}

		var req policy.TransferRequest

		decoder := json.NewDecoder(
			http.MaxBytesReader(w, r.Body, 8192),
		)
		decoder.DisallowUnknownFields()

		if err := decoder.Decode(&req); err != nil {
			http.Error(
				w,
				"Invalid request",
				http.StatusBadRequest,
			)
			return
		}

		var extra any

		if err := decoder.Decode(&extra); err != io.EOF {
			http.Error(
				w,
				"Request must contain exactly one JSON object",
				http.StatusBadRequest,
			)
			return
		}

		err := p.Evaluate(req)

		switch {
		case err == nil:
			w.Header().Set("Content-Type", "application/json")
			w.WriteHeader(http.StatusOK)

			w.Write([]byte(`{"decision":"allowed"}`))
			return

		case errors.Is(err, policy.ErrInvalidAmount):
			http.Error(
				w,
				err.Error(),
				http.StatusBadRequest,
			)
			return
		case errors.Is(err, policy.ErrUnsuportedCurrency):
			http.Error(
				w,
				err.Error(),
				http.StatusBadRequest,
			)
			return
		case errors.Is(err, policy.ErrLimitExceed):
			http.Error(
				w,
				err.Error(),
				http.StatusForbidden,
			)
			return

		default:
			http.Error(
				w,
				"Internal server error",
				http.StatusInternalServerError,
			)
			return
		}

	}
}
