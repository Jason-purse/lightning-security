package com.generatera.authorization.oauth2.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity {

    @CreatedBy
    @Column(name = "created_at")
    private Long createdAt;

    @CreatedDate
    @Column(name = "created_date")
    private Instant createdDate;

    @LastModifiedBy
    @Column(name = "updated_at")
    private Long updatedAt;

    @LastModifiedDate
    @Column(name = "updated_date")
    private Instant updatedDate;
}
