package com.generatera.authorization.jpa.audit;


import org.springframework.data.annotation.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.MappedSuperclass;
import java.time.Instant;

/**
 * 目的是让程序自动填充
 * @author weir
 *
 */
@MappedSuperclass
@Embeddable
@AccessType(AccessType.Type.FIELD)
public class Audit {

	@CreatedBy
	@Column(name = "created_by")
	private Long createdBy;

	@CreatedDate
	@Column(name = "created_date")
	private Instant createdDate;

	@LastModifiedBy
	@Column(name = "updated_by")
	private Long lastModifiedBy;

	@LastModifiedDate
	@Column(name = "updated_date")
	private Instant lastModifiedDate;
	
	public Audit() {
		
	}
	
	public Audit(Long createdBy, Instant createdDate, Long lastModifiedBy, Instant lastModifiedDate) {
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.lastModifiedBy = lastModifiedBy;
		this.lastModifiedDate = lastModifiedDate;
	}
	
	public Audit(Audit audit) {
		this.createdBy = audit.createdBy;
		this.createdDate = audit.createdDate;
		this.lastModifiedBy = audit.lastModifiedBy;
		this.lastModifiedDate = audit.lastModifiedDate;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Instant getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Instant createdDate) {
		this.createdDate = createdDate;
	}

	public Long getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(Long lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public Instant getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Instant lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

}
