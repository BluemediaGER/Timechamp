package dev.bluemedia.timechamp.model.object;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@DatabaseTable(tableName = "meta")
public class DbMetadata {

    @DatabaseField(id = true)
    private UUID id;

    @DatabaseField
    private long schemaVersion;

    @DatabaseField
    private Timestamp migrationDate;

    private DbMetadata() {}

    public DbMetadata(UUID id, long schemaVersion, LocalDateTime migrationDate) {
        this.id = id;
        this.schemaVersion = schemaVersion;
        this.migrationDate = Timestamp.valueOf(migrationDate);
    }

    public UUID getId() {
        return id;
    }

    public long getSchemaVersion() {
        return schemaVersion;
    }

    public LocalDateTime getMigrationDate() {
        return migrationDate.toLocalDateTime();
    }

}
