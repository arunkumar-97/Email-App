package com.jesperapps.email.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jesperapps.email.api.model.CalendarEvent;

@Repository
public  interface CalendarEventRepository  extends JpaRepository<CalendarEvent, Integer>{

}
