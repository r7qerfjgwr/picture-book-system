package com.picturebook.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.picturebook.entity.SysRole;
import com.picturebook.entity.SysUser;
import com.picturebook.mapper.SysRoleMapper;
import com.picturebook.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Autowired
    private SysRoleMapper roleMapper;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }
        SysRole role = roleMapper.selectById(user.getRoleId());
        if (role != null) {
            user.setRole(role);
        }
        LoginUser loginUser = new LoginUser(user);
        loginUser.setRoleKey(role != null ? role.getRoleKey() : "PARENT");
        return loginUser;
    }
}
