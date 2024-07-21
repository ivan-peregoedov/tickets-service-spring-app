package com.pepeg.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pepeg.application.entity.User;
import com.pepeg.application.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User findUserById(Long id) {
        return userRepository.findById(id).get();
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).get();
    }
}
