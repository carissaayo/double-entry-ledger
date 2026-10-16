package com.yussufajao.ledger.account.domain;

public enum AccountType {
	CUSTOMER_WALLET,
	MERCHANT_WALLET;

	public static AccountType parse(String raw) {
		try {
			return AccountType.valueOf(raw);
		} catch (IllegalArgumentException | NullPointerException ex) {
			throw new InvalidAccountException(
					"accountType must be CUSTOMER_WALLET or MERCHANT_WALLET");
		}
	}
}
