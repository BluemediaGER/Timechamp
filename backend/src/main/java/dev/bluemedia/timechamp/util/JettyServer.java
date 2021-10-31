package dev.bluemedia.timechamp.util;

import dev.bluemedia.timechamp.api.RestApplication;
import dev.bluemedia.timechamp.model.config.Config;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class to handle actions around the embedded Jetty server.
 *
 * @author Oliver Traber
 */
public class JettyServer {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class.getName());

    /** Embedded Jetty application server */
    private static Server jetty;

    /**
     * Start the internal Jetty server. This will also try to figure out if the JAR File contains a packaged frontend.
     * When a frontend is found, jetty serves it under the web root.
     */
    public static void start(Config config) throws Exception {

        // Create embedded Jetty server
        jetty = new Server();

        HandlerList handlerList = new HandlerList();

        LOG.info("Timechamp will be serving HTTP requests on port {}", config.getHttpPort());

        // Create HttpConfiguration for HTTP
        HttpConfiguration httpConfiguration = new HttpConfiguration();
        httpConfiguration.setSecureScheme("https");
        httpConfiguration.setSecurePort(config.getHttpsPort());
        httpConfiguration.setSendServerVersion(false);

        // Create ServerConnector for HTTP
        ServerConnector http = new ServerConnector(jetty, new HttpConnectionFactory(httpConfiguration));
        http.setPort(config.getHttpPort());
        jetty.addConnector(http);

        // Activate HTTPs if timechamp.jks exists in the jar path
        String keystoreFile = ConfigUtil.getJarPath() + "/timechamp.jks";
        Path keystorePath = Paths.get(keystoreFile);
        if (Files.exists(keystorePath)) {
            LOG.info("Keystore found. Timechamp will also be available via HTTPs on port {}", config.getHttpsPort());
            initHttps(config, keystoreFile, httpConfiguration);

            // Redirect HTTP requests to HTTPs if enabled in config
            if (config.shouldRedirectHttp()) {
                LOG.info("HTTP requests will automatically be redirected to HTTPs");
                SecuredRedirectHandler securedHandler = new SecuredRedirectHandler();
                handlerList.addHandler(securedHandler);
            }
        }

        // Create ServletContextHandler to combine multiple servlets
        ServletContextHandler srvCtxHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        srvCtxHandler.setContextPath("/*");

        // Figure out what path to serve the frontend from
        // Get a file from the frontend folder, as ClassLoader.getResource() is not
        // designed to look for directories (we resolve the directory later)
        URL webRootLocation = JettyServer.class.getResource("/static/index.html");
        if (webRootLocation == null) {
            LOG.warn("Unable to determine frontend location in JAR file. " +
                    "This may be ok, if you are running the backend without a packaged frontend.");
        } else {
            // Resolve file to directory
            URI webRootUri = URI.create(
                    webRootLocation.toURI().toASCIIString().replaceFirst("/index.html$", "/")
            );

            // Set frontend settings to main ServletContextHandler
            srvCtxHandler.setBaseResource(Resource.newResource(webRootUri));
            srvCtxHandler.setWelcomeFiles(new String[]{"index.html"});
        }

        // Create the servlet that handles the rest api
        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(new RestApplication()));
        srvCtxHandler.addServlet(jerseyServlet, "/api/*");

        // Lastly, the default servlet for serving frontend files.
        // It is important that this is last.
        ServletHolder frontendHolder = new ServletHolder("default", DefaultServlet.class);
        frontendHolder.setInitParameter("dirAllowed","false");
        frontendHolder.setInitParameter("pathInfoOnly","true");
        srvCtxHandler.addServlet(frontendHolder,"/*");

        handlerList.addHandler(srvCtxHandler);

        jetty.setHandler(handlerList);

        // Start the server thread
        jetty.start();
        LOG.info("Web server started successfully");
    }

    /**
     * Enable HTTPs on Jetty using the given keystore.
     * @param config Config to get relevant settings, like the keystore password and the HTTPS port.
     * @param keystoreFile Path to the keystore file.
     * @param httpConfiguration HttpConfiguration used as a base for the HTTPs configuration.
     */
    private static void initHttps(Config config, String keystoreFile, HttpConfiguration httpConfiguration) {
        // Create SslContextFactory for HTTPs requests
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keystoreFile);
        sslContextFactory.setKeyStorePassword(config.getKeystorePassword());

        // Create HttpConfiguration for HTTPs
        HttpConfiguration httpsConfiguration = new HttpConfiguration(httpConfiguration);
        httpsConfiguration.addCustomizer(new SecureRequestCustomizer());

        // Create ServerConnector for HTTPs
        ServerConnector httpsConnector = new ServerConnector(jetty,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfiguration));
        httpsConnector.setPort(config.getHttpsPort());
        jetty.addConnector(httpsConnector);
    }

    /**
     * Cleanly shutdown the embedded Jetty.
     */
    public static void stop() {
        try {
            jetty.stop();
        } catch (Exception ex) {
            // fail silently
        }
    }

}
