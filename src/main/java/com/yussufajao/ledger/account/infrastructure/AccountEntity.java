package com.yussufajao.ledger.account.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.yussufajao.ledger.account.domain.AccountStatus;
import com.yussufajao.ledger.account.domain.AccountType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "accounts")
public class AccountEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "external_reference", nullable = false, length = 128)
	private String externalReference;

	@Column(nullable = false, length = 255)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "account_type", nullable = false, length = 32)
	private AccountType accountType;

	@Column(nullable = false, length = 3)
	private String currency;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private AccountStatus status;

	@Column(name = "cached_balance_minor", nullable = false)
	private long cachedBalanceMinor;

	@Column(nullable = false)
	private long version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Transient
	private boolean newAccount = true;

	protected AccountEntity() {
	}

	public static AccountEntity from(com.yussufajao.ledger.account.domain.Account account) {
		AccountEntity row = new AccountEntity();
		row.id = account.id();
		row.externalReference = account.externalReference();
		row.name = account.name();
		row.accountType = account.accountType();
		row.currency = account.currency().code();
		row.status = account.status();
		row.cachedBalanceMinor = account.cachedBalanceMinor();
		row.version = account.version();
		row.createdAt = account.createdAt();
		row.updatedAt = account.updatedAt();
		row.newAccount = true;

		return row;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return newAccount;
	}

	@PostPersist
	@PostLoad
	void markNotNew() {
		newAccount = false;
	}

	public String getExternalReference() {
		return externalReference;
	}

	public String getName() {
		return name;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public String getCurrency() {
		return currency;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public long getCachedBalanceMinor() {
		return cachedBalanceMinor;
	}

	public long getVersion() {
		return version;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

}
