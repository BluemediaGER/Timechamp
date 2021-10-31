package dev.bluemedia.timechamp.model.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model for the main configuration file
 *
 * @author Oliver Traber
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

    /** Optional jdbc url for use with other databases like mysql */
    @JsonProperty("databaseJDBCUrl")
    private String databaseJDBCUrl;

    /** Port for HTTP on which the web application server should be listening */
    @JsonProperty(value = "httpPort")
    private int httpPort = 8080;

    /** Port for HTTPs on which the web application server should be listening */
    @JsonProperty(value = "httpsPort")
    private int httpsPort = 8443;

    /** Password for the keystore file if HTTPs should be enabled */
    @JsonProperty(value = "keystorePassword")
    private String keystorePassword = "changeit";

    /** Boolean to set if HTTP requests should be redirected to HTTPs */
    @JsonProperty(value = "redirectHttp")
    private boolean redirectHttp = true;

    /** Boolean to set if Timechamp runs behind a reverse proxy.
     * Setting this to true will enable usage of the X-Real-IP Header for session management */
    @JsonProperty(value = "reverseProxy")
    private boolean reverseProxy = false;

    /**
     * Get the JDBC url that should be used to connect to the database.
     * @return JDBC url that should be used for the database.
     */
    public String getDatabaseJDBCUrl() {
        return databaseJDBCUrl;
    }

    /**
     * Get the HTTP port on which the web application server should be listening
     * @return The HTTP port on which the web application server should be listening
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * Get the HTTPs port on which the web application server should be listening
     * @return The HTTPs port on which the web application server should be listening
     */
    public int getHttpsPort() {
        return httpsPort;
    }

    /**
     * Get the password for the keystore file if HTTPs should be enabled
     * @return The password for the keystore file if HTTPs should be enabled
     */
    public String getKeystorePassword() {
        return keystorePassword;
    }

    /**
     * Get if HTTP requests should be redirected to HTTPs.
     * @return true if HTTP requests should be redirected to HTTPs, otherwise false.
     */
    public boolean shouldRedirectHttp() {
        return redirectHttp;
    }

    /**
     * Get if Timechamp is running behind an HTTP reverse proxy.
     * @return true if Timechamp is located behind a reverse proxy, otherwise false.
     */
    public boolean isBehindReverseProxy() {
        return reverseProxy;
    }

}
