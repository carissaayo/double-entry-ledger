package com.yussufajao.ledger.account.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AccountTest {

	@Test
	void openStartsAtZeroForAnActiveWallet() {
		Account account = Account.open(
				" customer-123 ",
				"Customer Wallet",
				AccountType.CUSTOMER_WALLET,
				Currency.of("NGN"));

		assertThat(account.id()).isNotNull();
		assertThat(account.externalReference()).isEqualTo("customer-123");
		assertThat(account.status()).isEqualTo(AccountStatus.ACTIVE);
		assertThat(account.cachedBalanceMinor()).isZero();
		assertThat(account.version()).isZero();
		assertThat(account.currency().code()).isEqualTo("NGN");
	}

	@Test
	void openRejectsALowercaseCurrency() {
		assertThatThrownBy(() -> Currency.of("ngn"))
				.isInstanceOf(InvalidAccountException.class);
	}

	@Test
	void openRejectsAnUnknownAccountType() {
		assertThatThrownBy(() -> AccountType.parse("CLEARING"))
				.isInstanceOf(InvalidAccountException.class);
	}

	@Test
	void restoreRejectsANegativeCachedBalance() {
		Account created = Account.open(
				"customer-123",
				"Customer Wallet",
				AccountType.CUSTOMER_WALLET,
				Currency.of("NGN"));

		assertThatThrownBy(() -> Account.restore(
				created.id(),
				created.externalReference(),
				created.name(),
				created.accountType(),
				created.currency(),
				created.status(),
				-1L,
				created.version(),
				created.createdAt(),
				created.updatedAt()))
				.isInstanceOf(InvalidAccountException.class);
	}
}
