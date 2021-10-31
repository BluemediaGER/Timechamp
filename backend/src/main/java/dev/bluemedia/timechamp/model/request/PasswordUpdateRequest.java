package dev.bluemedia.timechamp.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request model used to change the password of an existing
 * {@link dev.bluemedia.timechamp.model.object.User}.
 *
 * @author Oliver Traber
 */
public class PasswordUpdateRequest {

    /** New password that should be set for the user */
    @JsonProperty("password")
    @NotNull(message = "field is required")
    @NotEmpty(message = "field cannot be empty")
    private String password;

    /** Default constructor for Jackson deserialization */
    public PasswordUpdateRequest() {}

    /**
     * Get the new password for the user.
     * @return New password for the user.
     */
    public String getPassword() {
        return password;
    }

}
