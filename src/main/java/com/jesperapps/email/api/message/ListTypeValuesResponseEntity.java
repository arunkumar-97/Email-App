package com.jesperapps.email.api.message;

import com.jesperapps.email.api.model.BaseResponse;
import com.jesperapps.email.api.model.ListTypeValues;

public class ListTypeValuesResponseEntity extends BaseResponse{
	
	private Integer listTypeValueId;
	private String listTypeValueName;
	private String description;

	private Integer status;
	

	public ListTypeValuesResponseEntity() {
		// TODO Auto-generated constructor stub
	}
	
	

	public ListTypeValuesResponseEntity(ListTypeValues l) {
		super();
		this.listTypeValueId = l.getListTypeValueId();
		this.listTypeValueName = l.getListTypeValueName();
		this.description = l.getDescription();
		this.status = l.getStatus();
	}



	public Integer getListTypeValueId() {
		return listTypeValueId;
	}

	public void setListTypeValueId(Integer listTypeValueId) {
		this.listTypeValueId = listTypeValueId;
	}

	public String getListTypeValueName() {
		return listTypeValueName;
	}

	public void setListTypeValueName(String listTypeValueName) {
		this.listTypeValueName = listTypeValueName;
	}

	public String getDescription() {
		return description;
	}

	public String setDescription(String description) {
		return this.description = description;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
}
