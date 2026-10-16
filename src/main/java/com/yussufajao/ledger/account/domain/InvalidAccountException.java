package com.yussufajao.ledger.account.domain;

public class InvalidAccountException extends RuntimeException {

	public InvalidAccountException(String message) {
		super(message);
	}
}
