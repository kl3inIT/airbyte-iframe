package com.company.airbyteiframe.security;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.*;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@RowLevelRole(name = "Airbyte-Admin", code = AirbyteAdminRole.CODE)
@ResourceRole(name = "Airbyte-Admin", code = AirbyteAdminRole.CODE)
public interface AirbyteAdminRole {
    String CODE = "airbyte-admin";

    @EntityPolicy(entityName = "*", actions = {EntityPolicyAction.ALL})
    @EntityAttributePolicy(entityName = "*", attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @ViewPolicy(viewIds = "*")
    @MenuPolicy(menuIds = "*")
    @SpecificPolicy(resources = "*")
    void fullAccess();
}