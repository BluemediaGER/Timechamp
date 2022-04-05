package dev.bluemedia.timechamp.db;

import dev.bluemedia.timechamp.model.object.DbMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Helper class to migrate the database schema after release upgrades.
 *
 * @author Oliver Traber
 */
public class MigrationHelper {

    /** SLF4J logger for usage in this class */
    private static final Logger LOG = LoggerFactory.getLogger(MigrationHelper.class.getName());

    // Pattern and placeholders for matching / replacing migration filenames.
    private static final Pattern migrationFilePattern = Pattern.compile("migration-([0-9]+)\\.sql");
    private static final String migrationFilePlaceholder = "migration-%s.sql";

    /**
     * Check if the database needs to be migrated and automatically perform migration to the latest schema version.
     */
    protected void migrate() {
        // Check if the database is empty. Set empty database to the latest schema version,
        // because ORMLite handles the initialisation of empty databases.
        if (DBHelper.getMetadataDao().countOf() == 0) {
            long latestSchemaVersion = getMigrationFileVersion(getLatestMigration());
            DBHelper.getMetadataDao().persist(
                    new DbMetadata(UUID.randomUUID(), latestSchemaVersion, LocalDateTime.now())
            );
            return;
        }

        // Get current and latest schema version and migrate if
        long currentSchemaVersion = DBHelper.getMetadataDao().getLatest().getSchemaVersion();
        long latestSchemaVersion = getMigrationFileVersion(getLatestMigration());
        if (currentSchemaVersion < latestSchemaVersion) {
            LOG.info("The database schema has changed and needs to be updated. " +
                    "Migration will be performed automatically.");
            for (long i = currentSchemaVersion + 1; i <= latestSchemaVersion; i++) {
                String migrationFile = String.format(migrationFilePlaceholder, i);
                try {
                    runMigration(migrationFile);
                    DBHelper.getMetadataDao().persist(
                            new DbMetadata(UUID.randomUUID(), i, LocalDateTime.now())
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

    /**
     * Get the filename of the latest migration file.
     * @return Filename of the latest migration file inside the migrations/ resource folder.
     */
    static String getLatestMigration() {
        try {
            // Figure out where database migration files are stored.
            // Get a file from the migrations folder, as ClassLoader.getResource() is not
            // designed to look for directories (we resolve the directory later)
            URL migrationsFolderLocation = MigrationHelper.class.getResource("/migrations/migration-0.sql");
            if (migrationsFolderLocation == null) {
                LOG.error("Unable to determine database migration folder location. " +
                        "Unable to check if the database needs to be migrated.");
                System.exit(1);
            }

            // Resolve file to directory
            URI migrationsFolderUri = URI.create(
                    migrationsFolderLocation.toURI().toASCIIString().replaceFirst("/migration-0.sql$", "/")
            );

            // Check if migration folder resides inside a JAR file or if it's a normal file system path
            Path normalizedMigrationsPath;
            if (migrationsFolderUri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(migrationsFolderUri, Collections.emptyMap());
                normalizedMigrationsPath = fileSystem.getPath("/migrations");
            } else {
                normalizedMigrationsPath = Paths.get(migrationsFolderUri);
            }

            List<String> files = new ArrayList<>();
            // Get all paths to actual files and replace the path, so we get only the file names
            Stream<Path> migrationPaths = Files.walk(normalizedMigrationsPath, 1).filter(Files::isRegularFile);
            for (Iterator<Path> it = migrationPaths.iterator(); it.hasNext();) {
                Path migration = it.next();
                files.add(migration.toString().replace("/migrations/", ""));
            }
            // Sort the file list in descending order to normalize differences between the
            // list being built from a normal folder and from a folder inside a JAR file
            files.sort(Collections.reverseOrder());
            // Get the first file, which is also the latest migration
            return files.get(0);
        } catch (URISyntaxException | IOException ex) {
            LOG.error("Fatal error while migrating database. " +
                    "Failed to get migration folder path.", ex);
            System.exit(1);
            return null;
        }
    }

    /**
     * Get the schema version of a migration file from its file name.
     * @param migrationFileName Filename the schema version should be extracted from.
     * @return Schema version extracted from the filename.
     */
    private long getMigrationFileVersion(String migrationFileName) {
        Matcher matcher = migrationFilePattern.matcher(migrationFileName);
        matcher.find();
        return Long.parseLong(matcher.group(1));
    }

    /**
     * Read the given file and return every line in a ArrayList for further processing.
     * @param migrationFileName Filename of the migration file that should be read.
     * @return ArrayList containing all lines of the provided file.
     * @throws IOException Exception is thrown if the provided file cannot be read.
     */
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

    /**
     * Apply a given migration file by executing the contained SQL statements on the database.
     * @param migrationFileName Name of the migration file that should be applied.
     * @throws IOException Exception thrown if the given migration file cannot be read.
     * @throws SQLException Exception thrown if the execution of an SQL statement fails.
     */
    private void runMigration(String migrationFileName) throws IOException, SQLException {
        LOG.info(String.format("Applying database migration %s...", migrationFileName));
        ArrayList<String> statements = getMigrationStatements(migrationFileName);
        for (String statement : statements) {
            DBHelper.getMetadataDao().executeRawStatement(statement);
        }
        LOG.info(String.format("Migration %s applied successfully", migrationFileName));
    }

}
