package com.qaplatform.factory;

import com.qaplatform.model.User;

/**
 * 用户工厂接口
 * 工厂模式：定义创建用户对象的接口
 */
public interface UserFactory {
    
    /**
     * 创建用户
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @return 用户对象
     */
    User createUser(String username, String password, String email);
} 