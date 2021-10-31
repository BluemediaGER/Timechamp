package dev.bluemedia.timechamp.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.bluemedia.timechamp.model.type.Permission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * RequestModel used to create an new {@link dev.bluemedia.timechamp.model.object.User}.
 *
 * @author Oliver Traber
 */
public class UserCreateRequest {

    /** Username of the new user */
    @JsonProperty("username")
    @NotNull(message = "field is required")
    @NotEmpty(message = "field cannot be empty")
    private String username;

    /** Password of the new user */
    @JsonProperty("password")
    @NotNull(message = "field is required")
    @NotEmpty(message = "field cannot be empty")
    private String password;

    /** {@link Permission} for the new user */
    @JsonProperty("permission")
    @NotNull(message = "field is not supplied or invalid")
    private Permission permission;

    /** Default constructor for Jackson deserialization */
    public UserCreateRequest() {}

    /**
     * Get the username for the new {@link dev.bluemedia.timechamp.model.object.User}.
     * @return Username for the new {@link dev.bluemedia.timechamp.model.object.User}.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the password for the new {@link dev.bluemedia.timechamp.model.object.User}.
     * @return Password for the new {@link dev.bluemedia.timechamp.model.object.User}.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get the {@link Permission} for the new {@link dev.bluemedia.timechamp.model.object.User}.
     * @return {@link Permission} for the new {@link dev.bluemedia.timechamp.model.object.User}.
     */
    public Permission getPermission() {
        return permission;
    }

}
