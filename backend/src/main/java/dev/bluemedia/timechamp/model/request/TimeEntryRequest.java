package dev.bluemedia.timechamp.model.request;

import dev.bluemedia.timechamp.model.type.TimeEntryType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class TimeEntryRequest {

    @NotNull(message = "field is not supplied or invalid")
    private LocalDateTime startTime;

    @NotNull(message = "field is not supplied or invalid")
    private LocalDateTime endTime;

    @NotNull(message = "field is not supplied or invalid")
    private TimeEntryType type;

    private String description;

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public TimeEntryType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
