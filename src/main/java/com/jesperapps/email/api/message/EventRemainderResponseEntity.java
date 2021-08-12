package com.jesperapps.email.api.message;

import com.jesperapps.email.api.model.BaseResponse;
import com.jesperapps.email.api.model.CalendarEvent;
import com.jesperapps.email.api.model.EventRemainder;

public class EventRemainderResponseEntity extends BaseResponse{
	
	private Integer eventRemainderId;
	private String eventRemainderType;
	private boolean eventRemainderBefore;
	private String eventRemainderTime;
	private CalendarEvent calendarEvent;
	
	public EventRemainderResponseEntity() {
		
	}
	
	public EventRemainderResponseEntity(EventRemainder eventRemainder) {
		this.eventRemainderId=eventRemainder.getEventRemainderId();
		this.eventRemainderType=eventRemainder.getEventRemainderType();
		this.eventRemainderBefore=eventRemainder.isEventRemainderBefore();
		this.eventRemainderTime=eventRemainder.getEventRemainderTime();

		
	}
	public EventRemainderResponseEntity(int i, String string) {
		super(i,string);
	}

	public Integer getEventRemainderId() {
		return eventRemainderId;
	}
	public void setEventRemainderId(Integer eventRemainderId) {
		this.eventRemainderId = eventRemainderId;
	}
	public String getEventRemainderType() {
		return eventRemainderType;
	}
	public void setEventRemainderType(String eventRemainderType) {
		this.eventRemainderType = eventRemainderType;
	}
	public boolean isEventRemainderBefore() {
		return eventRemainderBefore;
	}
	public void setEventRemainderBefore(boolean eventRemainderBefore) {
		this.eventRemainderBefore = eventRemainderBefore;
	}
	public String getEventRemainderTime() {
		return eventRemainderTime;
	}
	public void setEventRemainderTime(String eventRemainderTime) {
		this.eventRemainderTime = eventRemainderTime;
	}
	public CalendarEvent getCalendarEvent() {
		return calendarEvent;
	}
	public void setCalendarEvent(CalendarEvent calendarEvent) {
		this.calendarEvent = calendarEvent;
	}
	
	
	
	

}
