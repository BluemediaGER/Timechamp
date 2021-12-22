package dev.bluemedia.timechamp.model.object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import dev.bluemedia.timechamp.db.persister.LocalDateTimePersister;
import dev.bluemedia.timechamp.db.persister.PermissionPersister;
import dev.bluemedia.timechamp.model.type.Permission;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * User object model that holds all information about a Timechamp user.
 *
 * @author Oliver Traber
 */
@DatabaseTable(tableName = "users")
public class User {

    /** SecureRandom for salt generation */
    @JsonIgnore
    private static final SecureRandom random = new SecureRandom();

    /** Internal id of the user */
    @DatabaseField(id = true)
    private String id;

    /** Username of the user */
    @DatabaseField(uniqueIndex = true)
    private String username;

    /** Hash of the users password */
    @DatabaseField
    @JsonIgnore
    private String passwordHash;

    /** Salt used for the users password */
    @DatabaseField
    @JsonIgnore
    private String passwordSalt;

    /** Permission level for the user */
    @DatabaseField(persisterClass = PermissionPersister.class)
    private Permission permission;

    /** Time at which the user logged in the last time */
    @DatabaseField(persisterClass = LocalDateTimePersister.class)
    private LocalDateTime lastLoginTime;

    /** Name of the user */
    @DatabaseField
    private String name;

    /** Employee id of the user */
    @DatabaseField
    private String employeeId;

    /** Department of the user */
    @DatabaseField
    private String department;

    /** Default constructor for OrmLite */
    private User() {}

    /**
     * Constructor to create a new instance of this class.
     * @param username Username for the new user.
     * @param password Password for the new user.
     * @param permission {@link Permission} for the new user.
     */
    public User(String username, String password, Permission permission) {
        this.id = UUID.randomUUID().toString();
        this.username = username;
        updatePassword(password);
        this.permission = permission;
        this.lastLoginTime = LocalDateTime.now();
    }

    /**
     * Get the {@link User} id.
     * @return The {@link User} id.
     */
    @JsonProperty("_id")
    public String getId() {
        return id;
    }

    /**
     * Get the {@link User} username.
     * @return The {@link User} username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the {@link User} {@link Permission}.
     * @return The {@link User} {@link Permission}.
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * Get the time at which the {@link User} was last logged in.
     * @return The time at which the {@link User} was last logged in.
     */
    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    /**
     * Update the {@link User} password.
     * @param password New Password that should be set.
     */
    @JsonIgnore
    public void updatePassword(String password) {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        this.passwordSalt = Base64.getEncoder().encodeToString(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            passwordHash = Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            passwordHash = "invalid";
        }
    }

    /**
     * Validate if the given password matches the stored one.
     * @param password Password that should be checked against the store one.
     * @return True if the passwords match, otherwise false.
     */
    public boolean validatePassword(String password) {
        if ("invalid".equals(passwordHash)) return false;
        KeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                Base64.getDecoder().decode(passwordSalt),
                65536,
                128
        );
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            String hash = Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
            return hash.equals(this.passwordHash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update the {@link Permission} of the {@link User}.
     * @param permission New {@link Permission} that should be set.
     */
    @JsonIgnore
    public void updatePermission(Permission permission) {
        this.permission = permission;
    }

    /**
     * Set the lastLoginTime to the current time.
     */
    @JsonIgnore
    public void resetLastLoginTime() {
        lastLoginTime = LocalDateTime.now();
    }

    /**
     * Get the {@link User} name.
     * @return The {@link User} name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the {@link User} employeeId.
     * @return The {@link User} employeeId.
     */
    public String getEmployeeId() {
        return employeeId;
    }

    /**
     * Get the {@link User} department.
     * @return The {@link User} department.
     */
    public String getDepartment() {
        return department;
    }

}
