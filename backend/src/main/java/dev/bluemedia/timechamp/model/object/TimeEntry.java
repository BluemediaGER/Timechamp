package dev.bluemedia.timechamp.model.object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import dev.bluemedia.timechamp.db.persister.DurationPersister;
import dev.bluemedia.timechamp.db.persister.LocalDateTimePersister;
import dev.bluemedia.timechamp.db.persister.TimeEntryTypePersister;
import dev.bluemedia.timechamp.model.type.TimeEntryType;

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

    @DatabaseField(index = true, persisterClass = LocalDateTimePersister.class)
    @JsonIgnore
    private LocalDateTime startTime;

    @DatabaseField(persisterClass = LocalDateTimePersister.class)
    @JsonIgnore
    private LocalDateTime endTime;

    @DatabaseField(persisterClass = DurationPersister.class)
    @JsonIgnore
    private Duration worktime;

    @DatabaseField(persisterClass = TimeEntryTypePersister.class)
    private TimeEntryType type;

    @DatabaseField
    @JsonInclude(JsonInclude.Include.NON_NULL)
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

    @JsonProperty("startTime")
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @JsonProperty("endTime")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @JsonProperty("worktime")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Duration getWorktime() {
        return worktime;
    }

    public TimeEntryType getType() {
        return type;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getDescription() {
        return description;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setWorktime(Duration worktime) {
        this.worktime = worktime;
    }

    @JsonIgnore
    public void calculateWorktime() {
        if (startTime != null && endTime != null) {
            this.worktime = Duration.between(startTime, endTime);
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
