package com.gettgi.mvp.service;

import com.gettgi.mvp.entity.User;

public interface UserService {

    User findByTelephone(String telephone);

    User save(User user);

    boolean existsByEmail(String email);


}
