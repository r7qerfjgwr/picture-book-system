package com.picturebook.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class PasswordTestController {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/encode")
    public Map<String, Object> encodePassword(@RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        String encoded = passwordEncoder.encode(password);
        result.put("original", password);
        result.put("encoded", encoded);
        result.put("length", encoded.length());
        result.put("matches", passwordEncoder.matches(password, encoded));
        return result;
    }
    
    @GetMapping("/verify")
    public Map<String, Object> verifyPassword(@RequestParam String password, @RequestParam String hash) {
        Map<String, Object> result = new HashMap<>();
        result.put("password", password);
        result.put("hash", hash);
        result.put("matches", passwordEncoder.matches(password, hash));
        return result;
    }
}
