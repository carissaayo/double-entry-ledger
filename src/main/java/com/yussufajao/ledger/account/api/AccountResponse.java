package com.yussufajao.ledger.account.api;

import java.time.Instant;
import java.util.UUID;

import com.yussufajao.ledger.account.domain.Account;
import com.yussufajao.ledger.account.domain.AccountStatus;
import com.yussufajao.ledger.account.domain.AccountType;

public record AccountResponse(
		UUID id,
		String externalReference,
		String name,
		AccountType accountType,
		String currency,
		AccountStatus status,
		long cachedBalanceMinor,
		long version,
		Instant createdAt,
		Instant updatedAt) {
	static AccountResponse from(Account account) {
		return new AccountResponse(
				account.id(),
				account.externalReference(),
				account.name(),
				account.accountType(),
				account.currency().code(),
				account.status(),
				account.cachedBalanceMinor(),
				account.version(),
				account.createdAt(), account.updatedAt());
	}
}
