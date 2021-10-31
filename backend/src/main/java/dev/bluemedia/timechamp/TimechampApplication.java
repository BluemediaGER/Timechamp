package dev.bluemedia.timechamp;

import dev.bluemedia.timechamp.db.DBHelper;
import dev.bluemedia.timechamp.util.ConfigUtil;
import dev.bluemedia.timechamp.util.JettyServer;
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

        try {
            JettyServer.start(ConfigUtil.getConfig());
        } catch (Exception ex) {
            LOG.error("Failed to start web server.", ex);
            System.exit(1);
        }
    }

}
