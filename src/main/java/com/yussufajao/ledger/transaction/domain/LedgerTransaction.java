package com.yussufajao.ledger.transaction.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.yussufajao.ledger.account.domain.Currency;

public final class LedgerTransaction {

	private final UUID id;
	private final String reference;
	private final TransactionType type;
	private final TransactionStatus status;
	private final Currency currency;
	private final String description;
	private final List<Posting> postings;
	private final Instant createdAt;
	private final Instant postedAt;

	private LedgerTransaction(
			UUID id,
			String reference,
			TransactionType type,
			TransactionStatus status,
			Currency currency,
			String description,
			List<Posting> postings,
			Instant createdAt,
			Instant postedAt) {
		if (id == null || currency == null || type == null || status == null || createdAt == null || postedAt == null) {
			throw new InvalidTransferException("transaction fields are required");
		}

		if (reference == null || reference.isBlank() || reference.trim().length() > 128) {
			throw new InvalidTransferException("description is required and must be at most 255 characters");
		}

		if (description == null || description.isBlank() || description.trim().length() > 255) {
			throw new InvalidTransferException("description is required and must be at most 255 characters");
		}

		if (postings == null || postings.size() < 2) {
			throw new InvalidTransferException("a transaction needs at least two postings");
		}

		long sum = 0L;
		for (Posting posting : postings) {
			if (!posting.currency().code().equals(currency.code())) {
				throw new InvalidTransferException("every posting must use the transaction currency");
			}
			try {
				sum = Math.addExact(sum, posting.amountMinor());
			} catch (ArithmeticException ex) {
				throw new InvalidTransferException("posting amounts overflow");
			}
		}
		if (sum != 0L) {
			throw new InvalidTransferException("postings must sum to zero");
		}

		if (postings.get(0).acccountId().equals(postings.get(1).acccountId())) {
			throw new InvalidTransferException("source and destination accounts must differ");
		}

		this.id = id;
		this.reference = reference;
		this.type = type;
		this.status = status;
		this.currency = currency;
		this.description = description.trim();
		this.postings = List.copyOf(postings);
		this.createdAt = createdAt;
		this.postedAt = postedAt;
	}

	public static LedgerTransaction transfer(
			UUID sourceAccountId,
			UUID destinationAccountId,
			long amountMinor,
			Currency currency,
			String reference,
			String description) {
		Instant now = Instant.now();
		return new LedgerTransaction(UUID.randomUUID(), reference, TransactionType.TRANSFER, TransactionStatus.POSTED,
				currency, description, List.of(
						Posting.debit(sourceAccountId, amountMinor, currency),
						Posting.credit(destinationAccountId, amountMinor, currency)),
				now, now);
	}

	public UUID id() {
		return id;
	}

	public String reference() {
		return reference;
	}

	public TransactionType type() {
		return type;
	}

	public TransactionStatus status() {
		return status;
	}

	public Currency currency() {
		return currency;
	}

	public String description() {
		return description;
	}

	public List<Posting> postings() {
		return postings;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant postedAt() {
		return postedAt;
	}
}
