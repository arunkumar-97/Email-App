package com.jesperapps.email.api.service;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jesperapps.email.api.message.CalendarEventRequestEntity;
import com.jesperapps.email.api.model.CalendarEvent;
import com.jesperapps.email.api.model.EventRemainder;
import com.jesperapps.email.api.repository.CalendarEventRepository;

@Service
public class CalendarEventServiceImpl  implements CalendarEventService{

	@Autowired
	private CalendarEventRepository calendareventrepository;
	
	@Autowired
	private EventRemainderService eventRemainderService;
	
	

	@Override
	public List<CalendarEvent> findall() {
		
		return calendareventrepository.findAll();
	}


	@Override
	public Optional<CalendarEvent> findbyid(Integer calendareventid) {
		
		return calendareventrepository.findById(calendareventid);
	}


	@Override
	public CalendarEvent updateevent(CalendarEvent calendarEvent) {
		
		CalendarEvent calendarEvent1=new CalendarEvent(calendarEvent);
		CalendarEvent createdCalendarEvent=calendareventrepository.save(calendarEvent1);
		List<EventRemainder> remainders=calendarEvent1.getEventRemainder();
		System.out.println("Remainders :" +remainders);
		if(remainders.size() != 0) {
			CalendarEvent calendarEventDB=new CalendarEvent(createdCalendarEvent.getCalendarEventId());
			for(EventRemainder each:remainders) {
				EventRemainder item=new EventRemainder(each,calendarEventDB);
				
				eventRemainderService.save(item);
			}
		
	}
		return createdCalendarEvent;

	
	}


	@Override
	public CalendarEvent createevent(CalendarEventRequestEntity calendarEventRequestEntity) {
		CalendarEvent calendarEvent=new CalendarEvent(calendarEventRequestEntity);
		CalendarEvent createdCalendarEvent=calendareventrepository.save(calendarEvent);
		List<EventRemainder> remainders=calendarEventRequestEntity.getEventRemainder();
		if(remainders.size() != 0) {
			CalendarEvent calendarEventDB=new CalendarEvent(createdCalendarEvent.getCalendarEventId());
			for(EventRemainder each:remainders) {
				String startDate=calendarEventRequestEntity.getStartDate();
				String startTime=calendarEventRequestEntity.getStartTime();
				String[] date1 = startDate.split("-");
				String[] time1 = startTime.split(":");
//				String[] time2 = time1[1].split(" ");
				if(each.isEventRemainderBefore() == true) {
					Calendar beginTime = Calendar.getInstance();
					
					beginTime.set(Integer.parseInt(date1[0]), Integer.parseInt(date1[1]), Integer.parseInt(date1[2]), Integer.parseInt(time1[0]), Integer.parseInt(time1[1]));
					System.out.println("date :" + date1[2]);
					System.out.println("month :" + date1[1]);
					System.out.println("year :" + date1[0]);
					
					System.out.println("BeginTime " + beginTime.getTime());
					long startMills = beginTime.getTimeInMillis();
					
				}
				EventRemainder item=new EventRemainder(each,calendarEventDB);
				
				eventRemainderService.save(item);
			}
		
	}
		return createdCalendarEvent;

	}
}
