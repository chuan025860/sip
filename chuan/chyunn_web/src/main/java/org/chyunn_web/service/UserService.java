package org.chyunn_web.service;

import jakarta.transaction.Transactional;
import org.chyunn_web.bean.User;
import org.chyunn_web.bean.UserRole;
import org.chyunn_web.bean.UserRoleId;
import org.chyunn_web.exception.UserNotFoundException;
import org.chyunn_web.repository.UserRepository;
import org.chyunn_web.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRoleRepository userRoleRepository;
    public User getUser(String id) {
        Optional<User> optional = userRepository.findById(id);
        if (optional.isPresent()) {
            User user = optional.get();
            return user;
        } else {
            return null;
        }
    }

    public Boolean existsByUserId(String userId) {
        return userRepository.existsByUserId(userId);
    }
    public void saveUser(User user) {
        userRepository.save(user);
    }

    public List<String> findRolesByLoginId(String loginId) {
        return userRoleRepository.findRolesByLoginId(loginId);
    }

    @Transactional
    public void updateUserPermissions(String loginId, List<String> permissions) {
        User user = userRepository.findById(loginId)
                .orElseThrow(() -> new UserNotFoundException("找不到使用者"));

        userRoleRepository.deleteByLoginId(user.getLoginId());

        // 建立新的角色清單
        List<UserRole> newRoles = new ArrayList<>();
        for (String role : permissions) {
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setId(new UserRoleId(loginId, role));
            newRoles.add(userRole);
        }

        // 儲存新的角色
        userRoleRepository.saveAll(newRoles);
    }


}
