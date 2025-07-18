package org.chyunn_web.bean.admin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;
//第一層部門
@Entity
@Table(name="sub_unit")
@Data
@EqualsAndHashCode(exclude = {"department","positions","subSubUnits"})
public class SubUnit {
    @Id
    @GeneratedValue
    Integer id;

    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")   // ← 這裡保留 FK
    Department department;


    @OneToMany(mappedBy="subUnit", cascade = CascadeType.ALL)
    @OrderBy("id ASC")
        Set<Position> positions;

    @OneToMany(mappedBy="subUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private Set<SubSubUnit> subSubUnits = new HashSet<>();
}