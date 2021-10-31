package dev.bluemedia.timechamp;

import dev.bluemedia.timechamp.db.DBHelper;
import dev.bluemedia.timechamp.model.object.User;
import dev.bluemedia.timechamp.model.type.Permission;
import dev.bluemedia.timechamp.util.ConfigUtil;
import dev.bluemedia.timechamp.util.JettyServer;
import dev.bluemedia.timechamp.util.RandomString;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for Timechamp. Perform initialisation of all components and launches the application afterwards.
 *
 * @author Oliver Traber
 */
public class TimechampApplication {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(TimechampApplication.class.getName());

    /** Global Quartz scheduler used to schedule sync tasks */
    private static Scheduler quartzScheduler;

    public static void main(String[] args) {
        // Add shutdown hook to cleanly shut down the application
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Performing clean shutdown");
            JettyServer.stop();
            if (quartzScheduler != null) {
                try {
                    quartzScheduler.shutdown();
                } catch (SchedulerException ex) {
                    LOG.error("An unexpected error occurred", ex);
                }
            }
            DBHelper.close();
        }));

        // Initialize the database helper class
        DBHelper.init(ConfigUtil.getConfig().getDatabaseJDBCUrl());

        // Create default user if the database doesn't contain any users
        if (DBHelper.getUserDao().countOf() == 0) {
            String username = "timechamp";
            String passwordSymbols = RandomString.UPPER_CASE + RandomString.LOWER_CASE + RandomString.DIGITS;
            String password = new RandomString(12, passwordSymbols).nextString();
            DBHelper.getUserDao().persist(new User(username, password, Permission.MANAGE));
            LOG.info("New default user account created. Username: {} / Password: {}", username, password);
        }

        // Start Jetty web server
        try {
            JettyServer.start(ConfigUtil.getConfig());
        } catch (Exception ex) {
            LOG.error("Failed to start web server.", ex);
            System.exit(1);
        }
    }

}
