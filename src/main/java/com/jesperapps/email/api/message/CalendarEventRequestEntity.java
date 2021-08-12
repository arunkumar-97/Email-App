package com.jesperapps.email.api.message;

import java.util.List;

import com.jesperapps.email.api.model.EventRemainder;
import com.jesperapps.email.api.model.User;

public class CalendarEventRequestEntity {
	
	private Integer calendarEventId;
	private String title;
	private String startDate;
	private String endDate;
	private String startTime;
	private String endTime;
	
	private String[] eventParticipants;	
	private String eventLocation;
	private String meetingUrl;
	private Integer status;
	private String eventDescription;
	private User user;
	private List<EventRemainder> eventRemainder;
	
	
	public CalendarEventRequestEntity() {
		
	}


	public Integer getCalendarEventId() {
		return calendarEventId;
	}


	public void setCalendarEventId(Integer calendarEventId) {
		this.calendarEventId = calendarEventId;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getStartDate() {
		return startDate;
	}


	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}


	public String getEndDate() {
		return endDate;
	}
	
	
	public String getStartTime() {
		return startTime;
	}


	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}


	public String getEndTime() {
		return endTime;
	}


	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}




	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}


	public String[] getEventParticipants() {
		return eventParticipants;
	}


	public void setEventParticipants(String[] eventParticipants) {
		this.eventParticipants = eventParticipants;
	}


	public String getEventLocation() {
		return eventLocation;
	}


	public void setEventLocation(String eventLocation) {
		this.eventLocation = eventLocation;
	}


	public String getMeetingUrl() {
		return meetingUrl;
	}


	public void setMeetingUrl(String meetingUrl) {
		this.meetingUrl = meetingUrl;
	}


	public Integer getStatus() {
		return status;
	}


	public void setStatus(Integer status) {
		this.status = status;
	}


	public String getEventDescription() {
		return eventDescription;
	}


	public void setEventDescription(String eventDescription) {
		this.eventDescription = eventDescription;
	}


	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


	public List<EventRemainder> getEventRemainder() {
		return eventRemainder;
	}


	public void setEventRemainder(List<EventRemainder> eventRemainder) {
		this.eventRemainder = eventRemainder;
	}
	
	
	
	
	

}
