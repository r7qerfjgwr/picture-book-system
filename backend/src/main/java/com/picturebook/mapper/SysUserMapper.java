package com.picturebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.picturebook.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    
    @Select("SELECT u.*, r.role_name, r.role_key, r.description " +
            "FROM sys_user u " +
            "LEFT JOIN sys_role r ON u.role_id = r.id " +
            "WHERE u.id = #{id}")
    SysUser selectUserWithRoleById(Long id);
    
    @Select("SELECT u.*, r.role_name, r.role_key, r.description " +
            "FROM sys_user u " +
            "LEFT JOIN sys_role r ON u.role_id = r.id " +
            "WHERE u.username = #{username}")
    SysUser selectUserWithRoleByUsername(String username);
}
