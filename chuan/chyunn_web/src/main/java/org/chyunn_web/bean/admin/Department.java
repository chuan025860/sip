package org.chyunn_web.bean.admin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.chyunn_web.bean.User.User;
import org.chyunn_web.bean.User.UserRole;

import java.util.List;
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

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
    private List<User> users;  // 使用者的角色

    @OneToMany(mappedBy="department")
    @OrderBy("id ASC")
    Set<SubUnit> subUnits;

    @OneToMany(mappedBy="department") // 直屬職位
    @OrderBy("id ASC")
    Set<Position> positions;
}




