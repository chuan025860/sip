package org.chyunn_web.bean.admin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
//第一層、二層 部門職位  用fk綁定
@Entity
@Table(name="position")
@Data
@EqualsAndHashCode(exclude = {"department","subUnit","subSubUnit"})
public class Position {
    @Id
    @GeneratedValue
    Integer id;

    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_unit_id")
    SubUnit subUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_sub_unit_id")
    SubSubUnit subSubUnit;
}