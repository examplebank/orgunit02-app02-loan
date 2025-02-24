package com.example.bank.loan.domain.model.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.springframework.util.Assert;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
public abstract class BaseAggregateRoot<A extends BaseAggregateRoot<A, Id>, Id extends Serializable> extends BaseEntity<Id> {

	private transient final @Transient List<Object> domainEvents = new ArrayList<>();

	/**
	 * Registers the given event object for publication on a call to a Spring Data repository's save or delete methods.
	 *
	 * @param event must not be {@literal null}.
	 * @return the event that has been added.
	 * @see #andEvent(Object)
	 */
	protected <T> T registerEvent(T event) {

		Assert.notNull(event, "Domain event must not be null");

		this.domainEvents.add(event);
		return event;
	}

	/**
	 * Clears all domain events currently held. Usually invoked by the infrastructure in place in Spring Data
	 * repositories.
	 */
	@AfterDomainEventPublication
	protected void clearDomainEvents() {
		this.domainEvents.clear();
	}

	/**
	 * All domain events currently captured by the aggregate.
	 */
	@DomainEvents
	protected Collection<Object> domainEvents() {
		return Collections.unmodifiableList(domainEvents);
	}

	/**
	 * Adds all events contained in the given aggregate to the current one.
	 *
	 * @param aggregate must not be {@literal null}.
	 * @return the aggregate
	 */
	@SuppressWarnings("unchecked")
	protected final A andEventsFrom(A aggregate) {

		Assert.notNull(aggregate, "Aggregate must not be null");

		this.domainEvents.addAll(aggregate.domainEvents());

		return (A) this;
	}

	/**
	 * Adds the given event to the aggregate for later publication
	 * when calling a Spring Data repository's save or delete method.
	 * Does the same as {@link #registerEvent(Object)} but returns the aggregate instead of the event.
	 *
	 * @param event must not be {@literal null}.
	 * @return the aggregate
	 * @see #registerEvent(Object)
	 */
	@SuppressWarnings("unchecked")
	protected final A andEvent(Object event) {

		registerEvent(event);

		return (A) this;
	}
}
