package com.jesperapps.email.api.message;

import java.util.List;

import com.jesperapps.email.api.model.BaseResponse;
import com.jesperapps.email.api.model.ListTypes;

public class ListTypesResponseEntity extends BaseResponse{
	private Integer listTypeId;
	private String listTypeName;
	private String description;

	private Integer status;


	public ListTypesResponseEntity() {
		// TODO Auto-generated constructor stub
	}
	
	public ListTypesResponseEntity(ListTypes l) {
		super();
		this.listTypeId = l.getListTypeId();
		this.listTypeName = l.getListTypeName();
		this.description = l.getDescription();
		this.status = l.getStatus();
	}



	public Integer getListTypeId() {
		return listTypeId;
	}

	public void setListTypeId(Integer listTypeId) {
		this.listTypeId = listTypeId;
	}

	public String getListTypeName() {
		return listTypeName;
	}

	public void setListTypeName(String listTypeName) {
		this.listTypeName = listTypeName;
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
