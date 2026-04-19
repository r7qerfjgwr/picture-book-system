package com.picturebook.service;

import com.picturebook.dto.LoginDTO;
import com.picturebook.dto.RegisterDTO;
import com.picturebook.vo.LoginVO;
import com.picturebook.vo.UserInfoVO;

public interface AuthService {
    
    LoginVO login(LoginDTO dto);
    
    void register(RegisterDTO dto);
    
    UserInfoVO getCurrentUser();
    
    void logout();
}
