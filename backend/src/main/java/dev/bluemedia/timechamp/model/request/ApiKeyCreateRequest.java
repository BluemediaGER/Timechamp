package dev.bluemedia.timechamp.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.bluemedia.timechamp.model.type.Permission;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request model used to create an new {@link dev.bluemedia.timechamp.model.object.ApiKey}.
 *
 * @author Oliver Traber
 */
public class ApiKeyCreateRequest {

    /** Name for the new {@link dev.bluemedia.timechamp.model.object.ApiKey} */
    @JsonProperty("keyName")
    @NotNull(message = "field is required")
    @NotEmpty(message = "field cannot be empty")
    private String keyName;

    /** {@link Permission} for the new user */
    @JsonProperty("permission")
    @NotNull(message = "field is not supplied or invalid")
    private Permission permission;

    /** Default constructor for Jackson deserialization */
    public ApiKeyCreateRequest() {}

    /**
     * Get the name for the new {@link dev.bluemedia.timechamp.model.object.ApiKey}.
     * @return Name for the new {@link dev.bluemedia.timechamp.model.object.ApiKey}.
     */
    public String getKeyName() {
        return keyName;
    }

    /**
     * Get the {@link Permission} for the new {@link dev.bluemedia.timechamp.model.object.ApiKey}.
     * @return {@link Permission} for the new {@link dev.bluemedia.timechamp.model.object.ApiKey}.
     */
    public Permission getPermission() {
        return permission;
    }

}
