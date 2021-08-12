package com.jesperapps.email.api.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.joda.time.DateTime;
@Entity
public class Email implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	@Transient
	private String[] to, cc, bcc, replyTo;
	@Transient
	private String from, subject, body, emailType, emailStatus, checked, emailPriority, emailPriorityExtra;
	@Transient
	private List<MultipleAttachment> multipleAttachment;
	@Transient
	private DateTime createdOn;
	@Transient
	private DateTime updatedOn;
	@Transient
	private boolean repliedFlag,forwardedFlag;


	@ManyToOne
	@JoinColumn(name = "id", nullable = false, insertable=false, updatable=false )
	private User user;

	public Email() {
		// TODO Auto-generated constructor stub
	}

	// inbox listing
	public Email(String from2, String[] toArray, String subject2, String result, DateTime dtRec, String[] ccArray,
			String[] bccArray, Folder folder, Long uid, Flags flags, List<MultipleAttachment> multipleAttachment2) {
		this.createdOn = dtRec;
		this.id = uid;
		this.from = from2;
		this.to = toArray;
		this.cc = ccArray;
		this.bcc = bccArray;
		this.subject = subject2;
		this.body = result;
		if (folder.getName().equals("INBOX")) {
			this.emailType = "Received";
		}
		if (flags.contains(Flag.SEEN)) {
			this.emailStatus = "Read";
			this.checked = "true";
		} else if (flags.contains(Flag.ANSWERED) || flags.contains(Flag.DELETED) || flags.contains(Flag.DRAFT)
				|| flags.contains(Flag.RECENT) || flags.contains(Flag.FLAGGED) || flags.contains(Flag.USER)) {
			this.emailStatus = "UnRead";
			this.checked = "false";
		} else {
			this.emailStatus = "UnRead";
			this.checked = "false";
		}

		if (flags.contains(Flag.FLAGGED)) {
			this.emailPriority = "Important";
			this.emailPriorityExtra = "Important";
		} else if (flags.contains(Flag.ANSWERED) || flags.contains(Flag.DELETED) || flags.contains(Flag.DRAFT)
				|| flags.contains(Flag.RECENT) || flags.contains(Flag.SEEN) || flags.contains(Flag.USER)) {
			this.emailPriority = "UnImportant";
			this.emailPriorityExtra = "UnImportant";
		} else {
			this.emailPriority = "UnImportant";
			this.emailPriorityExtra = "UnImportant";
		}

		this.multipleAttachment = multipleAttachment2;
		this.forwardedFlag = false;
		this.repliedFlag = false;
	}

	// sent listing
	public Email(String from2, String[] toArray, String subject2, String result, DateTime dtRec, String[] ccArray,
			String[] bccArray, Folder folder, long uid, Flags flags, List<MultipleAttachment> multipleAttachment2,
			long uid2) {
		this.createdOn = dtRec;
		this.id = uid;
		this.from = from2;
		this.to = toArray;
		this.cc = ccArray;
		this.bcc = bccArray;
		this.subject = subject2;
		this.body = result;
		if (folder.getName().equals("Sent")) {
			this.emailType = "Sent";
		}
		if (flags.contains(Flag.SEEN)) {
			this.emailStatus = "Read";
			this.checked = "true";
		} else if (flags.contains(Flag.ANSWERED) || flags.contains(Flag.DELETED) || flags.contains(Flag.DRAFT)
				|| flags.contains(Flag.RECENT) || flags.contains(Flag.FLAGGED) || flags.contains(Flag.USER)) {
			this.emailStatus = "UnRead";
			this.checked = "false";
		} else {
			this.emailStatus = "UnRead";
			this.checked = "false";
		}

		if (flags.contains(Flag.FLAGGED)) {
			this.emailPriority = "Important";
			this.emailPriorityExtra = "Important";
		} else if (flags.contains(Flag.ANSWERED) || flags.contains(Flag.DELETED) || flags.contains(Flag.DRAFT)
				|| flags.contains(Flag.RECENT) || flags.contains(Flag.SEEN) || flags.contains(Flag.USER)) {
			this.emailPriority = "UnImportant";
			this.emailPriorityExtra = "UnImportant";
		} else {
			this.emailPriority = "UnImportant";
			this.emailPriorityExtra = "UnImportant";
		}

		this.multipleAttachment = multipleAttachment2;
	}

	// draft listing
	public Email(String from2, String[] toArray, String subject2, String result, DateTime dtRec, String[] ccArray,
			String[] bccArray, Folder folder, long uid, Flags flags, long uid2,
			List<MultipleAttachment> multipleAttachment2) {
		this.createdOn = dtRec;
		this.id = uid;
		this.from = from2;
		this.to = toArray;
		this.cc = ccArray;
		this.bcc = bccArray;
		this.subject = subject2;
		this.body = result;
		if (folder.getName().equals("Drafts")) {
			this.emailType = "Draft";
		}
		if (flags.contains(Flag.SEEN)) {
			this.emailStatus = "Read";
			this.checked = "true";
		} else if (flags.contains(Flag.ANSWERED) || flags.contains(Flag.DELETED) || flags.contains(Flag.DRAFT)
				|| flags.contains(Flag.RECENT) || flags.contains(Flag.FLAGGED) || flags.contains(Flag.USER)) {
			this.emailStatus = "UnRead";
			this.checked = "false";
		} else {
			this.emailStatus = "UnRead";
			this.checked = "false";
		}

		if (flags.contains(Flag.FLAGGED)) {
			this.emailPriority = "Important";
			this.emailPriorityExtra = "Important";
		} else if (flags.contains(Flag.ANSWERED) || flags.contains(Flag.DELETED) || flags.contains(Flag.DRAFT)
				|| flags.contains(Flag.RECENT) || flags.contains(Flag.SEEN) || flags.contains(Flag.USER)) {
			this.emailPriority = "UnImportant";
			this.emailPriorityExtra = "UnImportant";
		} else {
			this.emailPriority = "UnImportant";
			this.emailPriorityExtra = "UnImportant";
		}

		this.multipleAttachment = multipleAttachment2;
	}

	// trash listing
	public Email(long uid, String from2, String[] toArray, String subject2, String result, DateTime dtRec,
			String[] ccArray, String[] bccArray, Folder folder, long uid2, Flags flags,
			List<MultipleAttachment> multipleAttachment2) {
		this.createdOn = dtRec;
		this.id = uid;
		this.from = from2;
		this.to = toArray;
		this.cc = ccArray;
		this.bcc = bccArray;
		this.subject = subject2;
		this.body = result;
		if (folder.getName().equals("Trash")) {
			this.emailType = "Deleted";
		}
		if (flags.contains(Flag.SEEN)) {
			this.emailStatus = "Read";
			this.checked = "true";
		} else if (flags.contains(Flag.ANSWERED) || flags.contains(Flag.DELETED) || flags.contains(Flag.DRAFT)
				|| flags.contains(Flag.RECENT) || flags.contains(Flag.FLAGGED) || flags.contains(Flag.USER)) {
			this.emailStatus = "UnRead";
			this.checked = "false";
		} else {
			this.emailStatus = "UnRead";
			this.checked = "false";
		}

		if (flags.contains(Flag.FLAGGED)) {
			this.emailPriority = "Important";
			this.emailPriorityExtra = "Important";
		} else if (flags.contains(Flag.ANSWERED) || flags.contains(Flag.DELETED) || flags.contains(Flag.DRAFT)
				|| flags.contains(Flag.RECENT) || flags.contains(Flag.SEEN) || flags.contains(Flag.USER)) {
			this.emailPriority = "UnImportant";
			this.emailPriorityExtra = "UnImportant";
		} else {
			this.emailPriority = "UnImportant";
			this.emailPriorityExtra = "UnImportant";
		}

		this.multipleAttachment = multipleAttachment2;
	}

	// get by id//id based data
	public Email(String from2, String[] toArray, String subject2, String result, DateTime dtRec, String[] ccArray,
			String[] bccArray, Folder folder, long uid, long uid2, Flags flags,
			List<MultipleAttachment> multipleAttachment2) {
		this.createdOn = dtRec;
		this.id = uid;
		this.from = from2;
		this.to = toArray;
		this.cc = ccArray;
		this.bcc = bccArray;
		this.subject = subject2;
		this.body = result;
		if (folder.getName().equals("INBOX")) {
			this.emailType = "Received";
		} else if (folder.getName().equals("Sent")) {
			this.emailType = "Sent";
		} else if (folder.getName().equals("Drafts")) {
			this.emailType = "Draft";
		} else if (folder.getName().equals("Trash")) {
			this.emailType = "Deleted";
		} else {
			this.emailType = folder.getName();
		}
		if (flags.contains(Flag.SEEN)) {
			this.emailStatus = "Read";
			this.checked = "true";
		} else if (flags.contains(Flag.ANSWERED) || flags.contains(Flag.DELETED) || flags.contains(Flag.DRAFT)
				|| flags.contains(Flag.RECENT) || flags.contains(Flag.FLAGGED) || flags.contains(Flag.USER)) {
			this.emailStatus = "UnRead";
			this.checked = "false";
		} else {
			this.emailStatus = "UnRead";
			this.checked = "false";
		}

		if (flags.contains(Flag.FLAGGED)) {
			this.emailPriority = "Important";
			this.emailPriorityExtra = "Important";
		} else if (flags.contains(Flag.ANSWERED) || flags.contains(Flag.DELETED) || flags.contains(Flag.DRAFT)
				|| flags.contains(Flag.RECENT) || flags.contains(Flag.SEEN) || flags.contains(Flag.USER)) {
			this.emailPriority = "UnImportant";
			this.emailPriorityExtra = "UnImportant";
		} else {
			this.emailPriority = "UnImportant";
			this.emailPriorityExtra = "UnImportant";
		}

		this.multipleAttachment = multipleAttachment2;
	}

	public Email(long uid) {
		this.id = uid;
	}

	public Email(String from2, String[] toArray, String subject2, String result, String[] ccArray, String[] bccArray,
			long uid) {
		this.id = uid;
		this.from = from2;
		this.to = toArray;
		this.cc = ccArray;
		this.bcc = bccArray;
		this.subject = subject2;
		this.body = result;
	}

	// unseen listing
	public Email(String from2, String[] toArray, String subject2, long uid, String result, DateTime dtRec,
			String[] ccArray, String[] bccArray, Folder folder, long uid2, Flags flags,
			List<MultipleAttachment> multipleAttachment2) {
		this.createdOn = dtRec;
		this.id = uid;
		this.from = from2;
		this.to = toArray;
		this.cc = ccArray;
		this.bcc = bccArray;
		this.subject = subject2;
		this.body = result;
		if (folder.getName().equals("INBOX")) {
			this.emailType = "Received";
		} else if (folder.getName().equals("Sent")) {
			this.emailType = "Sent";
		} else if (folder.getName().equals("Drafts")) {
			this.emailType = "Draft";
		} else if (folder.getName().equals("Trash")) {
			this.emailType = "Deleted";
		} else {
			this.emailType = folder.getName();
		}
		if (flags.contains(Flag.SEEN)) {
			this.emailStatus = "Read";
			this.checked = "true";
		} else if (flags.contains(Flag.ANSWERED) || flags.contains(Flag.DELETED) || flags.contains(Flag.DRAFT)
				|| flags.contains(Flag.RECENT) || flags.contains(Flag.FLAGGED) || flags.contains(Flag.USER)) {
			this.emailStatus = "UnRead";
			this.checked = "false";
		} else {
			this.emailStatus = "UnRead";
			this.checked = "false";
		}

		if (flags.contains(Flag.FLAGGED)) {
			this.emailPriority = "Important";
			this.emailPriorityExtra = "Important";
		} else if (flags.contains(Flag.ANSWERED) || flags.contains(Flag.DELETED) || flags.contains(Flag.DRAFT)
				|| flags.contains(Flag.RECENT) || flags.contains(Flag.SEEN) || flags.contains(Flag.USER)) {
			this.emailPriority = "UnImportant";
			this.emailPriorityExtra = "UnImportant";
		} else {
			this.emailPriority = "UnImportant";
			this.emailPriorityExtra = "UnImportant";
		}

		this.multipleAttachment = multipleAttachment2;
	}

	// important listing
	public Email(String from2, long uid, String[] toArray, String subject2, String result, DateTime dtRec,
			String[] ccArray, String[] bccArray, Folder folder, long uid2, Flags flags,
			List<MultipleAttachment> multipleAttachment2) {
		this.createdOn = dtRec;
		this.id = uid;
		this.from = from2;
		this.to = toArray;
		this.cc = ccArray;
		this.bcc = bccArray;
		this.subject = subject2;
		this.body = result;
		if (folder.getName().equals("INBOX")) {
			this.emailType = "Received";
		} else if (folder.getName().equals("Sent")) {
			this.emailType = "Sent";
		} else if (folder.getName().equals("Drafts")) {
			this.emailType = "Draft";
		} else if (folder.getName().equals("Trash")) {
			this.emailType = "Deleted";
		} else {
			this.emailType = folder.getName();
		}
		if (flags.contains(Flag.SEEN)) {
			this.emailStatus = "Read";
			this.checked = "true";
		} else if (flags.contains(Flag.ANSWERED) || flags.contains(Flag.DELETED) || flags.contains(Flag.DRAFT)
				|| flags.contains(Flag.RECENT) || flags.contains(Flag.FLAGGED) || flags.contains(Flag.USER)) {
			this.emailStatus = "UnRead";
			this.checked = "false";
		} else {
			this.emailStatus = "UnRead";
			this.checked = "false";
		}

		if (flags.contains(Flag.FLAGGED)) {
			this.emailPriority = "Important";
			this.emailPriorityExtra = "Important";
		} else if (flags.contains(Flag.ANSWERED) || flags.contains(Flag.DELETED) || flags.contains(Flag.DRAFT)
				|| flags.contains(Flag.RECENT) || flags.contains(Flag.SEEN) || flags.contains(Flag.USER)) {
			this.emailPriority = "UnImportant";
			this.emailPriorityExtra = "UnImportant";
		} else {
			this.emailPriority = "UnImportant";
			this.emailPriorityExtra = "UnImportant";
		}

		this.multipleAttachment = multipleAttachment2;
	}

	public Email(String subject2) {
		this.subject = subject2;
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

	public String[] getBcc() {
		return bcc;
	}

	public void setBcc(String[] bcc) {
		this.bcc = bcc;
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

	public List<MultipleAttachment> getMultipleAttachment() {
		return multipleAttachment;
	}

	public void setMultipleAttachment(List<MultipleAttachment> multipleAttachment) {
		this.multipleAttachment = multipleAttachment;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String[] getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String[] replyTo) {
		this.replyTo = replyTo;
	}

	public boolean isRepliedFlag() {
		return repliedFlag;
	}

	public void setRepliedFlag(boolean repliedFlag) {
		this.repliedFlag = repliedFlag;
	}

	public boolean isForwardedFlag() {
		return forwardedFlag;
	}

	public void setForwardedFlag(boolean forwardedFlag) {
		this.forwardedFlag = forwardedFlag;
	}

	@Override
	public String toString() {
		return "Email [id=" + id + ", to=" + Arrays.toString(to) + ", cc=" + Arrays.toString(cc) + ", bcc="
				+ Arrays.toString(bcc) + ", from=" + from + ", subject=" + subject + ", body=" + body + ", emailType="
				+ emailType + ", emailStatus=" + emailStatus + ", checked=" + checked + ", emailPriority="
				+ emailPriority + ", emailPriorityExtra=" + emailPriorityExtra + "]";
	}

}
