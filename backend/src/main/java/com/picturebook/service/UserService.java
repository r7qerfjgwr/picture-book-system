package com.picturebook.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.PageDTO;
import com.picturebook.entity.SysUser;

public interface UserService {
    
    Page<SysUser> getUserList(PageDTO dto);
    
    SysUser getUserById(Long id);
    
    void updateUser(SysUser user);
    
    void deleteUser(Long id);
    
    void resetPassword(Long id);
}
