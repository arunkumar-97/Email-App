package com.jesperapps.email.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jesperapps.email.api.model.EventRemainder;

public interface EventRemainderRepository extends JpaRepository<EventRemainder, Integer>{

	EventRemainder findByEventRemainderId(Integer eventRemainderId);

}
