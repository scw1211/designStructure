package com.qaplatform.factory;

import com.qaplatform.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 版主用户工厂
 * 创建版主权限的用户
 */
@Component("moderatorUserFactory")
public class ModeratorUserFactory implements UserFactory {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public User createUser(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(User.UserRole.MODERATOR);
        user.setEnabled(true);
        user.setReputation(500);  // 版主初始声望值中等
        user.setAvatar("/api/avatar/moderator.png");  // 版主专属头像
        user.setBio("平台版主");
        
        return user;
    }
} 