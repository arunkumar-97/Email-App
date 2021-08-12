package com.jesperapps.email.api.service;

import java.util.List;

import com.jesperapps.email.api.message.EventRemainderResponseEntity;
import com.jesperapps.email.api.model.EventRemainder;

public interface EventRemainderService {

	EventRemainder createEventRemainder(EventRemainder eventRemainder);

	List<EventRemainder> findAll();

	EventRemainder save(EventRemainder item);

	EventRemainder findById(Integer eventRemainderId);

	EventRemainder saveEventRemainder(EventRemainder eventRemainder);

}
