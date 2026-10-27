package com.yussufajao.ledger.account.api;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.yussufajao.ledger.account.application.AccountService;
import com.yussufajao.ledger.account.domain.Account;
import com.yussufajao.ledger.account.domain.AccountType;
import com.yussufajao.ledger.account.domain.Currency;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

	private final AccountService accounts;

	public AccountController(AccountService accounts) {
		this.accounts = accounts;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AccountResponse open(@Valid @RequestBody OpenAccountRequest request) {
		Account account = accounts.open(
				request.externalReference(),
				request.name(),
				AccountType.parse(request.accountType()),
				Currency.of(request.currency()));

		return AccountResponse.from(account);
	}

	@GetMapping("/{accountId}")
	public AccountResponse get(@PathVariable UUID accountId) {
		return AccountResponse.from(accounts.get(accountId));
	}
}
