package org.chyunn_web.bean;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

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

}
