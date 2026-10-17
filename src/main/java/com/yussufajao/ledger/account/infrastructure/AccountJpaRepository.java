package com.yussufajao.ledger.account.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yussufajao.ledger.account.domain.Currency;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {
	boolean existsByExternalReferenceAndCurrency(String externalReference, Currency currency);
}
