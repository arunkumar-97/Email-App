package com.jesperapps.email.api.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jesperapps.email.api.message.CalendarEventRequestEntity;

@SuppressWarnings("serial")
@Entity
public class CalendarEvent extends AbstractAuditingEntity implements Serializable {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "CALENDAR_EVENT_ID")
	private Integer calendarEventId;

	@Column(name = "TITLE")	
	private String title;
	
	
	@Column(name = "START_DATE")	
	private String startDate;

	
	@Column(name = "END_DATE")	
	private String endDate;
	
	@Column(name = "START_TIME")
	private String startTime;
	
	@Column(name  = "END_TIME")
	  private String endTime;
	
	@Column(name = "PARTICIPANTS")
	private String[] eventParticipants;
	
	@Column(name = "EVENT_LOCATION")	
	private String eventLocation;
	
	@Column(name = "MEETING_URL")	
	private String meetingUrl;
	
	@Column(name = "STATUS")	
	private Integer status;
	
	@Column(name = "EVENT_DESCRIPTION")	
	private String eventDescription;
	
	@ManyToOne
	@JoinColumn(name = "user_Id", nullable = false, updatable=false )
	private User user;
	
	
	@OneToMany(mappedBy = "calendarEvent", cascade = CascadeType.ALL)
	private List<EventRemainder> eventRemainder;
	
	

	public CalendarEvent() {
		super();
	}

	
	public CalendarEvent(CalendarEventRequestEntity calendarEventRequestEntity) {
		
		this.calendarEventId = calendarEventRequestEntity.getCalendarEventId();
		this.title = calendarEventRequestEntity.getTitle();
		this.startDate = calendarEventRequestEntity.getStartDate();
		this.endDate = calendarEventRequestEntity.getEndDate();
		this.startTime = calendarEventRequestEntity.getStartTime();
		this.endTime = calendarEventRequestEntity.getEndTime();
		this.eventParticipants = calendarEventRequestEntity.getEventParticipants();
		this.eventLocation = calendarEventRequestEntity.getEventLocation();
		this.meetingUrl = calendarEventRequestEntity.getMeetingUrl();
		this.status = 1;
		this.eventDescription = calendarEventRequestEntity.getEventDescription();
		this.user = calendarEventRequestEntity.getUser();
	}


	public CalendarEvent(CalendarEventRequestEntity calendarEventRequestEntity,
			CalendarEventRequestEntity calendarEventRequestEntity2) {
		this.calendarEventId = calendarEventRequestEntity.getCalendarEventId();
		this.title = calendarEventRequestEntity.getTitle();
		this.startDate = calendarEventRequestEntity.getStartDate();
		this.endDate = calendarEventRequestEntity.getEndDate();
		this.startTime = calendarEventRequestEntity.getStartTime();
		this.endTime = calendarEventRequestEntity.getEndTime();
		this.eventParticipants = calendarEventRequestEntity.getEventParticipants();
		this.eventLocation = calendarEventRequestEntity.getEventLocation();
		this.meetingUrl = calendarEventRequestEntity.getMeetingUrl();
		this.status = calendarEventRequestEntity.getStatus();
		this.eventDescription = calendarEventRequestEntity.getEventDescription();
		this.user = calendarEventRequestEntity.getUser();
		this.eventRemainder=calendarEventRequestEntity.getEventRemainder();
	}


	public CalendarEvent(CalendarEvent calendarEventRequestEntity) {
		this.calendarEventId = calendarEventRequestEntity.getCalendarEventId();
		this.title = calendarEventRequestEntity.getTitle();
		this.startDate = calendarEventRequestEntity.getStartDate();
		this.endDate = calendarEventRequestEntity.getEndDate();
		this.startTime = calendarEventRequestEntity.getStartTime();
		this.endTime = calendarEventRequestEntity.getEndTime();
		this.eventParticipants = calendarEventRequestEntity.getEventParticipants();
		this.eventLocation = calendarEventRequestEntity.getEventLocation();
		this.meetingUrl = calendarEventRequestEntity.getMeetingUrl();
		this.status = calendarEventRequestEntity.getStatus();
		this.eventDescription = calendarEventRequestEntity.getEventDescription();
		this.eventRemainder=calendarEventRequestEntity.getEventRemainder();
	}


	public CalendarEvent(Integer calendarEventId2) {
		this.calendarEventId=calendarEventId2;
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


	public List<EventRemainder> getEventRemainder() {
		return eventRemainder;
	}


	public void setEventRemainder(List<EventRemainder> eventRemainder) {
		this.eventRemainder = eventRemainder;
	}
	
	
	

	
	

}
