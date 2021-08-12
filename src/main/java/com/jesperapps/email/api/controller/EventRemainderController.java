package com.jesperapps.email.api.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.jesperapps.email.api.message.EventRemainderRequestEntity;
import com.jesperapps.email.api.message.EventRemainderResponseEntity;
import com.jesperapps.email.api.model.CalendarEvent;
import com.jesperapps.email.api.model.EventRemainder;
import com.jesperapps.email.api.service.EventRemainderService;

@RestController
public class EventRemainderController {

	@Autowired
	private EventRemainderService  eventRemainderService;
	

	@PostMapping("/eventRemainder")
	public ResponseEntity addEventRemainder(@RequestBody EventRemainderRequestEntity eventRemaindeRequestEntity) {
		
		EventRemainder eventRemainder = new EventRemainder(eventRemaindeRequestEntity);
		EventRemainder createdEventRemainder = eventRemainderService.createEventRemainder(eventRemainder);
			if (createdEventRemainder != null) {
				EventRemainderResponseEntity eventRemainderResponseEntity = new EventRemainderResponseEntity();
				eventRemainderResponseEntity.setStatusCode(200);
				eventRemainderResponseEntity.setDescription("Event Remainder Created Sucessfully for the Calendar Event");
				return new ResponseEntity(eventRemainderResponseEntity, HttpStatus.OK);
			} else {
				EventRemainderResponseEntity eventRemainderResponseEntity = new EventRemainderResponseEntity();
				eventRemainderResponseEntity.setStatusCode(409);
				eventRemainderResponseEntity.setDescription(" Unable to Create Event Remainder for the Calendar Event");
				return new ResponseEntity(eventRemainderResponseEntity, HttpStatus.CONFLICT);
			}
		
		
	}
	
	@PutMapping("/eventRemainder/{eventRemainderId}")
	public EventRemainderResponseEntity updateeventRemainder(@PathVariable Integer eventRemainderId,@RequestBody EventRemainderRequestEntity eventRemaindeRequestEntity)
	{
		EventRemainderResponseEntity response=new EventRemainderResponseEntity(409, "no such id found");
		
		if(eventRemainderId != null)
		{
	
		
			EventRemainder eventRemainderFromDB=eventRemainderService.findById(eventRemainderId);
		 if(eventRemainderFromDB!=null) {
			 EventRemainder eventRemainder=new EventRemainder(eventRemaindeRequestEntity);
			 
			
			 eventRemainderService.saveEventRemainder(eventRemainder);
			response.setStatusCode(200);
			response.setDescription("EventRemainder successfully updated");
			
			
		 }else {
			 response.setStatusCode(404);
			 response.setDescription("EventRemainder with the Id:" + eventRemainderId + " not Found");
			 return response;
		 }
		
		}
		return response;
	}
	
	@GetMapping("/eventRemainder")
	public List<EventRemainderResponseEntity> listAllEventRemainders() {
		List<EventRemainderResponseEntity> res = new ArrayList<>();

		eventRemainderService.findAll().forEach(eventRemainder -> {
			CalendarEvent ce=eventRemainder.getCalendarEvent();
			CalendarEvent c=new CalendarEvent(ce);
			eventRemainder.setCalendarEvent(c);
			res.add(new EventRemainderResponseEntity(eventRemainder));
			
			
		});
		return res;
	}
	
	@GetMapping("/eventRemainder/{eventRemainderId}")
	public ResponseEntity viewEventRemainderById(@PathVariable Integer eventRemainderId) {
		EventRemainder eventRemainder = eventRemainderService.findById(eventRemainderId);
		 
		if (eventRemainder != null) {
			EventRemainderResponseEntity eventRemainderResponse=new EventRemainderResponseEntity(eventRemainder);
		
			
			return new ResponseEntity(eventRemainderResponse, HttpStatus.OK);
		}else{
			EventRemainderResponseEntity eventRemainderResponseEntity = new EventRemainderResponseEntity();
			eventRemainderResponseEntity.setStatusCode(409);
			eventRemainderResponseEntity.setDescription("EventRemainder with ID not found");
			return new ResponseEntity(eventRemainderResponseEntity, HttpStatus.CONFLICT);
			}
		

	}
	
	
}
