package dev.bluemedia.timechamp.api.service;

import com.blueconic.browscap.*;
import dev.bluemedia.timechamp.db.DBHelper;
import dev.bluemedia.timechamp.model.object.ApiKey;
import dev.bluemedia.timechamp.model.object.Session;
import dev.bluemedia.timechamp.model.object.User;
import dev.bluemedia.timechamp.model.request.ApiKeyCreateRequest;
import dev.bluemedia.timechamp.model.request.UserCreateRequest;
import dev.bluemedia.timechamp.api.exception.NotFoundException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;

/**
 * Service used to manage all actions needed for authentication and access management.
 *
 * @author Oliver Traber
 */
public class AuthenticationService {

    /**
     * User agent parser to parse user agents from clients.
     */
    private static UserAgentParser parser;
    static {
        try {
            parser = new UserAgentService().loadParser(Arrays.asList(BrowsCapField.DEVICE_NAME, BrowsCapField.BROWSER, BrowsCapField.PLATFORM));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * Validate the credentials send by the client.
     * @param username Username that was sent by the client.
     * @param password Password that was sent by the client.
     * @return User object matching the supplied credentials, or null otherwise.
     */
    public static User validateCredentials(String username, String password) {
        User user = DBHelper.getUserDao().getByAttributeMatch("username", username);
        if (user == null) return null;
        if (user.validatePassword(password)) {
            return user;
        }
        return null;
    }

    /**
     * Issue a new {@link Session} and persist it in the database.
     * @return Session key used by the client to identify itself.
     */
    public static String issueSession(String username, String rawUserAgent, String clientIp) {
        Capabilities uaCaps = parser.parse(rawUserAgent);
        String userAgent = uaCaps.getBrowser() + " on " + uaCaps.getPlatform();
        User user = DBHelper.getUserDao().getByAttributeMatch("username", username);
        user.resetLastLoginTime();
        DBHelper.getUserDao().update(user);
        Session session = new Session(user, userAgent, clientIp);
        DBHelper.getSessionDao().persist(session);
        return session.getSessionKey();
    }

    /**
     * Check if a session key is valid and allowed to perform REST actions.
     * @param sessionKey Session key that should be validated.
     * @return true if the session key is valid, otherwise false.
     */
    public static Session validateSession(String sessionKey) {
        if (sessionKey == null) return null;
        Session session = DBHelper.getSessionDao().getByAttributeMatch("sessionKey", sessionKey);
        if (session == null) return null;
        session.resetLasAccessTime();
        DBHelper.getSessionDao().update(session);
        return session;
    }

    /**
     * Invalidate the given session key and remove the corresponding {@link Session} from the database.
     * @param sessionKey Session key of the session that should be invalidated.
     */
    public static void invalidateSession(String sessionKey) {
        Session session = DBHelper.getSessionDao().getByAttributeMatch("sessionKey", sessionKey);
        if (session != null) {
            DBHelper.getSessionDao().delete(session);
        }
    }

    /**
     * Create a new {@link ApiKey} and persist it in the database.
     * @return Generated {@link ApiKey}.
     */
    public static ApiKey createApiKey(User parentUser, ApiKeyCreateRequest request) {
        ApiKey key = new ApiKey(request.getKeyName(), parentUser, request.getPermission());
        DBHelper.getApiKeyDao().persist(key);
        return key;
    }

    /**
     * Get an {@link ApiKey} by it's secret key.
     * @return Retrieved {@link ApiKey}.
     */
    public static ApiKey getApiKey(String authenticationHeader) {
        String authenticationKey = authenticationHeader.replace("Bearer " , "");
        return DBHelper.getApiKeyDao().getByAttributeMatch("authenticationKey", authenticationKey);
    }

    /**
     * Check if an api key is valid and allowed to perform REST actions.
     * @param authenticationHeader Authentication HTTP header containing the api key that should be validated.
     * @return true if the api key is valid, otherwise false.
     */
    public static ApiKey validateApiKey(String authenticationHeader) {
        if (authenticationHeader == null) return null;
        String authenticationKey = authenticationHeader.replace("Bearer " , "");
        ApiKey apiKey = DBHelper.getApiKeyDao().getByAttributeMatch("authenticationKey", authenticationKey);
        if (apiKey == null) return null;
        apiKey.resetLastAccessTime();
        DBHelper.getApiKeyDao().update(apiKey);
        return apiKey;
    }

    /**
     * Create a new {@link User} and persist it to the database.
     * @param request {@link UserCreateRequest} containing the details for the new user.
     * @return Newly created {@link User}.
     */
    public static User createUser(UserCreateRequest request) {
        User user = DBHelper.getUserDao().getByAttributeMatch("username", request.getUsername());
        if (user != null) throw new BadRequestException("username_already_existing");
        User newUser = new User(request.getUsername(), request.getPassword(), request.getPermission());
        DBHelper.getUserDao().persist(newUser);
        return newUser;
    }

    /**
     * Update the password of an logged in {@link User} using his session key.
     * @param password New password that should be set.
     * @return {@link User} if the change was successful.
     */
    public static User updateUserPassword(User user, String password) {
        user.updatePassword(password);
        DBHelper.getUserDao().update(user);
        return user;
    }

    /**
     * Update the password of an logged in {@link User} using his id.
     * @param userId Id of the user the password should be changed for.
     * @param password New password that should be set.
     * @return {@link User} if the change was successful.
     */
    public static User updateUserPasswordById(UUID userId, String password) {
        User user = DBHelper.getUserDao().get(userId);
        if (user == null) throw new NotFoundException("user_not_existing");
        user.updatePassword(password);
        DBHelper.getUserDao().update(user);
        return user;
    }

    /**
     * Delete an {@link User} from the database.
     * @param authenticatedUser Currently authenticated {@link User}.
     * @param userId Id of the {@link User} that should be deleted.
     * @return {@link Response} containing an empty array and status 200 if the operation was successful.
     */
    public static Response deleteUser(User authenticatedUser, UUID userId) {
        if (userId.equals(authenticatedUser.getId())) throw new BadRequestException("cant_delete_own_user");
        User user = DBHelper.getUserDao().get(userId);
        if (user == null) throw new NotFoundException("user_not_existing");
        try {
            DBHelper.getSessionDao().removeAllSessionsOfUser(user);
            DBHelper.getApiKeyDao().removeAllKeysOfUser(user);
        } catch (SQLException ex) {
            // Do nothing
        }
        DBHelper.getUserDao().delete(user);
        return Response.ok().entity("[]").build();
    }

}
