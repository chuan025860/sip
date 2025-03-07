package org.chyunn_web.service;

import org.chyunn_web.bean.User;
import org.chyunn_web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;

    public User getTestUser(String id) {
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
}
