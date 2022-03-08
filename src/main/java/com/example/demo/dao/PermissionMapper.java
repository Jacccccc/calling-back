package com.example.demo.dao;

import com.example.demo.entity.Permission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionMapper {

    @Select("select  rp.permission_id as id,p.per_name as permissionName  from role_permission rp left join permission p on rp.permission_id=p.id where rp.role_id=#{role_id} ")
    List<Permission> findPermissionListByRoleId(@Param("role_id") int role_id);
}
