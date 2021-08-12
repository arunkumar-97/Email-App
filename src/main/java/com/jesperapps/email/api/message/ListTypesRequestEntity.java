package com.jesperapps.email.api.message;

import java.util.List;

import com.jesperapps.email.api.model.ListTypeValues;

public class ListTypesRequestEntity {
	
	private Integer listTypeId;
	private String listTypeName;
	private String description;

	private Integer status;

	private List<ListTypeValues> listTypeValues;

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

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public List<ListTypeValues> getListTypeValues() {
		return listTypeValues;
	}

	public void setListTypeValues(List<ListTypeValues> listTypeValues) {
		this.listTypeValues = listTypeValues;
	}
	
	

}
