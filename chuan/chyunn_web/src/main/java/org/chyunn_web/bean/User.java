package org.chyunn_web.bean;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Entity
@Table(name = "user")
public class User {
        @Id
        @Column(name = "loginId", nullable = false, unique = true, length = 100)
        private String loginId;  // 主鍵登入帳號 (LOGINID)
        private String userId;  // 串接員工 USERID (USERID)
        private String username;  // 使用者名稱 (USERNAME)
        private String password;  // 密碼 (應加密)

        private boolean enabled = true;  // 是否啟用該使用者

        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<UserRole> roles;  // 使用者的角色

}
