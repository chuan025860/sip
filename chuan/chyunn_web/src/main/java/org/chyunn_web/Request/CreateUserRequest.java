package org.chyunn_web.Request;

import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {
    public String loginId;
    public String originalLoginId;
    public String password;
    public String username;
    public String employeeNo;
    public String email;
    public Integer departmentId;
    public Integer subUnitId;
    public Integer subSubUnitId;
    public Integer positionId;
    private List<String> permissions;
}
