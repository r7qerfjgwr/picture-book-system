package com.picturebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebook.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

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

    @Select("SELECT u.*, r.role_name, r.role_key, r.description " +
            "FROM sys_user u " +
            "LEFT JOIN sys_role r ON u.role_id = r.id " +
            "WHERE r.role_key = #{roleKey} AND u.status = 1")
    List<SysUser> selectUsersByRoleKey(String roleKey);

    @Select("<script>" +
            "SELECT u.*, r.role_name, r.role_key, r.description " +
            "FROM sys_user u " +
            "LEFT JOIN sys_role r ON u.role_id = r.id " +
            "WHERE 1=1 " +
            "<if test='username != null and username != \"\"'>" +
            "AND u.username LIKE CONCAT('%', #{username}, '%') " +
            "</if>" +
            "<if test='role != null and role != \"\"'>" +
            "AND r.role_key = #{role} " +
            "</if>" +
            "ORDER BY u.create_time DESC " +
            "</script>")
    Page<SysUser> selectUserPageWithRole(Page<SysUser> page,
                                          @Param("username") String username,
                                          @Param("role") String role);
}
