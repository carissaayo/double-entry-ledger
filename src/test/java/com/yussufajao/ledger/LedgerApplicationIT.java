package com.yussufajao.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class LedgerApplicationIT {

	@Container
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

	@DynamicPropertySource
	static void datasource(DynamicPropertyRegistry registry) {
		registry.add("LEDGER_DATASOURCE_URL", postgres::getJdbcUrl);
		registry.add("LEDGER_DATASOURCE_USERNAME", postgres::getUsername);
		registry.add("LEDGER_DATASOURCE_PASSWORD", postgres::getPassword);
		registry.add("management.endpoint.health.show-details", () -> "always");
		registry.add("management.endpoint.health.show-components", () -> "always");
		registry.add("management.endpoint.health.group.readiness.show-details", () -> "always");
		registry.add("management.endpoint.health.group.liveness.show-details", () -> "always");
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void flywayAppliesAccountsMigration() {
		Long applied = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success",
				Long.class);
		assertThat(applied).isEqualTo(1L);

		List<String> columns = jdbcTemplate.queryForList(
				"""
				SELECT column_name
				FROM information_schema.columns
				WHERE table_schema = 'public' AND table_name = 'accounts'
				ORDER BY column_name
				""",
				String.class);
		assertThat(columns).containsExactly(
				"account_type",
				"cached_balance_minor",
				"created_at",
				"currency",
				"external_reference",
				"id",
				"name",
				"status",
				"updated_at",
				"version");
	}

	@Test
	void readinessIncludesDatabaseAndLivenessDoesNot() throws Exception {
		mockMvc.perform(get("/actuator/health/readiness"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components.db.status").value("UP"));

		mockMvc.perform(get("/actuator/health/liveness"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.components.db").doesNotExist());
	}
}
