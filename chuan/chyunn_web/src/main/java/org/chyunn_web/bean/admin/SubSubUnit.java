package org.chyunn_web.bean.admin;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;
//第二層部門
@Data
@Entity
@Table(name="sub_sub_unit")
public class SubSubUnit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_unit_id", nullable = false)
    private SubUnit subUnit;

    private String name;

    @OneToMany(mappedBy="subSubUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private Set<Position> positions = new HashSet<>();


}