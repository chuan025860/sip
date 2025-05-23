package org.chyunn_web.bean.admin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;
//部門
@Entity
@Table(name="department")
@Data
@EqualsAndHashCode(exclude = {"subUnits","positions"})
public class Department {
    @Id
    @GeneratedValue
    Integer id;

    String name;

    @OneToMany(mappedBy="department")
    @OrderBy("id ASC")
    Set<SubUnit> subUnits;

    @OneToMany(mappedBy="department") // 直屬職位
    @OrderBy("id ASC")
    Set<Position> positions;
}




