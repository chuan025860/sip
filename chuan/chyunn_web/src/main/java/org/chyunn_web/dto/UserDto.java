package org.chyunn_web.dto;

import lombok.Data;

@Data
public class UserDto {
    private String loginId;  // 主鍵登入帳號 (LOGINID)
    private String userId;  // 串接員工 USERID (USERID)
    private String username;  // 使用者名稱 (USERNAME)
    private String password;  // 密碼 (應加密)
    private String email; //email
    private String employee_no; //工號
    private String departmentName;
    private boolean enabled = true;  // 是否啟用該使用者
    public UserDto() {

    }
    public UserDto(String loginId, String username) {
        this.loginId = loginId;
        this.username  = username;
    }
}
