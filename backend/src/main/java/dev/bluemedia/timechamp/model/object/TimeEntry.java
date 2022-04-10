package dev.bluemedia.timechamp.model.object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import dev.bluemedia.timechamp.db.persister.DurationPersister;
import dev.bluemedia.timechamp.db.persister.TimeEntryTypePersister;
import dev.bluemedia.timechamp.model.type.TimeEntryType;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@DatabaseTable(tableName = "time_entries")
public class TimeEntry {

    @DatabaseField(id = true)
    private UUID id;

    @DatabaseField(columnName = "parentUser", foreign = true, canBeNull = false, index = true)
    @JsonIgnore
    private User parentUser;

    @DatabaseField(index = true)
    private Timestamp startTime;

    @DatabaseField
    private Timestamp endTime;

    @DatabaseField(persisterClass = DurationPersister.class)
    private Duration worktime;

    @DatabaseField(persisterClass = TimeEntryTypePersister.class)
    private TimeEntryType type;

    @DatabaseField
    private String description;

    private TimeEntry() {}

    public TimeEntry(User parentUser, TimeEntryType type) {
        this.id = UUID.randomUUID();
        this.parentUser = parentUser;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    @JsonIgnore
    public User getParentUser() {
        return parentUser;
    }

    public LocalDateTime getStartTime() {
        return startTime.toLocalDateTime();
    }

    public LocalDateTime getEndTime() {
        return endTime.toLocalDateTime();
    }

    public Duration getWorktime() {
        return worktime;
    }

    public TimeEntryType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = Timestamp.valueOf(startTime);
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = Timestamp.valueOf(endTime);
    }

    public void setWorktime(Duration worktime) {
        this.worktime = worktime;
    }

    public void calculateWorktime() {
        if (startTime != null && endTime != null) {
            this.worktime = Duration.between(startTime.toInstant(), endTime.toInstant());
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
