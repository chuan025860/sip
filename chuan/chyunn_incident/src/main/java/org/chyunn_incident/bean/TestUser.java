package org.chyunn_incident.bean;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "testuser")

public class TestUser {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "password")
    private String password;
}
