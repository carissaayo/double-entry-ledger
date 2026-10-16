CREATE TABLE accounts (
  id UUID PRIMARY KEY,
  external_reference VARCHAR(128) NOT NULL,
  name VARCHAR(255) NOT NULL,
  account_type VARCHAR(32) NOT NULL,
  currency CHAR(3) NOT NULL,
  status VARCHAR(16) NOT NULL,
  cached_balance_minor BIGINT NOT NULL,
  version BIGINT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT accounts_account_type_check CHECK (account_type IN ('CUSTOMER_WALLET', 'MERCHANT_WALLET')),
  CONSTRAINT accounts_status_check CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED')),
  CONSTRAINT accounts_currency_check CHECK (currency ~ '^[A-Z]{3}$'),
  CONSTRAINT accounts_cached_balance_non_negative_check CHECK (cached_balance_minor >= 0),
  CONSTRAINT accounts_external_reference_currency_key UNIQUE (external_reference, currency)
);

COMMENT ON TABLE accounts IS 'Ledger accounts. Journal history is not stored on this table.';
COMMENT ON COLUMN accounts.cached_balance_minor IS 'Spendable balance in minor units. Equals the sum of signed postings and stays non-negative for MVP wallet types. See docs/adr/0001-posting-and-balance-model.md.';
COMMENT ON COLUMN accounts.version IS 'Optimistic lock version. Not a balance.';
