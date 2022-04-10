package dev.bluemedia.timechamp.model.object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import dev.bluemedia.timechamp.db.DBHelper;
import dev.bluemedia.timechamp.model.type.Permission;
import dev.bluemedia.timechamp.util.RandomString;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Session object model that holds all information about an REST api session including the sessions key.
 *
 * @author Oliver Traber
 */
@DatabaseTable(tableName = "sessions")
public class Session {

    /** Instance of the {@link RandomString} used to generate the session keys */
    private static final RandomString random;

    // Init SecureRandom
    static {
        random = new RandomString(128, RandomString.UPPER_CASE + RandomString.LOWER_CASE + RandomString.DIGITS);
    }

    /** Internal id of the session */
    @DatabaseField(id = true)
    @JsonProperty("_id")
    private UUID id;

    /** Key used by the client to authenticate itself */
    @DatabaseField(uniqueIndex = true)
    @JsonIgnore
    private String sessionKey;

    /** {@link User} this {@link Session} was created for */
    @DatabaseField(columnName = "parentUser", foreign = true, canBeNull = false, index = true)
    @JsonIgnore
    private User parentUser;

    /** {@link Permission} of the parent {@link User} for faster access */
    @DatabaseField
    @JsonIgnore
    private Permission permission;

    /** Human friendly user agent string that created the session */
    @DatabaseField
    private String userAgent;

    /** IP address last used to gain access using this session */
    @DatabaseField
    private String lastAccessIpAddress;

    /** {@link LocalDateTime} when the session was last used to make an api call */
    @DatabaseField
    private Timestamp lastAccessTime;

    /** Default constructor for OrmLite */
    private Session() {}

    /**
     * Create an new instance and generate an random session id and an random session key.
     */
    public Session(User parentUser, String userAgent, String clientIP) {
        this.id = UUID.randomUUID();
        this.sessionKey = random.nextString();
        this.lastAccessTime = Timestamp.valueOf(LocalDateTime.now());
        this.parentUser = parentUser;
        this.permission = parentUser.getPermission();
        this.userAgent = userAgent;
        this.lastAccessIpAddress = clientIP;
    }

    /**
     * Get the session's id.
     * @return The session's id.
     */
    @JsonProperty("_id")
    public UUID getSessionId() {
        return id;
    }

    /**
     * Get the {@link LocalDateTime} the session was last used to make an api call.
     * @return {@link LocalDateTime} the session was last used to make an api call.
     */
    public LocalDateTime getLastAccessTime() {
        return lastAccessTime.toLocalDateTime();
    }

    /**
     * Get the session key used by the client to authenticate itself.
     * @return Session key used by the client to authenticate itself.
     */
    @JsonIgnore
    public String getSessionKey() {
        return sessionKey;
    }

    /**
     * Get the parent {@link User} of the session.
     * @return Parent {@link User} of the session.
     */
    @JsonIgnore
    public User getParentUser() {
        return parentUser;
    }

    /**
     * Get the {@link Permission} of the parent {@link User} of the session.
     * @return {@link Permission} of the parent {@link User} of the session.
     */
    @JsonIgnore
    public Permission getPermission() {
        return permission;
    }

    /**
     * Reset the {@link LocalDateTime} the session was last used to make an api call to the current time.
     */
    @JsonIgnore
    public void resetLasAccessTime() {
        this.lastAccessTime = Timestamp.valueOf(LocalDateTime.now());
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getLastAccessIpAddress() {
        return lastAccessIpAddress;
    }
}
