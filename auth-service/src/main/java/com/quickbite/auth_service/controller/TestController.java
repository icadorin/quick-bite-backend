package com.quickbite.auth_service.controller;

import com.quickbite.auth_service.entity.User;
import com.quickbite.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/test")
    public String test() {
        return "Auth Service is working! Data base connected: " +
            (userRepository != null ? "YES" : "NO");
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        System.out.println("Usuários encontrados: " + users.size());
        return users;
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }
}

