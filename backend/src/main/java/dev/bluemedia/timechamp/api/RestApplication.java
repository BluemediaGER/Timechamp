package dev.bluemedia.timechamp.api;

import dev.bluemedia.timechamp.api.provider.ObjectMapperProvider;
import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for the Timechamp REST API.
 *
 * @author Oliver Traber
 */
@ApplicationPath("/api/*")
public class RestApplication extends ResourceConfig {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(RestApplication.class.getName());

    /**
     * Create a new entry point instance and initialize all components.
     */
    public RestApplication() {
        LOG.info("Initializing API...");
        LOG.info("Registering components...");
        packages(
                "dev.bluemedia.timechamp.api.filter",
                "dev.bluemedia.timechamp.api.controller",
                "dev.bluemedia.timechamp.api.exception.mapper"
        );
        LOG.info("Components registered successfully");
        LOG.info("Registering features and providers...");
        register(MultiPartFeature.class);
        register(ObjectMapperProvider.class);
        register(JacksonFeature.class);
        LOG.info("Features and providers registered successfully");

        // Create the default api user if no users exist in the database
        LOG.info("API successfully initialized");
    }
}
