package com.jesperapps.email.api.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.jesperapps.email.api.message.EventRemainderRequestEntity;

@Entity
public class EventRemainder {

	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer eventRemainderId;
	private String eventRemainderType;
	private boolean eventRemainderBefore;
	private String eventRemainderTime;
	
	@ManyToOne
	@JoinColumn
	private CalendarEvent calendarEvent;
	
	public EventRemainder() {
		
	}
	
	public EventRemainder(EventRemainderRequestEntity eventRemainderRequestEntity) {
		this.eventRemainderId=eventRemainderRequestEntity.getEventRemainderId();
		this.eventRemainderType=eventRemainderRequestEntity.getEventRemainderType();
		this.eventRemainderBefore=eventRemainderRequestEntity.isEventRemainderBefore();
		this.eventRemainderTime=eventRemainderRequestEntity.getEventRemainderTime();
		this.calendarEvent=eventRemainderRequestEntity.getCalendarEvent();
	}
	public EventRemainder(EventRemainder each, CalendarEvent calendarEventDB) {
		this.eventRemainderId=each.getEventRemainderId();
		this.eventRemainderType=each.getEventRemainderType();
		this.eventRemainderBefore=each.isEventRemainderBefore();
		this.eventRemainderTime=each.getEventRemainderTime();
		this.calendarEvent=calendarEventDB;
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
