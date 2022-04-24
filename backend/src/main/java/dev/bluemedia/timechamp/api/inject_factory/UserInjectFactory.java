package dev.bluemedia.timechamp.api.inject_factory;

import dev.bluemedia.timechamp.model.object.User;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.glassfish.hk2.api.Factory;

public class UserInjectFactory implements Factory<User> {

    private final ContainerRequestContext context;

    @Inject
    public UserInjectFactory(ContainerRequestContext context) {
        this.context = context;
    }

    @Override
    public User provide() {
        return (User)context.getProperty("userFromFilter");
    }

    @Override
    public void dispose(User t) {}
}
