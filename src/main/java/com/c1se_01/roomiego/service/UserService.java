package com.c1se_01.roomiego.service;

import com.c1se_01.roomiego.model.User;

import java.util.List;

public interface UserService {
    List<User> findByFullName(String fullName);
}
