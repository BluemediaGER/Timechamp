package dev.bluemedia.timechamp.model.object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import dev.bluemedia.timechamp.db.persister.DurationPersister;
import dev.bluemedia.timechamp.db.persister.LocalDateTimePersister;
import dev.bluemedia.timechamp.db.persister.TimeEntryTypePersister;
import dev.bluemedia.timechamp.db.persister.WorkplaceTypePersister;
import dev.bluemedia.timechamp.model.type.TimeEntryType;
import dev.bluemedia.timechamp.model.type.WorkplaceType;

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

    @DatabaseField(persisterClass = WorkplaceTypePersister.class)
    private WorkplaceType workplace;

    @DatabaseField
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    private TimeEntry() {}

    public TimeEntry(User parentUser, TimeEntryType type) {
        this.id = UUID.randomUUID();
        this.parentUser = parentUser;
        this.type = type;
        this.workplace = WorkplaceType.OFFICE;
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

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    @JsonProperty("endTime")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @JsonProperty("worktime")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Duration getWorktime() {
        return worktime;
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

    public TimeEntryType getType() {
        return type;
    }

    public void setType(TimeEntryType type) {
        this.type = type;
    }

    public WorkplaceType getWorkplace() {
        return workplace;
    }

    public void setWorkplace(WorkplaceType workplaceType) {
        this.workplace = workplaceType;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
