package com.complaintiq.common;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
@Getter @Setter @MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate @Column(name="created_at",nullable=false,updatable=false) private LocalDateTime createdAt;
    @LastModifiedDate @Column(name="updated_at") private LocalDateTime updatedAt;
    @CreatedBy @Column(name="created_by",updatable=false) private String createdBy;
    @LastModifiedBy @Column(name="updated_by") private String updatedBy;
}
