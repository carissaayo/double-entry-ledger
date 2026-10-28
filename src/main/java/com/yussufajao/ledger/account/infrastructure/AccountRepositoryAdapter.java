package com.yussufajao.ledger.account.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.yussufajao.ledger.account.domain.Account;
import com.yussufajao.ledger.account.domain.AccountRepository;
import com.yussufajao.ledger.account.domain.Currency;

@Component
public class AccountRepositoryAdapter implements AccountRepository {

	private final AccountJpaRepository accounts;

	public AccountRepositoryAdapter(AccountJpaRepository accounts) {
		this.accounts = accounts;
	}

	@Override
	@Transactional
	public Account save(Account account) {
		AccountEntity saved = accounts.save(AccountEntity.from(account));
		return toDomain(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Account> findById(UUID id) {
		return accounts.findById(id).map(AccountRepositoryAdapter::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByExternalReferenceAndCurrency(String externalReference, Currency currency) {
		return accounts.existsByExternalReferenceAndCurrency(externalReference, currency.code());
	}

	private static Account toDomain(AccountEntity row) {
		return Account.restore(
				row.getId(),
				row.getExternalReference(),
				row.getName(),
				row.getAccountType(),
				Currency.of(row.getCurrency().trim()),
				row.getStatus(),
				row.getCachedBalanceMinor(),
				row.getVersion(),
				row.getCreatedAt(),
				row.getUpdatedAt());
	}
}
