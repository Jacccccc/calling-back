package com.example.demo.dao;

import com.example.demo.entity.Role;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleMapper {

    @Select("select user_role.role_id as id,role_name  from user_role left join role on user_role.role_id=role.id where user_id=#{user_id}")
    @Results(
            value = {
                    @Result(id=true, property = "id",column = "id"),
                    @Result(property = "name",column = "name"),
                    @Result(property = "permissions",column = "id",
                            many = @Many(select = "com.example.demo.dao.PermissionMapper.findPermissionListByRoleId", fetchType = FetchType.DEFAULT)
                    )
            }
    )
    List<Role> findRoleListByUserId(@Param("user_id")Long user_id);

    @Insert("insert into user_role(user_id,role_id) values(#{user_id},#{role_id}) ")
    int insertUserRole(Long user_id,Long role_id);
}
