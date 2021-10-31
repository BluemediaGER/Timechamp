package dev.bluemedia.timechamp.db;

import dev.bluemedia.timechamp.model.object.DbMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MigrationHelper {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(MigrationHelper.class.getName());

    /** Latest schema version this code is currently at. Increment on release! */
    private static final long LATEST_SCHEMA_VERSION = 1;

    private static final Pattern migrationFilePattern = Pattern.compile("migration-([0-9]+)\\.sql");
    private static final String migrationFilePlaceholder = "migration-%s.sql";

    protected void migrate() {
        // Check if the database is empty. Set empty database to the latest schema version,
        // because ORMLite handles the initialisation of empty databases.
        if (DBHelper.getMetadataDao().countOf() == 0) {
            DBHelper.getMetadataDao().persist(
                    new DbMetadata(UUID.randomUUID().toString(), LATEST_SCHEMA_VERSION, LocalDateTime.now())
            );
            return;
        }
        long currentSchemaVersion = DBHelper.getMetadataDao().getLatest().getSchemaVersion();
        long latestSchemaVersion = getMigrationFileVersion(getLatestMigration());
        if (currentSchemaVersion < latestSchemaVersion) {
            LOG.info("The database schema has changed and needs to be updated. " +
                    "Migration will be run automatically.");
            for (long i = currentSchemaVersion + 1; i <= latestSchemaVersion; i++) {
                String migrationFile = String.format(migrationFilePlaceholder, i);
                try {
                    runMigration(migrationFile);
                    DBHelper.getMetadataDao().persist(
                            new DbMetadata(UUID.randomUUID().toString(), i, LocalDateTime.now())
                    );
                } catch (Exception ex) {
                    LOG.error(String.format("Fatal error while migrating database. " +
                            "The error occurred at file %s", migrationFile), ex);
                    System.exit(1);
                }
            }
            LOG.info("Database migration completed successfully.");
        }
    }

    static String getLatestMigration() {
        // Resolve path to migrations folder.
        // Get a file from the migrations folder, as ClassLoader.getResource() is not
        // designed to look for directories (we resolve the directory later)
        URL migrationFolder = MigrationHelper.class.getResource("/migrations/migration-0.sql");
        if (migrationFolder == null) {
            LOG.error("Unable to determine migrations URL location");
            System.exit(1);
        }
        try {
            // Resolve file to directory
            URI path = URI.create(
                    migrationFolder.toURI().toASCIIString().replaceFirst("/migration-0.sql$", "/")
            );
            LOG.info("Migrations folder path: " + path);
            File file = new File(path);
            List<File> files = new ArrayList<>();
            if(file.isDirectory()){
                try {
                    Files.walk(file.toPath()).filter(Files::isRegularFile).forEach(f -> files.add(f.toFile()));
                } catch (IOException ex) {
                    LOG.error("Fatal error while migrating database. " +
                            "Failed to get latest database version from executable.", ex);
                    System.exit(1);
                }
            }else{
                files.add(file);
            }
            return files.get(files.size() - 1).getName();
        } catch (URISyntaxException ex) {
            LOG.error("Fatal error while migrating database. " +
                    "Failed to get migration folder path.", ex);
            System.exit(1);
            return null;
        }
    }

    private long getMigrationFileVersion(String migrationFileName) {
        LOG.info("Matcher input: " + migrationFileName);
        Matcher matcher = migrationFilePattern.matcher(migrationFileName);
        matcher.find();
        return Long.parseLong(matcher.group(1));
    }

    private ArrayList<String> getMigrationStatements(String migrationFileName) throws IOException {
        ArrayList<String> lines = new ArrayList<>();

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream resourceStream = classLoader.getResourceAsStream("migrations/" + migrationFileName);
        if (resourceStream == null) {
            LOG.error(String.format("Fatal error while migrating database. " +
                    "Requested migration file %s can't be found.", migrationFileName));
            System.exit(1);
        }
        InputStreamReader streamReader = new InputStreamReader(resourceStream);
        BufferedReader bufferedReader = new BufferedReader(streamReader);

        String line;
        while ((line = bufferedReader.readLine()) != null) {
            lines.add(line);
        }

        resourceStream.close();
        streamReader.close();
        bufferedReader.close();
        return lines;
    }

    private void runMigration(String migrationFileName) throws IOException, SQLException {
        LOG.info(String.format("Applying database migration %s...", migrationFileName));
        ArrayList<String> statements = getMigrationStatements(migrationFileName);
        for (String statement : statements) {
            DBHelper.getMetadataDao().executeRawStatement(statement);
        }
        LOG.info(String.format("Migration %s applied successfully", migrationFileName));
    }

}
