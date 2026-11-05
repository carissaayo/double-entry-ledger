package com.yussufajao.ledger.transaction.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.yussufajao.ledger.account.domain.Currency;

class LedgerTransactionTest {

	private final UUID source = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
	private final UUID destination = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

	@Test
	void transferBalancesADebitAndACredit() {
		LedgerTransaction transaction = LedgerTransaction.transfer(
				source,
				destination,
				2_500_000L,
				Currency.of("NGN"),
				"order-10452",
				"Merchant settlement");

		assertThat(transaction.type()).isEqualTo(TransactionType.TRANSFER);
		assertThat(transaction.status()).isEqualTo(TransactionStatus.POSTED);
		assertThat(transaction.postings()).hasSize(2);
		assertThat(transaction.postings().get(0).amountMinor()).isEqualTo(-2_500_000L);
		assertThat(transaction.postings().get(1).amountMinor()).isEqualTo(2_500_000L);
		assertThat(transaction.postings().stream().mapToLong(Posting::amountMinor).sum()).isZero();
	}

	@Test
	void transferRejectsTheSameAccountOnBothSides() {
		assertThatThrownBy(() -> LedgerTransaction.transfer(
				source,
				source,
				2_500_000L,
				Currency.of("NGN"),
				"order-10452",
				"Merchant settlement"))
				.isInstanceOf(InvalidTransferException.class);
	}

	@Test
	void transferRejectsAZeroAmount() {
		assertThatThrownBy(() -> LedgerTransaction.transfer(
				source,
				destination,
				0L,
				Currency.of("NGN"),
				"order-10452",
				"Merchant settlement"))
				.isInstanceOf(InvalidTransferException.class);
	}
}
