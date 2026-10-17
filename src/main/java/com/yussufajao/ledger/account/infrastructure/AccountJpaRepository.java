package com.yussufajao.ledger.account.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, UUID> {
	boolean existsByExternalReferenceAndCurrency(String externalReference, String currency);
}
