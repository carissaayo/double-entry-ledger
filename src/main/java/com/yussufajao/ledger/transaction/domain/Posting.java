package com.yussufajao.ledger.transaction.domain;

import java.util.UUID;

import com.yussufajao.ledger.account.domain.Currency;

public class Posting {

	private final UUID accountId;
	private final long amountMinor;
	private final Currency currency;

	private Posting(UUID accountId, long amountMinor, Currency currency) {

		if (accountId == null) {
			throw new InvalidTransferException("accountId is required");
		}

		if (amountMinor == 0L) {
			throw new InvalidTransferException("amountMinor must not be zero");
		}

		if (currency == null) {
			throw new InvalidTransferException("currency is required");
		}

		this.accountId = accountId;
		this.amountMinor = amountMinor;
		this.currency = currency;
	}

	public static Posting debit(UUID accountId, long positiveAmountMinor, Currency currency) {
		return new Posting(accountId, negatePostive(positiveAmountMinor), currency);
	}

	public static Posting credit(UUID accountId, long positiveAmountMinor, Currency currency) {
		if (positiveAmountMinor <= 0l) {
			throw new InvalidTransferException("amountMinor must be greater than zero");
		}
		return new Posting(accountId, positiveAmountMinor, currency);
	}

	private static long negatePostive(long positiveAmountMinor) {
		if (positiveAmountMinor <= 0L) {
			throw new InvalidTransferException("amountMinor must be greater than zero");
		}

		try {
			return Math.negateExact(positiveAmountMinor);
		} catch (ArithmeticException ex) {
			throw new InvalidTransferException("amountMinor is too large");
		}
	}

	public UUID acccountId() {
		return accountId;
	}

	public long amountMinor() {
		return amountMinor;
	}

	public Currency currency() {
		return currency;
	}
}
