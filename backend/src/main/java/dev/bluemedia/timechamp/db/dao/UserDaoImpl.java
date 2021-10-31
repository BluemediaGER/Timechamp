package dev.bluemedia.timechamp.db.dao;

import com.j256.ormlite.dao.Dao;
import dev.bluemedia.timechamp.model.object.User;

/**
 * DAO to access and manipulate {@link User} objects.
 *
 * @author Oliver Traber
 */
public class UserDaoImpl extends GenericDao<User> {

    /**
     * Default constructor to instantiate this class.
     *
     * @param dao {@link Dao} that should be used for database operations.
     */
    public UserDaoImpl(Dao<User, String> dao) {
        super(dao);
    }
}
