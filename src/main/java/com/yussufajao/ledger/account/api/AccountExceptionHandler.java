package com.yussufajao.ledger.account.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.yussufajao.ledger.account.application.AccountNotFoundException;
import com.yussufajao.ledger.account.domain.DuplicateAccountException;
import com.yussufajao.ledger.account.domain.InvalidAccountException;

@RestControllerAdvice
public class AccountExceptionHandler {

	@ExceptionHandler(InvalidAccountException.class)
	ProblemDetail invalid(InvalidAccountException ex) {
		return problem(HttpStatus.BAD_REQUEST, "invalid account", ex.getMessage());
	}

	@ExceptionHandler(DuplicateAccountException.class)
	ProblemDetail duplicate(DuplicateAccountException ex) {
		return problem(HttpStatus.CONFLICT, "Duplicate account", ex.getMessage());
	}

	@ExceptionHandler(AccountNotFoundException.class)
	ProblemDetail missing(AccountNotFoundException ex) {
		return problem(HttpStatus.NOT_FOUND, "Account not found", ex.getMessage());
	}

	private static ProblemDetail problem(HttpStatus status, String title, String detail) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		;
		return problem;
	}
}
