package com.qaplatform.factory;

import com.qaplatform.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 普通用户工厂
 * 创建普通权限的用户
 */
@Component("regularUserFactory")
public class RegularUserFactory implements UserFactory {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public User createUser(String username, String password, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(User.UserRole.USER);
        user.setEnabled(true);
        user.setReputation(0);
        user.setAvatar("/api/avatar/default.png");  // 默认头像
        user.setBio("这个用户很懒，什么都没有留下~");
        
        return user;
    }
} 