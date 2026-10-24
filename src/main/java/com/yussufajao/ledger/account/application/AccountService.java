package com.yussufajao.ledger.account.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yussufajao.ledger.account.domain.Account;
import com.yussufajao.ledger.account.domain.AccountRepository;
import com.yussufajao.ledger.account.domain.AccountType;
import com.yussufajao.ledger.account.domain.Currency;
import com.yussufajao.ledger.account.domain.DuplicateAccountException;

@Service
public class AccountService {

	private final AccountRepository accounts;

	public AccountService(AccountRepository accounts) {
		this.accounts = accounts;
	}

	@Transactional
	public Account open(String externalReference, String name, AccountType accountType, Currency currency) {
		Account account = Account.open(externalReference, name, accountType, currency);
		if (accounts.existsByExternalReferenceAndCurrency(account.externalReference(), account.currency())) {
			throw new DuplicateAccountException(account.externalReference(), account.currency().code());
		}
		return accounts.save(account);
	}

	@Transactional(readOnly = true)
	public Account get(UUID id) {
		return accounts.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
	}
}
