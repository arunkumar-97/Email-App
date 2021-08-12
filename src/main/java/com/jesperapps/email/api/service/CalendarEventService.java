package com.jesperapps.email.api.service;

import java.util.List;
import java.util.Optional;

import com.jesperapps.email.api.message.CalendarEventRequestEntity;
import com.jesperapps.email.api.model.CalendarEvent;

public interface CalendarEventService  {

	CalendarEvent createevent(CalendarEventRequestEntity calendarEventRequestEntity);

	List<CalendarEvent> findall();

	Optional<CalendarEvent> findbyid(Integer calendareventid);

	CalendarEvent updateevent(CalendarEvent calendarEvent);
	

}
