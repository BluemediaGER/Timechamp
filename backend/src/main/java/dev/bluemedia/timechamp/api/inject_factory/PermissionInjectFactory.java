package dev.bluemedia.timechamp.api.inject_factory;

import dev.bluemedia.timechamp.model.type.Permission;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.glassfish.hk2.api.Factory;

public class PermissionInjectFactory implements Factory<Permission> {

    private final ContainerRequestContext context;

    @Inject
    public PermissionInjectFactory(ContainerRequestContext context) {
        this.context = context;
    }

    @Override
    public Permission provide() {
        return (Permission) context.getProperty("permission");
    }

    @Override
    public void dispose(Permission t) {}
}
