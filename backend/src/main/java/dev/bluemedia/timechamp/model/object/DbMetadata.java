package dev.bluemedia.timechamp.model.object;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import dev.bluemedia.timechamp.db.persister.LocalDateTimePersister;

import java.time.LocalDateTime;

@DatabaseTable(tableName = "meta")
public class DbMetadata {

    @DatabaseField(id = true)
    private String id;

    @DatabaseField
    private long schemaVersion;

    @DatabaseField(persisterClass = LocalDateTimePersister.class)
    private LocalDateTime migrationDate;

    private DbMetadata() {}

    public DbMetadata(String id, long schemaVersion, LocalDateTime migrationDate) {
        this.id = id;
        this.schemaVersion = schemaVersion;
        this.migrationDate = migrationDate;
    }

    public String getId() {
        return id;
    }

    public long getSchemaVersion() {
        return schemaVersion;
    }

    public LocalDateTime getMigrationDate() {
        return migrationDate;
    }

}
