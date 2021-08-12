package com.jesperapps.email.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jesperapps.email.api.message.ListTypesResponseEntity;
import com.jesperapps.email.api.model.ListTypes;
import com.jesperapps.email.api.repository.ListTypesRepository;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ListTypesController {

	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ListTypesRepository listTypesRepository;

	ListTypesResponseEntity res = new ListTypesResponseEntity();

	@GetMapping("/list-types")
	public ResponseEntity listListTypes() {
		List<ListTypesResponseEntity> resEntity = new ArrayList<ListTypesResponseEntity>();
		List<ListTypes> listTypes = listTypesRepository.findAll();
		if (listTypes.isEmpty()) {

		} else {
			for (ListTypes l : listTypes) {
				ListTypesResponseEntity managementResponseEntity = new ListTypesResponseEntity(l);
				resEntity.add(managementResponseEntity);
			}
		}
		if (resEntity.isEmpty()) {
			ObjectNode jsonObject = objectMapper.createObjectNode();
			jsonObject.put("statusCode", res.setStatusCode(204));
			jsonObject.put("message", res.setDescription("no data"));
			return new ResponseEntity(jsonObject, HttpStatus.BAD_REQUEST);
		} else {
			return new ResponseEntity<List<ListTypesResponseEntity>>(resEntity, HttpStatus.OK);
		}
	}

}
