package org.chyunn_web.bean.User;

import jakarta.persistence.*;
import lombok.Data;
import org.chyunn_web.bean.Resource.ResourceBorrowRequest;
import org.chyunn_web.bean.admin.Department;
import org.chyunn_web.bean.admin.Position;
import org.chyunn_web.bean.admin.SubSubUnit;
import org.chyunn_web.bean.admin.SubUnit;

import java.util.List;

@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String loginId;  // 登入帳號 (LOGINID)
    private String userId;  // 串接員工 USERID (USERID)
    private String username;  // 使用者名稱 (USERNAME)
    private String password;  // 密碼 (應加密)
    private String employee_no;//工號
    private String email; //email
    private boolean enabled = true;  // 是否啟用該使用者

    // 多個使用者會指向同一個位置 department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    // 多個使用者會指向同一個位置 sub_unit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_unit_id")
    private SubUnit subUnit;

    // 多個使用者會指向同一個位置sub_sub_unit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_sub_unit_id")
    private SubSubUnit subSubUnit;

    // 多個使用者會指向同一個位置Position
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY,orphanRemoval = true)
    private List<UserRole> roles;  // 使用者的角色

    // 一對多：一個使用者可建立多筆借用紀錄
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResourceBorrowRequest> borrowRequests;

}
