package com.danya.mdm.model;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static com.danya.mdm.util.Constant.DEFAULT_DB_USER;

@Getter
@Setter
@MappedSuperclass
public class AuditableEntity {

    private LocalDateTime createTime;
    private String createUser;
    private LocalDateTime lastUpdateTime;
    private String lastUpdateUser;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createTime = now;
        this.lastUpdateTime = now;
        this.createUser = DEFAULT_DB_USER;
        this.lastUpdateUser = DEFAULT_DB_USER;
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdateTime = LocalDateTime.now();
        this.lastUpdateUser = DEFAULT_DB_USER;
    }
}
