package com.jesperapps.email.api.message;

import org.joda.time.DateTime;

import com.jesperapps.email.api.model.User;

public class SignatureRequestEntity {

	private Integer id;
	private String name;
	private String body;
	private Integer status;
	private String associateemail; 
	private User user;
	private DateTime createdOn;
	private DateTime updatedOn;
	

	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getAssociateemail() {
		return associateemail;
	}
	public void setAssociateemail(String associateemail) {
		this.associateemail = associateemail;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public DateTime getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(DateTime createdOn) {
		this.createdOn = createdOn;
	}
	public DateTime getUpdatedOn() {
		return updatedOn;
	}
	public void setUpdatedOn(DateTime updatedOn) {
		this.updatedOn = updatedOn;
	}
}
