package dev.bluemedia.timechamp.model.object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.time.LocalTime;
import java.util.UUID;

@DatabaseTable(tableName = "user_settings")
public class UserSettings {

    /** Internal id of the user */
    @DatabaseField(id = true)
    @JsonIgnore
    private UUID id;

    /** Username of the user */
    @DatabaseField(columnName = "parentUser", foreign = true, canBeNull = false, index = true)
    @JsonIgnore
    private User parentUser;

    private float hoursMonday;
    private float hoursTuesday;
    private float hoursWednesday;
    private float hoursThursday;
    private float hoursFriday;
    private float hoursSaturday;
    private float hoursSunday;

    private float vacationDays;

    private boolean autoBreak;
    private int breakDurationMinutes;
    private int breakThresholdMinutes;
    private LocalTime breakStartTime;

}
