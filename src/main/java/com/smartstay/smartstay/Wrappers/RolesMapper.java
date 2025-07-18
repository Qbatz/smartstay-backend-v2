package com.smartstay.smartstay.Wrappers;

import com.smartstay.smartstay.dao.RolesV1;
import com.smartstay.smartstay.responses.Roles;

import java.util.function.Function;

public class RolesMapper implements Function<RolesV1, Roles> {
    @Override
    public Roles apply(RolesV1 rolesV1) {
        return new Roles(
                rolesV1.getRoleId(), rolesV1.getRoleName(), rolesV1.getIsActive()
        );
    }
}
