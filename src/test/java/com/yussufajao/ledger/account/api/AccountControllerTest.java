package com.yussufajao.ledger.account.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.yussufajao.ledger.account.application.AccountNotFoundException;
import com.yussufajao.ledger.account.application.AccountService;
import com.yussufajao.ledger.account.domain.Account;
import com.yussufajao.ledger.account.domain.AccountStatus;
import com.yussufajao.ledger.account.domain.AccountType;
import com.yussufajao.ledger.account.domain.Currency;
import com.yussufajao.ledger.account.domain.DuplicateAccountException;
import com.yussufajao.ledger.account.domain.InvalidAccountException;

@WebMvcTest(AccountController.class)
@Import(AccountExceptionHandler.class)
class AccountControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AccountService accounts;

	@Test
	void openReturnsTheCreatedAccount() throws Exception {
		when(accounts.open(anyString(), anyString(), any(), any())).thenReturn(sample());

		mockMvc.perform(post("/api/v1/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "externalReference": "customer-123",
						  "name": "Customer Wallet",
						  "accountType": "CUSTOMER_WALLET",
						  "currency": "NGN"
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.externalReference").value("customer-123"))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.cachedBalanceMinor").value(0));
	}

	@Test
	void openRejectsABalanceSuppliedByTheClient() throws Exception {
		mockMvc.perform(post("/api/v1/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "externalReference": "customer-123",
						  "name": "Customer Wallet",
						  "accountType": "CUSTOMER_WALLET",
						  "currency": "NGN",
						  "cachedBalanceMinor": 5000
						}
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void openRejectsAnUnknownAccountType() throws Exception {
		when(accounts.open(anyString(), anyString(), any(), any()))
				.thenThrow(new InvalidAccountException("accountType must be CUSTOMER_WALLET or MERCHANT_WALLET"));

		mockMvc.perform(post("/api/v1/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "externalReference": "customer-123",
						  "name": "Customer Wallet",
						  "accountType": "CLEARING",
						  "currency": "NGN"
						}
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void openRejectsADuplicate() throws Exception {
		when(accounts.open(anyString(), anyString(), any(), any()))
				.thenThrow(new DuplicateAccountException("customer-123", "NGN"));

		mockMvc.perform(post("/api/v1/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "externalReference": "customer-123",
						  "name": "Customer Wallet",
						  "accountType": "CUSTOMER_WALLET",
						  "currency": "NGN"
						}
						"""))
				.andExpect(status().isConflict());
	}

	@Test
	void getRejectsAnUnknownId() throws Exception {
		UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
		when(accounts.get(id)).thenThrow(new AccountNotFoundException(id));

		mockMvc.perform(get("/api/v1/accounts/{accountId}", id))
				.andExpect(status().isNotFound());
	}

	private static Account sample() {
		Instant now = Instant.parse("2026-10-24T10:15:30Z");
		return Account.restore(
				UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
				"customer-123",
				"Customer Wallet",
				AccountType.CUSTOMER_WALLET,
				Currency.of("NGN"),
				AccountStatus.ACTIVE,
				0L,
				0L,
				now,
				now);
	}
}
