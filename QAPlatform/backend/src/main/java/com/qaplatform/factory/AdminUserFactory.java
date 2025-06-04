package com.qaplatform.factory;

import com.qaplatform.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 管理员用户工厂
 * 创建管理员权限的用户
 */
@Component("adminUserFactory")
public class AdminUserFactory implements UserFactory {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public User createUser(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(User.UserRole.ADMIN);
        user.setEnabled(true);
        user.setReputation(1000);  // 管理员初始声望值更高
        user.setAvatar("/api/avatar/admin.png");  // 管理员专属头像
        user.setBio("系统管理员");
        
        return user;
    }
} 