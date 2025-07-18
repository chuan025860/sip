package org.chyunn_web.bean.Asset;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "category_general_catalog")
public class Category_General_Catalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;
}
