package com.yussufajao.ledger.account.domain;

public final class Currency {

	private final String code;

	private Currency(String code) {
		this.code = code;
	}

	public static Currency of(String raw) {
		if (raw == null || !raw.matches("^[A-Z]{3}$")) {
			throw new InvalidAccountException("currency must be a 3-letter uppercase ISO code");
		}
		return new Currency(raw);
	}

	public String code() {
		return code;
	}

}
