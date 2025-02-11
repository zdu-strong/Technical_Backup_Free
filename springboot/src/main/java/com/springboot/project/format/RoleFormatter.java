package com.springboot.project.format;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import com.springboot.project.common.baseService.BaseService;
import com.springboot.project.entity.RoleEntity;
import com.springboot.project.model.RoleModel;

@Service
public class RoleFormatter extends BaseService {

    public RoleModel format(RoleEntity roleEntity) {
        var roleModel = new RoleModel();
        BeanUtils.copyProperties(roleEntity, roleModel);
        var id = roleEntity.getId();

        var organizeList = this.streamAll(RoleEntity.class)
                .where(s -> s.getId().equals(id))
                .selectAllList(s -> s.getRoleOrganizeRelationList())
                .select(s -> s.getOrganize())
                .where(s -> s.getIsActive())
                .map(s -> this.organizeFormatter.format(s))
                .toList();
        roleModel.setOrganizeList(organizeList);

        var permissionList = this.streamAll(RoleEntity.class)
                .where(s -> s.getId().equals(id))
                .selectAllList(s -> s.getRolePermissionRelationList())
                .select(s -> s.getPermission().getName())
                .toList();
        roleModel.setPermissionList(permissionList);
        return roleModel;
    }

}
