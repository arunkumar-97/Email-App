package com.jesperapps.email.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Size;

@SuppressWarnings("serial")
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = { "createDateTime", "updateDateTime" }, allowGetters = true)
public abstract class AbstractAuditingEntity implements Serializable {

	@Column(name = "CREATION_DATE", updatable = false)
	@JsonSerialize(using = ToStringSerializer.class)
	@CreationTimestamp
	protected LocalDateTime createDateTime;

	@Column(name = "LAST_UPDATED_DATE")
	@JsonSerialize(using = ToStringSerializer.class)
	@UpdateTimestamp
	protected LocalDateTime updateDateTime;

	@Column(name = "CREATED_BY")
	@Size(max = 150)
	protected String createdBy;

	@Column(name = "LAST_UPDATED_BY")
	@Size(max = 150)
	protected String lastUpdatedBy;

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

	@Override
	public String toString() {
		return "AbstractAuditingEntity [createDateTime=" + createDateTime + ", updateDateTime=" + updateDateTime
				+ ", createdBy=" + createdBy + ", lastUpdatedBy=" + lastUpdatedBy + "]";
	}
}
