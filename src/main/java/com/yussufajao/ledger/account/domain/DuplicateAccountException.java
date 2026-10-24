package com.yussufajao.ledger.account.domain;

public class DuplicateAccountException extends RuntimeException {

	public DuplicateAccountException(String externalReference, String currency) {
		super("an account already exists for " + externalReference + " and " + currency);
	}
}
