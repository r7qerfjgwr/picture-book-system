package com.picturebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.dto.PageDTO;
import com.picturebook.entity.SysUser;
import com.picturebook.exception.BusinessException;
import com.picturebook.mapper.SysUserMapper;
import com.picturebook.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public Page<SysUser> getUserList(PageDTO dto) {
        Page<SysUser> page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(dto.getKeyword())) {
            wrapper.like(SysUser::getUsername, dto.getKeyword())
                   .or().like(SysUser::getRealName, dto.getKeyword());
        }
        if (dto.getParentId() != null) {
            wrapper.eq(SysUser::getRoleId, dto.getParentId());
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }
    
    @Override
    public SysUser getUserById(Long id) {
        SysUser user = userMapper.selectUserWithRoleById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
    
    @Override
    public void updateUser(SysUser user) {
        SysUser existUser = userMapper.selectById(user.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        userMapper.updateById(user);
    }
    
    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
    
    @Override
    public void resetPassword(Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(user);
    }
}
