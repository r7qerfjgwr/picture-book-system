package com.picturebook.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserInfoVO {
    
    private Long id;
    
    private String username;
    
    private String realName;
    
    private String phone;
    
    private String email;
    
    private Long roleId;
    
    private String roleName;
    
    private String roleKey;
    
    private String avatar;
    
    private LocalDateTime lastLoginTime;
}
