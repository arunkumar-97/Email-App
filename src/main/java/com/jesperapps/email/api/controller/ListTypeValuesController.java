package com.jesperapps.email.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jesperapps.email.api.message.ListTypeValuesResponseEntity;
import com.jesperapps.email.api.model.ListTypeValues;
import com.jesperapps.email.api.model.ListTypes;
import com.jesperapps.email.api.repository.ListTypeValuesRepository;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ListTypeValuesController {
	
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ListTypeValuesRepository listTypeValuesRepository;

	ListTypeValuesResponseEntity res = new ListTypeValuesResponseEntity();

	@GetMapping("/list-type-values/{listTypeId}")
	public ResponseEntity listListTypeValues(@PathVariable("listTypeId") Integer listTypesId ) {
		ListTypes listTypes = new ListTypes(listTypesId);
		List<ListTypeValuesResponseEntity> resEntity = new ArrayList<ListTypeValuesResponseEntity>();
		List<ListTypeValues> listTypeValues = listTypeValuesRepository.findAllByListTypes(listTypes);
		if (listTypeValues.isEmpty()) {

		} else {
			for (ListTypeValues l : listTypeValues) {
				if(l.getStatus() == null || l.getStatus().equals(1)
						|| l.getStatus().equals(2)
						|| l.getStatus().equals(3)) {
				ListTypeValuesResponseEntity listTypeValuesResponseEntity = new ListTypeValuesResponseEntity(l);
				resEntity.add(listTypeValuesResponseEntity);
				}
			}
		}
		if (resEntity.isEmpty()) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.setStatusCode(204));
			jsonObject.put("message", res.setDescription("no data"));
			return new ResponseEntity(jsonObject, HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity<List<ListTypeValuesResponseEntity>>(resEntity, HttpStatus.OK);
		}
	}
	
	@GetMapping("/list-type-value/{listTypeValueId}")
	public ResponseEntity listTypeValues(@PathVariable("listTypeValueId") Integer listTypeValueId ) {
		ListTypeValues listTypeValues = new ListTypeValues(listTypeValueId);
		List<ListTypeValuesResponseEntity> resEntity = new ArrayList<ListTypeValuesResponseEntity>();
		List<ListTypeValues> listTypeValuesList = listTypeValuesRepository.findAllByListTypeValueId(listTypeValueId);
		if (listTypeValuesList.isEmpty()) {

		} else {
			for (ListTypeValues l : listTypeValuesList) {
				if(l.getStatus() == null || l.getStatus().equals(1)
						|| l.getStatus().equals(2)
						|| l.getStatus().equals(3)) {
				ListTypeValuesResponseEntity listTypeValuesResponseEntity = new ListTypeValuesResponseEntity(l);
				resEntity.add(listTypeValuesResponseEntity);
				}
			}
		}
		if (resEntity.isEmpty()) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.setStatusCode(204));
			jsonObject.put("message", res.setDescription("no data"));
			return new ResponseEntity(jsonObject, HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity<List<ListTypeValuesResponseEntity>>(resEntity, HttpStatus.OK);
		}
	}


}
