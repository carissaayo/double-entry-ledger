package com.yussufajao.ledger.account.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AccountApiIT {

	@Container
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

	@DynamicPropertySource
	static void datasource(DynamicPropertyRegistry registry) {
		registry.add("LEDGER_DATASOURCE_URL", postgres::getJdbcUrl);
		registry.add("LEDGER_DATASOURCE_USERNAME", postgres::getUsername);
		registry.add("LEDGER_DATASOURCE_PASSWORD", postgres::getPassword);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void postThenGetPersistsAZeroBalance() throws Exception {
		MvcResult created = mockMvc.perform(post("/api/v1/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(accountJson("customer-roundtrip", "Customer Wallet", "CUSTOMER_WALLET", "NGN")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.cachedBalanceMinor").value(0))
				.andReturn();

		String id = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

		mockMvc.perform(get("/api/v1/accounts/{accountId}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.externalReference").value("customer-roundtrip"))
				.andExpect(jsonPath("$.currency").value("NGN"))
				.andExpect(jsonPath("$.cachedBalanceMinor").value(0));

		Long storedBalance = jdbc.queryForObject(
				"select cached_balance_minor from accounts where id = ?",
				Long.class,
				UUID.fromString(id));
		assertThat(storedBalance).isZero();
	}

	@Test
	void secondPostWithTheSameReferenceAndCurrencyConflicts() throws Exception {
		String body = accountJson("customer-duplicate", "Customer Wallet", "CUSTOMER_WALLET", "NGN");

		mockMvc.perform(post("/api/v1/accounts").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/accounts").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Duplicate account"));
	}

	@Test
	void getRejectsAnUnknownId() throws Exception {
		mockMvc.perform(get("/api/v1/accounts/{accountId}", "11111111-1111-1111-1111-111111111111"))
				.andExpect(status().isNotFound());
	}

	private static String accountJson(String reference, String name, String accountType, String currency) {
		return """
				{
				  "externalReference": "%s",
				  "name": "%s",
				  "accountType": "%s",
				  "currency": "%s"
				}
				""".formatted(reference, name, accountType, currency);
	}
}
