package com.gettgi.mvp.service.Impl;

import com.gettgi.mvp.entity.User;
import com.gettgi.mvp.repository.UserRepository;
import com.gettgi.mvp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    @Override
    public User findByTelephone(String telephone) {
        return userRepository.findByTelephone(telephone).orElse(null) ;
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
