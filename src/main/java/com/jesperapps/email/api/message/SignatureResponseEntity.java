package com.jesperapps.email.api.message;

import java.time.LocalDateTime;

import org.joda.time.DateTime;

import com.jesperapps.email.api.model.BaseResponse;
import com.jesperapps.email.api.model.Signature;
import com.jesperapps.email.api.model.User;

public class SignatureResponseEntity  extends BaseResponse
{
	private Integer id;
	private String name;
	private String body;
	private Integer status;
	private String associateemail; 
	private User user;
	private DateTime createdOn;
	private DateTime updatedOn;
	
	private Integer statusCode;
	private String description;
	private String message;
	private Integer errorCode;
	
	
	public SignatureResponseEntity() 
	{
		
	}
	public SignatureResponseEntity(Signature signature) 
	{
		super();
		this.id = signature.getId();
		this.name =  signature.getName();
		this.body = signature.getBody();
		this.status =  signature.getStatus();
		this.associateemail = signature.getAssociateemail();
		this.user = signature.getUser();
		LocalDateTime now = LocalDateTime.now();
		String date = now.toString();
		DateTime dt = new DateTime(date);		
		this.createdOn = dt;
		this.updatedOn = dt;
	}

	public SignatureResponseEntity(Signature signature , Signature signature1) 
	{
		super();
		this.id = signature.getId();
		this.name =  signature.getName();
		this.body = signature.getBody();
		this.status =  signature.getStatus();
		this.associateemail = signature.getAssociateemail();
		this.user = signature.getUser();
		LocalDateTime now = LocalDateTime.now();
		String date = now.toString();
		DateTime dt = new DateTime(date);
		System.out.println("dt"+dt);
		this.createdOn = dt;
		this.updatedOn = dt;
	}


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



	public Integer getStatusCode() {
		return statusCode;
	}



	public Integer setStatusCode(Integer statusCode) {
		return this.statusCode = statusCode;
	}



	public String getDescription() {
		return description;
	}



	public String setDescription(String description) {
		return this.description = description;
	}



	public String getMessage() {
		return message;
	}



	public String setMessage(String message) {
		return this.message = message;
	}



	public Integer getErrorCode() {
		return errorCode;
	}



	public Integer setErrorCode(Integer errorCode) {
		return this.errorCode = errorCode;
	}
	
}
