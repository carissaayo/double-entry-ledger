package com.yussufajao.ledger.account.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.yussufajao.ledger.account.domain.Account;
import com.yussufajao.ledger.account.domain.AccountRepository;
import com.yussufajao.ledger.account.domain.AccountType;
import com.yussufajao.ledger.account.domain.Currency;
import com.yussufajao.ledger.account.domain.DuplicateAccountException;

class AccountServiceTest {

	private final InMemoryAccounts accounts = new InMemoryAccounts();
	private final AccountService service = new AccountService(accounts);

	@Test
	void openStartsAtZeroAndCanBeLoaded() {
		Account opened = service.open(
				" customer-123 ",
				"Customer Wallet",
				AccountType.CUSTOMER_WALLET,
				Currency.of("NGN"));

		Account loaded = service.get(opened.id());

		assertThat(loaded.externalReference()).isEqualTo("customer-123");
		assertThat(loaded.cachedBalanceMinor()).isZero();
		assertThat(loaded.currency().code()).isEqualTo("NGN");
	}

	@Test
	void openRejectsTheSameReferenceAndCurrency() {
		service.open("customer-123", "Customer Wallet", AccountType.CUSTOMER_WALLET, Currency.of("NGN"));

		assertThatThrownBy(() -> service.open(
				"customer-123",
				"Another Wallet",
				AccountType.CUSTOMER_WALLET,
				Currency.of("NGN")))
				.isInstanceOf(DuplicateAccountException.class);
	}

	@Test
	void getRejectsAnUnknownId() {
		UUID missing = UUID.randomUUID();

		assertThatThrownBy(() -> service.get(missing))
				.isInstanceOf(AccountNotFoundException.class);
	}

	private static final class InMemoryAccounts implements AccountRepository {

		private final Map<UUID, Account> byId = new HashMap<>();

		@Override
		public Account save(Account account) {
			boolean duplicate = byId.values().stream()
					.anyMatch(existing -> existing.externalReference().equals(account.externalReference())
							&& existing.currency().code().equals(account.currency().code()));
			if (duplicate) {
				throw new DuplicateAccountException(account.externalReference(), account.currency().code());
			}
			byId.put(account.id(), account);
			return account;
		}

		@Override
		public Optional<Account> findById(UUID id) {
			return Optional.ofNullable(byId.get(id));
		}

		@Override
		public boolean existsByExternalReferenceAndCurrency(String externalReference, Currency currency) {
			return byId.values().stream().anyMatch(existing -> existing.externalReference().equals(externalReference)
					&& existing.currency().code().equals(currency.code()));
		}
	}
}
