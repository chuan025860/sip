package org.chyunn_web.bean.Resource;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="vehicle")
@Data
public class Vehicle {
    @Id
    @GeneratedValue
    Integer id;
    private String name;
    private String plate;
}