package com.jesperapps.email.api.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jesperapps.email.api.message.CalendarEventRequestEntity;
import com.jesperapps.email.api.message.CalendarEventResponseEntity;
import com.jesperapps.email.api.message.SignatureResponseEntity;
import com.jesperapps.email.api.model.CalendarEvent;
import com.jesperapps.email.api.model.User;
import com.jesperapps.email.api.service.CalendarEventService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class CalendarEventController {

	
	@Autowired
	  private CalendarEventService calendarEventService;
	
	@Autowired
	private  ObjectMapper objectMapper;
	
	
	private CalendarEventResponseEntity responseEntity  = new CalendarEventResponseEntity();
	
	
	
	
	@SuppressWarnings("unused")
	@PostMapping("/calendarevent")
	public ResponseEntity<CalendarEventResponseEntity> createcalendarevent (@RequestBody  CalendarEventRequestEntity calendarEventRequestEntity)
	{
		    
		     
		     CalendarEvent calEvent =    calendarEventService.createevent(calendarEventRequestEntity);
		     
		       if(calEvent != null) {
		    	   
		    	   ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("statusCode", responseEntity.setStatusCode(200));
					jsonObject.put("message", responseEntity.setMessage("Calendar Event  Created Successfully"));
					return new ResponseEntity(jsonObject, HttpStatus.OK);
		       }else {
		    	   ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", responseEntity.setErrorCode(409));
					jsonObject.put("message", responseEntity.setMessage("Unable to  Create  Calendar Event"));
					return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		       }
		          
		
		
	}
	
	
	@SuppressWarnings("unused")
	@GetMapping("/calendarevent")
	public ResponseEntity<List<CalendarEventResponseEntity>> calendareventlist()
	{
		
		     List<CalendarEventResponseEntity> resEntity = new ArrayList<CalendarEventResponseEntity>();     
		     List<CalendarEvent> calEvent =    calendarEventService.findall();
		     
		     
		       if(!calEvent.isEmpty()) {
		    	   
		    	          for(CalendarEvent calendarEvent : calEvent) {
		    	        	  
		    	        	       if(calendarEvent.getStatus() != 4)
		    	        	       {
		    	        	    	    CalendarEventResponseEntity calendarEventResponseEntity  = new CalendarEventResponseEntity(calendarEvent);
		    	        	    	    
		    	        	    	      User user = new User(calendarEventResponseEntity.getUser() ,calendarEventResponseEntity.getUser() , calendarEventResponseEntity.getUser());
		    	        	    	      calendarEventResponseEntity.setUser(user);
		    	        	    	    resEntity.add(calendarEventResponseEntity);
		    	        	       }
		    	        	  
		    	          }
		    	          
		    	          
		    	          if(!resEntity.isEmpty())
		    	          {
		    	        	  return new ResponseEntity<List<CalendarEventResponseEntity>>(resEntity, HttpStatus.OK);
		    	        	   
		    	          }else {
		    	        	  ObjectNode jsonObject = objectMapper.createObjectNode();
		  					jsonObject.put("errorCode", responseEntity.setErrorCode(404));
		  					jsonObject.put("message", responseEntity.setMessage("No data found"));
		  					return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		    	          }
		    	          
		    	   
		    	  
		       }else {
		    	   ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", responseEntity.setErrorCode(404));
					jsonObject.put("message", responseEntity.setMessage("No data found"));
					return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		       }
		          
		
		
	}
	
	
	@SuppressWarnings("unused")
	@GetMapping("/calendarevent/{calendareventid}")
	public ResponseEntity<CalendarEventResponseEntity> calendareventbyid(@PathVariable("calendareventid") Integer  calendareventid)
	{
		
		     Optional<CalendarEvent> calEvent =  calendarEventService.findbyid(calendareventid);
		       if(calEvent.isPresent()) {
        	    	      CalendarEventResponseEntity calendarEventResponseEntity  = new CalendarEventResponseEntity(calEvent.get());		    	        	    	    
        	    	      User user = new User(calendarEventResponseEntity.getUser() ,calendarEventResponseEntity.getUser() , calendarEventResponseEntity.getUser());
        	    	      calendarEventResponseEntity.setUser(user);        	    	    
		    	        	       
		    	          if(calendarEventResponseEntity != null)
		    	          {
		    	        	  return new ResponseEntity<CalendarEventResponseEntity>(calendarEventResponseEntity, HttpStatus.OK);
		    	        	   
		    	          }else {
		    	        	  ObjectNode jsonObject = objectMapper.createObjectNode();
		  					jsonObject.put("errorCode", responseEntity.setErrorCode(404));
		  					jsonObject.put("message", responseEntity.setMessage("No data found"));
		  					return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		    	          }   		    	  
		       }else {
		    	   ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", responseEntity.setErrorCode(404));
					jsonObject.put("message", responseEntity.setMessage("No data found"));
					return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		       }

	}
	
	
	@SuppressWarnings("unused")
	@PutMapping("/calendarevent/{calendareventid}")
	public ResponseEntity<CalendarEventResponseEntity> calendareventbyid(@PathVariable("calendareventid") Integer  calendareventid , @RequestBody CalendarEventRequestEntity calendarEventRequestEntity)
	{
		
		     Optional<CalendarEvent> calEvent =  calendarEventService.findbyid(calendareventid);
		       if(calEvent.isPresent()) {
        	    	       	    	    
		    	   
		    	   CalendarEvent calendarEvent = new CalendarEvent(calendarEventRequestEntity ,calendarEventRequestEntity );
		    	   CalendarEvent    calendarevents = calendarEventService.updateevent(calendarEvent);
		    	         
		    	   if(calendarevents != null)
		    	   {
		    		   ObjectNode jsonObject = objectMapper.createObjectNode();
		   			jsonObject.put("statusCode", responseEntity.SUCCESS);
		   			jsonObject.put("description", responseEntity.setMessage("CalendarEvent  Updated  Successfully"));
		   			return new ResponseEntity(jsonObject, HttpStatus.OK);
		    		    
		    	   }
		    	   else {
		    		   
		    		ObjectNode jsonObject = objectMapper.createObjectNode();
		   			jsonObject.put("errorCode", responseEntity.FAILURE);
		   			jsonObject.put("message", responseEntity.setMessage("Unable to Update CalendarEvent"));
		   			return new ResponseEntity(jsonObject, HttpStatus.CONFLICT);
		    	   
		    	   }
		    	        
		    	           	  
		       }else {
		    	   	
		    	   	ObjectNode jsonObject = objectMapper.createObjectNode();
					jsonObject.put("errorCode", responseEntity.setErrorCode(404));
					jsonObject.put("message", responseEntity.setMessage("Calendarevent with id=" + calendareventid + " not found"));
					return new ResponseEntity(jsonObject, HttpStatus.NOT_FOUND);
		       }
	}
}
