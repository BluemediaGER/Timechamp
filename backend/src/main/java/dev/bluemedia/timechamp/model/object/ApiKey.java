package dev.bluemedia.timechamp.model.object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import dev.bluemedia.timechamp.db.DBHelper;
import dev.bluemedia.timechamp.db.persister.PermissionPersister;
import dev.bluemedia.timechamp.model.type.Permission;
import dev.bluemedia.timechamp.util.RandomString;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ApiKey object model that holds all information about an api key including the authentication key.
 *
 * @author Oliver Traber
 */
@DatabaseTable(tableName = "api_keys")
public class ApiKey {

    /** Instance of the {@link RandomString} used to generate the session keys */
    private static final RandomString random = new RandomString(64,
            RandomString.UPPER_CASE + RandomString.LOWER_CASE + RandomString.DIGITS);

    /** Internal id of the session */
    @DatabaseField(id = true)
    private UUID id;

    /** Name for the api key */
    @DatabaseField
    private String keyName;

    /** Id of the user this key belongs to */
    @DatabaseField
    @JsonIgnore
    private UUID parentUserId;

    /** Key used by the client to authenticate itself */
    @DatabaseField
    @JsonIgnore
    private String authenticationKey;

    /** Permission level for the api key */
    @DatabaseField(persisterClass = PermissionPersister.class)
    public Permission permission;

    /** {@link Timestamp} the api key was last used to make an api call. */
    @DatabaseField
    private Timestamp lastAccessTime;

    /**
     * Create an new instance and generate an random key id and an random authentication key.
     */
    public ApiKey(String keyName, UUID parentUserId, Permission permission) {
        this.id = UUID.randomUUID();
        this.keyName = keyName;
        this.parentUserId = parentUserId;
        this.authenticationKey = random.nextString();
        this.lastAccessTime = Timestamp.valueOf(LocalDateTime.now());
        this.permission = permission;
    }

    /**
     * No-Arg constructor used by ORMLite
     */
    private ApiKey() {}

    /**
     * Get the api keys id.
     * @return The api keys id.
     */
    @JsonProperty("_id")
    public UUID getId() {
        return id;
    }

    /**
     * Get the api keys name.
     * @return The api keys name.
     */
    @JsonProperty("name")
    public String getKeyName() {
        return keyName;
    }

    /**
     * Get the id of the user this api key belongs to.
     * @return The id of the user this key belongs to.
     */
    @JsonIgnore
    public UUID getParentUserId() {
        return parentUserId;
    }

    @JsonIgnore
    public User getParentUser() {
        return DBHelper.getUserDao().getByAttributeMatch("id", parentUserId);
    }

    /**
     * Get the api keys authentication key.
     * @return The api keys authentication key.
     */
    @JsonIgnore
    public String getAuthenticationKey() {
        return authenticationKey;
    }

    /**
     * Get the {@link LocalDateTime} the api key was last used to make an api call.
     * @return {@link LocalDateTime} the api key was last used to make an api call.
     */
    @JsonProperty("lastUsed")
    public LocalDateTime getLastAccessTime() {
        return lastAccessTime.toLocalDateTime();
    }

    /**
     * Get the {@link Permission} of the api key.
     * @return {@link Permission} of the api key.
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * Reset the {@link LocalDateTime} the api key was last used to make an api call to the current time.
     */
    public void resetLastAccessTime() {
        this.lastAccessTime = Timestamp.valueOf(LocalDateTime.now());
    }

    /**
     * Update the {@link Permission} of the {@link ApiKey}.
     * @param permission New {@link Permission} that should be set.
     */
    @JsonIgnore
    public void updatePermission(Permission permission) {
        this.permission = permission;
    }

}
