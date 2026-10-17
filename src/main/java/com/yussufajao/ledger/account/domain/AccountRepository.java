package com.yussufajao.ledger.account.domain;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

	Account save(Account account);

	Optional<Account> findById(UUID id);

	boolean existsByExternalReferenceAndCurrency(String externalReference, Currency currency);
}
