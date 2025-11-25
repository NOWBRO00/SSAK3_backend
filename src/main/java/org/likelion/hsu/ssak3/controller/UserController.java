package org.likelion.hsu.ssak3.controller;

import org.likelion.hsu.ssak3.entity.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.likelion.hsu.ssak3.repository.UserProfileRepository;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileRepository userRepository;

    @Autowired
    public UserController(UserProfileRepository userRepository) {
        this.userRepository = userRepository;
    }

    //[POST] 유저 등록
    @PostMapping
    public UserProfile createUser(@RequestBody UserProfile user) {
        return userRepository.save(user);
    }

    //[GET] 전체 유저 조회
    @GetMapping
    public List<UserProfile> getAllUsers() {
        return userRepository.findAll();
    }

    //[GET] 특정 유저 조회
    @GetMapping("/{id}")
    public UserProfile getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    //[DELETE] 유저 삭제
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }
}
