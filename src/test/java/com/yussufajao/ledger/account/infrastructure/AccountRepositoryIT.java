package com.yussufajao.ledger.account.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.yussufajao.ledger.account.domain.Account;
import com.yussufajao.ledger.account.domain.AccountRepository;
import com.yussufajao.ledger.account.domain.AccountStatus;
import com.yussufajao.ledger.account.domain.AccountType;
import com.yussufajao.ledger.account.domain.Currency;

@SpringBootTest
@Testcontainers
class AccountRepositoryIT {

	@Container
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

	@DynamicPropertySource
	static void datasource(DynamicPropertyRegistry registry) {
		registry.add("LEDGER_DATASOURCE_URL", postgres::getJdbcUrl);
		registry.add("LEDGER_DATASOURCE_USERNAME", postgres::getUsername);
		registry.add("LEDGER_DATASOURCE_PASSWORD", postgres::getPassword);
	}

	@Autowired
	private AccountRepository repository;

	@Test
	void saveThenFindReturnsTheSameAccount() {
		Account opened = Account.open(
				"customer-123",
				"Customer Wallet",
				AccountType.CUSTOMER_WALLET,
				Currency.of("NGN"));

		repository.save(opened);

		Account loaded = repository.findById(opened.id()).orElseThrow();

		assertThat(loaded.externalReference()).isEqualTo("customer-123");
		assertThat(loaded.name()).isEqualTo("Customer Wallet");
		assertThat(loaded.accountType()).isEqualTo(AccountType.CUSTOMER_WALLET);
		assertThat(loaded.currency().code()).isEqualTo("NGN");
		assertThat(loaded.status()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(loaded.cachedBalanceMinor()).isZero();
		assertThat(loaded.version()).isZero();
		assertThat(repository.existsByExternalReferenceAndCurrency("customer-123", Currency.of("NGN"))).isTrue();
	}

	@Test
	void duplicateExternalReferenceAndCurrencyIsRejected() {
		Account first = Account.open("customer-123", "Customer Wallet", AccountType.CUSTOMER_WALLET,
				Currency.of("NGN"));
		Account second = Account.open("customer-123", "Another Wallet", AccountType.CUSTOMER_WALLET,
				Currency.of("NGN"));

		repository.save(first);

		assertThatThrownBy(() -> repository.save(second))
				.isInstanceOf(DataIntegrityViolationException.class);
	}
}
