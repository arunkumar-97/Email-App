package com.jesperapps.email.api.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jesperapps.email.api.model.EventRemainder;
import com.jesperapps.email.api.repository.EventRemainderRepository;

@Service
public class EventRemainderServicelmpl implements EventRemainderService{

	
	@Autowired
	private EventRemainderRepository eventRemainderRepository;

	@Override
	public EventRemainder createEventRemainder(EventRemainder eventRemainder) {
		// TODO Auto-generated method stub
		return eventRemainderRepository.save(eventRemainder);
	}

	@Override
	public List<EventRemainder> findAll() {
		// TODO Auto-generated method stub
		return eventRemainderRepository.findAll();
	}

	@Override
	public EventRemainder save(EventRemainder item) {
		// TODO Auto-generated method stub
		return eventRemainderRepository.save(item);
	}

	@Override
	public EventRemainder findById(Integer eventRemainderId) {
		// TODO Auto-generated method stub
		return eventRemainderRepository.findByEventRemainderId(eventRemainderId);
	}

	@Override
	public EventRemainder saveEventRemainder(EventRemainder eventRemainder) {
		// TODO Auto-generated method stub
		return eventRemainderRepository.save(eventRemainder);
	}
}
