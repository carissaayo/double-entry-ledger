# ADR 0001: Posting sign, balance cache, and MVP accounts

Status: Accepted
Date: 2026-10-16

## Context

The ledger records transfers as immutable journals. Every later feature (idempotency, concurrency, reversals, the outbox, reconciliation) depends on three choices:

- How a posting stores debit and credit.
- Whether an account balance is derived from the journal, cached, or both.
- Which account types exist in the MVP, and what their balance is allowed to do.

Amounts are integer minor units (`long` in Java, `BIGINT` in PostgreSQL). NGN 25,000.00 is `2500000` with currency `NGN`. Floating-point money is out of scope.

## Decision

### Signed postings

A posting stores one signed `amount_minor`. There is no separate direction column.

- A positive amount is a credit.
- A negative amount is a debit.
- The amount is never zero.
- A transaction is balanced when the sum of its posting amounts is zero.
- Every posting in a transaction uses that transaction's currency.

The HTTP API may still say debit and credit. The service translates those words into the sign before anything is stored.

### Journal is the source of truth; the cache is spendable balance

`accounts.cached_balance_minor` is the sum of signed postings on that account. It is the spendable balance in minor units.

- The journal is the source of truth.
- When the cache exists, it changes in the same database transaction as the postings.
- A response must not report success before that transaction commits.
- Reconciliation will recompute the sum from postings and compare it with the cache. That job is not part of this scaffold.

For the MVP wallet types, the cache must stay greater than or equal to zero. `V1__create_accounts.sql` enforces that with `accounts_cached_balance_non_negative_check`. An overdraft attempt rolls back with the journal.

### MVP accounts

Account types:

- `CUSTOMER_WALLET`
- `MERCHANT_WALLET`

Both are wallets that hold funds. The non-negative cache check applies to both. A later debit-normal account (clearing, expense, or a system funding account) needs its own rule. Do not reuse this check for that type without a new decision.

Status:

```text
ACTIVE -> FROZEN -> CLOSED
```

- `ACTIVE` accounts can be posted.
- `FROZEN` accounts stay readable and cannot take part in a new transfer.
- `CLOSED` accounts cannot be reopened in the MVP.

An account is created with a cached balance of zero. Funding it is a balanced journal, not a direct write to the cache. Same currency is required on both sides of a transfer. The pair `(external_reference, currency)` is unique.

## Example

Transfer NGN 25,000.00 from customer wallet A to merchant wallet B. A starts at NGN 30,000.00. B starts at NGN 0.00.

| Account | Posting `amount_minor` | Meaning |
| --- | ---: | --- |
| A | -2500000 | Debit |
| B | +2500000 | Credit |

The postings sum to zero. After the same transaction commits, A is `500000` and B is `2500000`.

If A had started below `2500000`, the non-negative check and the posting service both reject the transfer. No posting is stored.

A reversal is a new balanced journal with the signs flipped. It does not update or delete the original postings. Reversal behavior itself is a later phase.

## Consequences

- Balancing is one expression: the signed sum is zero. Database constraints and tests can assert that without interpreting a direction enum.
- Readers of the API still see debit and credit. The mapping lives at the boundary, not in the journal.
- The cache can drift from the journal if a future bug updates one and not the other. The non-negative check stops a negative cache. It does not prove the cache matches the journal. Reconciliation remains necessary.
- The non-negative check will be wrong for a debit-normal account. Adding that account type requires a new migration and a new ADR.
- Opening balances, holds, and available-versus-ledger balances are not in this decision. Until holds exist, ledger balance and available balance are the same number.
