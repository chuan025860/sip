package org.chyunn_web.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.chyunn_web.Request.CreateUserRequest;
import org.chyunn_web.bean.Asset.Inventory_Equipment;
import org.chyunn_web.bean.admin.Department;
import org.chyunn_web.bean.User.User;
import org.chyunn_web.bean.User.UserRole;
import org.chyunn_web.bean.User.UserRoleId;
import org.chyunn_web.bean.admin.Position;
import org.chyunn_web.bean.admin.SubSubUnit;
import org.chyunn_web.bean.admin.SubUnit;
import org.chyunn_web.dto.Inventory_EquipmentDto;
import org.chyunn_web.dto.UserDto;
import org.chyunn_web.exception.UserNotFoundException;
import org.chyunn_web.repository.DepartmentRepo;
import org.chyunn_web.repository.UserRepository;
import org.chyunn_web.repository.UserRoleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRoleRepository userRoleRepository;
    @Autowired
    DepartmentRepo departmentRepo;
    @Autowired
    PasswordEncoder pwdEncoder;
    @Autowired
    DepartmentService departmentService;

    private ModelMapper modelMapper = new ModelMapper();

    public UserDto convertUserDto(User user) {
        // 自動映射
        return modelMapper.map(user, UserDto.class);
    }

    public User getUser(String loginId) {
        User user = userRepository.findByLoginId(loginId);
        System.out.println(loginId);
        return user;
    }

    public Boolean existsById(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    public Boolean existsByLoginId(String userId) {
        return userRepository.existsByLoginId(userId);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
    public User saveReturnUser(User user) {
       return userRepository.save(user);
    }

    public void deleteById(String loginId) {
        userRepository.deleteById(loginId);
    }

    public void deleteUserRoleById(UserRoleId userRoleId) {
        userRoleRepository.deleteById(userRoleId);
    }

    public List<String> findRolesByLoginId(String loginId) {
        return userRoleRepository.findRolesByLoginId(loginId);
    }

    public Page<User> findAllWithSpecialOrder(Pageable pageable) {
        // 直接回傳分頁結果
        return userRepository.findAllWithSpecialOrder(pageable);
    }
    public Page<User> findUserKeyword(String keyword,Pageable pageable) {
        // 直接回傳分頁結果
        return userRepository.findUserKeyword(keyword,pageable);
    }

    public List<User> findByDepartmentId(Integer deptId) {
        return userRepository.findByDepartmentId(deptId);
    }


    //    @Transactional
//    public void updateUserPermissions(String loginId, List<String> permissions) {
//        User user = userRepository.findById(loginId)
//                .orElseThrow(() -> new UserNotFoundException("找不到使用者"));
//
//        userRoleRepository.deleteByLoginId(user.getLoginId());
//
//        // 建立新的角色清單
//        List<UserRole> newRoles = new ArrayList<>();
//        for (String role : permissions) {
//            UserRole userRole = new UserRole();
//            userRole.setUser(user);
//            userRole.setId(new UserRoleId(loginId, role));
//            newRoles.add(userRole);
//        }
//
//        // 儲存新的角色
//        userRoleRepository.saveAll(newRoles);
//    }
    public List<Department> findAllWithTree() {
        return departmentRepo.findAllWithTree();
    }


}
