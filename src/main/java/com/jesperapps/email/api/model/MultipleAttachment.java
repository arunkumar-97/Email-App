package com.jesperapps.email.api.model;

import java.io.Serializable;
import java.util.Arrays;

public class MultipleAttachment implements Serializable {


	private String id;
	private String fileName;
	private String fileType;
	private Long fileSize;
	private byte[] fileByte;
	
	private String fileDownloadUrl;
	private String fileViewUrl;
	
	private Email email;

	public MultipleAttachment() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
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

	public Email getEmail() {
		return email;
	}

	public void setEmail(Email email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "MultipleAttachment [id=" + id + ", fileName=" + fileName + ", fileType=" + fileType + ", fileSize="
				+ fileSize + ", fileByte=" + Arrays.toString(fileByte) + ", fileDownloadUrl="
				+ fileDownloadUrl + ", fileViewUrl=" + fileViewUrl + "]";
	}

	
}
