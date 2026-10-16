package com.yussufajao.ledger.account.domain;

import java.time.Instant;
import java.util.UUID;

public class Account {

	private final UUID id;
	private final String externalReference;
	private final String name;
	private final AccountType accountType;
	private final Currency currency;
	private final AccountStatus status;
	private final long cachedBalanceMinor;
	private final long version;
	private final Instant createdAt;
	private final Instant updatedAt;

	private Account(
			UUID id,
			String externalReference,
			String name,
			AccountType accountType,
			Currency currency,
			AccountStatus status,
			long cachedBalanceMinor,
			long version,
			Instant createdAt,
			Instant updatedAt) {

		this.id = id;
		this.externalReference = requireText(externalReference, "externalReference", 128);
		this.name = requireText(name, "name", 225);
		this.accountType = requirePresent(accountType, "accountType");
		this.currency = requirePresent(currency, "currency");
		this.status = requirePresent(status, "status");
		if (cachedBalanceMinor < 0) {
			throw new InvalidAccountException("cachedBalanceMinor must be zero or greater");
		}
		if (version < 0) {
			throw new InvalidAccountException("version must be zero or greater");
		}

		this.cachedBalanceMinor = cachedBalanceMinor;
		this.version = version;
		this.createdAt = requirePresent(createdAt, "createdAt");
		this.updatedAt = requirePresent(updatedAt, "updatedAt");
	}

	public static Account open(String externalReference, String name, AccountType accountType, Currency currency) {
		Instant now = Instant.now();
		return new Account(UUID.randomUUID(), externalReference, name, accountType, currency, AccountStatus.ACTIVE, 0L,
				0L, now, now);
	}

	public static Account restore(
			UUID id,
			String externalReference,
			String name,
			AccountType accountType,
			Currency currency,
			AccountStatus status,
			long cachedBalanceMinor,
			long version,
			Instant createdAt,
			Instant updatedAt) {
		return new Account(
				requirePresent(id, "id"),
				externalReference,
				name,
				accountType,
				currency,
				status,
				cachedBalanceMinor,
				version,
				createdAt,
				updatedAt);
	}

	private static String requireText(String value, String field, int maxLength) {
		if (value == null || value.isBlank()) {
			throw new InvalidAccountException(field + "is required");
		}
		String trimmed = value.trim();
		if (trimmed.length() > maxLength) {
			throw new InvalidAccountException(field + "must be at most" + maxLength + "characters");
		}
		return trimmed;
	}

	private static <T> T requirePresent(T value, String field) {
		if (value == null) {
			throw new InvalidAccountException(field + "is required");
		}
		return value;
	}

	public UUID id() {
		return id;
	}

	public String externalReference() {
		return externalReference;
	}

	public String name() {
		return name;
	}

	public AccountType accountType() {
		return accountType;
	}

	public Currency currency() {
		return currency;
	}

	public AccountStatus status() {
		return status;
	}

	public long cachedBalanceMinor() {
		return cachedBalanceMinor;
	}

	public long version() {
		return version;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
