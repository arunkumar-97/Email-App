package com.jesperapps.email.api.message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import com.jesperapps.email.api.model.BaseResponse;
import com.jesperapps.email.api.model.Email;
import com.jesperapps.email.api.model.MultipleAttachment;
import com.jesperapps.email.api.model.User;

public class EmailResponseEntity extends BaseResponse {

	private String id;
	private String from;
	private String[] to;
	private String[] cc;
	private String subject;
	private String body;
	private String status;
	private String emailType;
	private String emailStatus;
	private String checked;
	private String emailPriority;
	private String emailPriorityExtra;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String[] getTo() {
		return to;
	}
	public void setTo(String[] to) {
		this.to = to;
	}
	public String[] getCc() {
		return cc;
	}
	public void setCc(String[] cc) {
		this.cc = cc;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getEmailType() {
		return emailType;
	}
	public void setEmailType(String emailType) {
		this.emailType = emailType;
	}
	public String getEmailStatus() {
		return emailStatus;
	}
	public void setEmailStatus(String emailStatus) {
		this.emailStatus = emailStatus;
	}
	public String getChecked() {
		return checked;
	}
	public void setChecked(String checked) {
		this.checked = checked;
	}
	public String getEmailPriority() {
		return emailPriority;
	}
	public void setEmailPriority(String emailPriority) {
		this.emailPriority = emailPriority;
	}
	public String getEmailPriorityExtra() {
		return emailPriorityExtra;
	}
	public void setEmailPriorityExtra(String emailPriorityExtra) {
		this.emailPriorityExtra = emailPriorityExtra;
	}
	

}
