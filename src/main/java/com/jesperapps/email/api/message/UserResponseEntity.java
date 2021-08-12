package com.jesperapps.email.api.message;

import java.time.LocalDateTime;
import java.util.Optional;

import com.jesperapps.email.api.model.Attachment;
import com.jesperapps.email.api.model.BaseResponse;
import com.jesperapps.email.api.model.Email;
import com.jesperapps.email.api.model.EmailConfiguration;
import com.jesperapps.email.api.model.ListTypeValues;
import com.jesperapps.email.api.model.User;

public class UserResponseEntity extends BaseResponse {

	private Integer id;
	private String firstName;
	private String lastName;
	private String userName;
	private String password;
	private Integer status;
	private EmailConfiguration emailConfiguration;
	private Email email;
	private Integer statusCode;
	private String description;
	private String message;
	private Integer errorCode;
	private String contactNumber;
	private String token;
	private String extraEmail; 
	private LocalDateTime createDateTime;
	private LocalDateTime updateDateTime;
	protected String createdBy;
	protected String lastUpdatedBy;
	private String keyValue;
	private ListTypeValues listTypeValues;
	private Attachment attachment;
	
	public UserResponseEntity() {
		
	}

	public UserResponseEntity(User users, String password2) {
		this.id = users.getId(); 
		this.firstName = users.getFirstName();
		this.lastName = users.getLastName();
		this.userName = users.getUserName();
		this.listTypeValues = users.getListTypeValues();
	}

	public UserResponseEntity(User user) {
		super();
		this.id = user.getId();
		this.userName = user.getUserName();
		this.status = user.getStatus();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.contactNumber = user.getContactNumber();
		this.token = user.getToken();
		this.createDateTime = user.getCreateDateTime();
		this.updateDateTime = user.getUpdateDateTime();
		if (user.getAttachment() == null) {

		} else {
			this.attachment = user.getAttachment();
		}
		this.listTypeValues = user.getListTypeValues();
	}

	public UserResponseEntity(User user, User user2) {
		this.id = user.getId();
		this.userName = user.getUserName();
		this.status = user.getStatus();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.contactNumber = user.getContactNumber();
		this.token = user.getToken();
		this.createDateTime = user.getCreateDateTime();
		this.updateDateTime = user.getUpdateDateTime();
		if (user.getAttachment() == null) {

		} else {
			this.attachment = user.getAttachment();
		}
		this.listTypeValues = user.getListTypeValues();
	}

	public UserResponseEntity(User user, Integer id2, String password2) {
		this.id = user.getId();
		this.userName = user.getUserName();
		this.password = password2;
		this.extraEmail = user.getExtraEmail();
		this.status = user.getStatus();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.contactNumber = user.getContactNumber();
		this.listTypeValues = user.getListTypeValues();
	}

	public UserResponseEntity(User user, Long id2) {
		this.id = user.getId();
		this.userName = user.getUserName();
		this.status = user.getStatus();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.contactNumber = user.getContactNumber();
		this.token = user.getToken();
	}

	public UserResponseEntity(User user, Integer id2) {
		this.id = user.getId();
		this.userName = user.getUserName();
		this.status = user.getStatus();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.contactNumber = user.getContactNumber();
	}

	public UserResponseEntity(User user, Optional<User> users) {
		this.id = user.getId(); 
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.userName = user.getUserName();
		this.listTypeValues = user.getListTypeValues();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public EmailConfiguration getEmailConfiguration() {
		return emailConfiguration;
	}

	public void setEmailConfiguration(EmailConfiguration emailConfiguration) {
		this.emailConfiguration = emailConfiguration;
	}

	public Email getEmail() {
		return email;
	}

	public void setEmail(Email email) {
		this.email = email;
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

	public String getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}

	public ListTypeValues getListTypeValues() {
		return listTypeValues;
	}

	public void setListTypeValues(ListTypeValues listTypeValues) {
		this.listTypeValues = listTypeValues;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getExtraEmail() {
		return extraEmail;
	}

	public void setExtraEmail(String extraEmail) {
		this.extraEmail = extraEmail;
	}

	public LocalDateTime getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(LocalDateTime createDateTime) {
		this.createDateTime = createDateTime;
	}

	public LocalDateTime getUpdateDateTime() {
		return updateDateTime;
	}

	public void setUpdateDateTime(LocalDateTime updateDateTime) {
		this.updateDateTime = updateDateTime;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}



}
