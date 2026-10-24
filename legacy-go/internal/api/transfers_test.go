package api

import (
    "net/http"
    "net/http/httptest"
    "strings"
    "testing"

    "middleware/internal/policy"
)

func TestTransferEvaluationHandler(t *testing.T) {

    p := policy.TransferPolicy{
        MaxAmountMinor: 100000,
    }

    tests := []struct {
        name       string
        method     string
        body       string
        wantStatus int
    }{
        {
            name:   "Transfer within limit",
            method: http.MethodPost,
            body: `{
                "amount_minor": 50000,
                "currency": "GBP"
            }`,
            wantStatus: http.StatusOK,
        },
        {
            name:   "Transfer at exact limit",
            method: http.MethodPost,
            body: `{
                "amount_minor": 100000,
                "currency": "GBP"
            }`,
            wantStatus: http.StatusOK,
        },
        {
            name:   "Transfer exceeds limit",
            method: http.MethodPost,
            body: `{
                "amount_minor": 250000,
                "currency": "GBP"
            }`,
            wantStatus: http.StatusForbidden,
        },
        {
            name:   "Invalid zero amount",
            method: http.MethodPost,
            body: `{
                "amount_minor": 0,
                "currency": "GBP"
            }`,
            wantStatus: http.StatusBadRequest,
        },
        {
            name:   "Unsupported currency",
            method: http.MethodPost,
            body: `{
                "amount_minor": 50000,
                "currency": "EUR"
            }`,
            wantStatus: http.StatusBadRequest,
        },
        {
            name:       "Malformed JSON",
            method:     http.MethodPost,
            body:       `{invalid}`,
            wantStatus: http.StatusBadRequest,
        },
        {
            name:       "Incorrect HTTP method",
            method:     http.MethodGet,
            body:       "",
            wantStatus: http.StatusMethodNotAllowed,
        },
        {
            name:   "Multiple JSON objects",
            method: http.MethodPost,
            body: `{"amount_minor":50000,"currency":"GBP"}
                   {"amount_minor":250000,"currency":"GBP"}`,
            wantStatus: http.StatusBadRequest,
        },
    }

    handler := TransferEvaluationHandler(p)

    for _, tt := range tests {
        t.Run(tt.name, func(t *testing.T) {

            req := httptest.NewRequest(
                tt.method,
                "/v1/transfers/evaluate",
                strings.NewReader(tt.body),
            )

            req.Header.Set(
                "Content-Type",
                "application/json",
            )

            recorder := httptest.NewRecorder()

            handler(recorder, req)

            if recorder.Code != tt.wantStatus {
                t.Errorf(
                    "Expected HTTP %d, got %d. Body: %s",
                    tt.wantStatus,
                    recorder.Code,
                    recorder.Body.String(),
                )
            }
        })
    }
}