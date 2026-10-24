package policy

import (
	"errors"
)

type TransferRequest struct {
	SourceAccount string `json:"source_account"`
	DestinationAccount string `json:"destination_account"`
	AmountMinor int64 `json:"amount_minor"`
	Currency string `json:"currency"`
}

type TransferPolicy struct {
	MaxAmountMinor int64
}
const (
	CurrencyGBP = "GBP"
	CurrencyUSD = "USD"
	CurrencyEUR = "EUR"
)

var (
	ErrInvalidAmount = errors.New("amount must be positive")
	ErrUnsuportedCurrency = errors.New("unsuported currency")
	ErrLimitExceed = errors.New("transfer exceeds permitted limit")
)

func(p TransferPolicy) Evaluate(req TransferRequest) error {
	// 1. Reject non positive amount
	if req.AmountMinor <= 0{
		return ErrInvalidAmount
	}

	if req.Currency != CurrencyGBP{
		return ErrUnsuportedCurrency
	}

	if req.AmountMinor > p.MaxAmountMinor {
		return ErrLimitExceed
	}

	
	return nil
}