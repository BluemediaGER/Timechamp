package dev.bluemedia.timechamp.db.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import dev.bluemedia.timechamp.model.object.ApiKey;
import dev.bluemedia.timechamp.model.object.User;
import dev.bluemedia.timechamp.model.object.UserSettings;

import java.sql.SQLException;
import java.util.UUID;

/**
 * DAO to access and manipulate {@link UserSettings} objects.
 *
 * @author Oliver Traber
 */
public class UserSettingsDaoImpl extends GenericDao<UserSettings> {

    /**
     * Default constructor to instantiate this class.
     *
     * @param dao {@link Dao} that should be used for database operations.
     */
    public UserSettingsDaoImpl(Dao<UserSettings, UUID> dao) {
        super(dao);
    }

    /**
     * Get all {@link ApiKey} objects that belong to the specified user.
     * @param parentUser User whose keys should be retrieved.
     * @return List of {@link ApiKey} objects which belong to the specified user.
     */
    public UserSettings getByParentUser(User parentUser) throws SQLException {
        return getByAttributeMatch("parentUser", parentUser);
    }

    public int removeSettingsOfUser(User user) throws SQLException {
        DeleteBuilder<UserSettings, UUID> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where().eq("parentUser", user);
        return deleteBuilder.delete();
    }

}
