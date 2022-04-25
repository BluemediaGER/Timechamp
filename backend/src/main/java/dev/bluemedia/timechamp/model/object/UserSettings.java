package dev.bluemedia.timechamp.model.object;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import dev.bluemedia.timechamp.db.persister.LocalTimePersister;
import dev.bluemedia.timechamp.db.persister.WorkplaceTypePersister;
import dev.bluemedia.timechamp.model.type.WorkplaceType;

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

    @DatabaseField
    private float hoursMonday;

    @DatabaseField
    private float hoursTuesday;

    @DatabaseField
    private float hoursWednesday;

    @DatabaseField
    private float hoursThursday;

    @DatabaseField
    private float hoursFriday;

    @DatabaseField
    private float hoursSaturday;

    @DatabaseField
    private float hoursSunday;

    @DatabaseField
    private float vacationDays;

    @DatabaseField
    private boolean autoBreak;

    @DatabaseField
    private int breakDurationMinutes;

    @DatabaseField
    private int breakThresholdMinutes;
    @DatabaseField(persisterClass = LocalTimePersister.class)
    private LocalTime breakStartTime;

    @DatabaseField(persisterClass = WorkplaceTypePersister.class)
    private WorkplaceType defaultWorkplace;

    private UserSettings() {}

    public UserSettings(User parentUser) {
        this.id = UUID.randomUUID();
        this.parentUser = parentUser;
        this.defaultWorkplace = WorkplaceType.OFFICE;
    }

    public float getHoursMonday() {
        return hoursMonday;
    }

    public void setHoursMonday(float hoursMonday) {
        this.hoursMonday = hoursMonday;
    }

    public float getHoursTuesday() {
        return hoursTuesday;
    }

    public void setHoursTuesday(float hoursTuesday) {
        this.hoursTuesday = hoursTuesday;
    }

    public float getHoursWednesday() {
        return hoursWednesday;
    }

    public void setHoursWednesday(float hoursWednesday) {
        this.hoursWednesday = hoursWednesday;
    }

    public float getHoursThursday() {
        return hoursThursday;
    }

    public void setHoursThursday(float hoursThursday) {
        this.hoursThursday = hoursThursday;
    }

    public float getHoursFriday() {
        return hoursFriday;
    }

    public void setHoursFriday(float hoursFriday) {
        this.hoursFriday = hoursFriday;
    }

    public float getHoursSaturday() {
        return hoursSaturday;
    }

    public void setHoursSaturday(float hoursSaturday) {
        this.hoursSaturday = hoursSaturday;
    }

    public float getHoursSunday() {
        return hoursSunday;
    }

    public void setHoursSunday(float hoursSunday) {
        this.hoursSunday = hoursSunday;
    }

    public float getVacationDays() {
        return vacationDays;
    }

    public void setVacationDays(float vacationDays) {
        this.vacationDays = vacationDays;
    }

    public boolean isAutoBreak() {
        return autoBreak;
    }

    public void setAutoBreak(boolean autoBreak) {
        this.autoBreak = autoBreak;
    }

    public int getBreakDurationMinutes() {
        return breakDurationMinutes;
    }

    public void setBreakDurationMinutes(int breakDurationMinutes) {
        this.breakDurationMinutes = breakDurationMinutes;
    }

    public int getBreakThresholdMinutes() {
        return breakThresholdMinutes;
    }

    public void setBreakThresholdMinutes(int breakThresholdMinutes) {
        this.breakThresholdMinutes = breakThresholdMinutes;
    }

    public LocalTime getBreakStartTime() {
        return breakStartTime;
    }

    public void setBreakStartTime(LocalTime breakStartTime) {
        this.breakStartTime = breakStartTime;
    }

    public WorkplaceType getDefaultWorkplace() {
        return defaultWorkplace;
    }

    public void setDefaultWorkplace(WorkplaceType defaultWorkplace) {
        this.defaultWorkplace = defaultWorkplace;
    }
}
