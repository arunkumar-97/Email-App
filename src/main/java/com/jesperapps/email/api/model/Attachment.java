package com.jesperapps.email.api.model;

import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.web.multipart.MultipartFile;

@Entity
@Table(name = "EMP_ATTACHMENT")
public class Attachment {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Integer id;
	private String fileName;
	private String fileType;
	private Long fileSize;
	@Lob
	private byte[] fileByte;
	private Integer status;

	private String fileDownloadUrl;
	private String fileViewUrl;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	public Attachment() {

	}

	public Attachment(Attachment attachment) {
		super();
		this.id = attachment.getId();
		this.fileName = attachment.getFileName();
		this.fileType = attachment.getFileType();
		this.fileSize = attachment.getFileSize();
		this.fileByte = attachment.getFileByte();
		this.fileDownloadUrl = attachment.getFileDownloadUrl();
		this.fileViewUrl = attachment.getFileViewUrl();
	}

	public Attachment(MultipartFile file) throws IOException {
		this.fileName = file.getOriginalFilename();
		this.fileType = file.getContentType();
		this.fileSize = file.getSize();
		this.fileByte = file.getBytes();
	}


	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public byte[] getFileByte() {
		return fileByte;
	}

	public void setFileByte(byte[] fileByte) {
		this.fileByte = fileByte;
	}



	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getFileDownloadUrl() {
		return fileDownloadUrl;
	}

	public void setFileDownloadUrl(String fileDownloadUrl) {
		this.fileDownloadUrl = fileDownloadUrl;
	}

	public String getFileViewUrl() {
		return fileViewUrl;
	}

	public void setFileViewUrl(String fileViewUrl) {
		this.fileViewUrl = fileViewUrl;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Attachment [attachmentId=" + id + ", fileName=" + fileName + ", fileType=" + fileType
				+ ", fileSize=" + fileSize + ", status=" + status + ", fileDownloadUrl=" + fileDownloadUrl
				+ ", fileViewUrl=" + fileViewUrl + "]";
	}

}
