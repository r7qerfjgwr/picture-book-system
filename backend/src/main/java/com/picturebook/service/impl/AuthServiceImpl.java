package com.picturebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.picturebook.dto.LoginDTO;
import com.picturebook.dto.RegisterDTO;
import com.picturebook.entity.SysRole;
import com.picturebook.entity.SysUser;
import com.picturebook.exception.BusinessException;
import com.picturebook.mapper.SysRoleMapper;
import com.picturebook.mapper.SysUserMapper;
import com.picturebook.security.LoginUser;
import com.picturebook.service.AuthService;
import com.picturebook.utils.JwtUtils;
import com.picturebook.vo.LoginVO;
import com.picturebook.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private SysUserMapper userMapper;
    
    @Autowired
    private SysRoleMapper roleMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())
        );
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            SysRole role = roleMapper.selectById(loginUser.getRoleId());
            String roleKey = role != null ? role.getRoleKey() : "PARENT";
            String token = jwtUtils.generateToken(loginUser.getUserId(), loginUser.getUsername(), 
                                                   loginUser.getRoleId(), roleKey);
            userMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, user.getId())
                .set(SysUser::getLoginFailCount, 0)
                .set(SysUser::getLastLoginTime, LocalDateTime.now()));
            LoginVO vo = new LoginVO();
            vo.setToken(token);
            UserInfoVO userInfo = new UserInfoVO();
            userInfo.setId(loginUser.getUserId());
            userInfo.setUsername(loginUser.getUsername());
            userInfo.setRealName(user.getRealName());
            userInfo.setPhone(user.getPhone());
            userInfo.setEmail(user.getEmail());
            userInfo.setRoleId(loginUser.getRoleId());
            userInfo.setRoleName(role != null ? role.getRoleName() : "");
            userInfo.setRoleKey(roleKey);
            userInfo.setAvatar(user.getAvatar());
            userInfo.setLastLoginTime(LocalDateTime.now());
            vo.setUserInfo(userInfo);
            return vo;
        } catch (Exception e) {
            throw new BusinessException("用户名或密码错误");
        }
    }
    
    @Override
    public void register(RegisterDTO dto) {
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        SysRole role = roleMapper.selectById(dto.getRoleId());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRoleId(dto.getRoleId());
        user.setStatus(1);
        user.setLoginFailCount(0);
        userMapper.insert(user);
    }
    
    @Override
    public UserInfoVO getCurrentUser() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        SysUser user = userMapper.selectById(loginUser.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SysRole role = roleMapper.selectById(user.getRoleId());
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setRoleId(user.getRoleId());
        vo.setRoleName(role != null ? role.getRoleName() : "");
        vo.setRoleKey(role != null ? role.getRoleKey() : "");
        vo.setAvatar(user.getAvatar());
        vo.setLastLoginTime(user.getLastLoginTime());
        return vo;
    }
    
    @Override
    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
