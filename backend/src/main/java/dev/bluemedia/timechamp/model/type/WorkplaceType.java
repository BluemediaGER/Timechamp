package dev.bluemedia.timechamp.model.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum WorkplaceType {

    OFFICE("office"),
    WORK_FROM_HOME("work_from_home"),
    CUSTOMER("customer"),
    BUSINESS_TRIP("business_trip"),
    OTHER("other");

    /**
     * Mapping between the enum values and their string representations.
     */
    private final String textValue;

    WorkplaceType(String textValue) {
        this.textValue = textValue;
    }

    /**
     * Get the string representation of the enum value.
     * @return String representation of the enum value
     */
    @JsonValue
    public String toTextValue() {
        return this.textValue;
    }

    /**
     * Get the enum value that represents the given string.
     * @param textValue String value you want to get the enum value for.
     * @return Enum value that represents the given string.
     * @throws IllegalArgumentException if the given string could not be matched to any value.
     */
    @JsonCreator
    public static WorkplaceType fromTextValue(String textValue) {
        for (WorkplaceType type : WorkplaceType.values()) {
            if (type.textValue.equals(textValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant for text value " + textValue);
    }

}
