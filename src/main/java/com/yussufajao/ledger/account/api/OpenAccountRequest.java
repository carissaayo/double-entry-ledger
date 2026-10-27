package com.yussufajao.ledger.account.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = false)
public record OpenAccountRequest(
		@NotBlank @Size(max = 128) String externalReference,
		@NotBlank @Size(max = 256) String name,
		@NotBlank String accountType,
		@NotBlank String currency) {

}
