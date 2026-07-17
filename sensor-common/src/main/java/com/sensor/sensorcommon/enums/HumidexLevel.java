package com.sensor.sensorcommon.enums;

/**
 * Comfort level derived from the Humidex index.
 *
 * <pre>
 * Index range  | Level
 * -------------|-------------------
 * < 29         | NO_DISCOMFORT
 * 30 – 39      | SOME_DISCOMFORT
 * 40 – 45      | GREAT_DISCOMFORT
 * 45 – 54      | DANGEROUS
 * > 54         | HEAT_STROKE
 * </pre>
 */
public enum HumidexLevel {

    NO_DISCOMFORT("No discomfort"),
    SOME_DISCOMFORT("Some discomfort"),
    GREAT_DISCOMFORT("Great discomfort; avoid exertion"),
    DANGEROUS("Dangerous; heat cramps and heat exhaustion likely"),
    HEAT_STROKE("Heat stroke imminent");

    private final String description;

    HumidexLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Derives the {@link HumidexLevel} from a computed Humidex index value.
     *
     * @param humidex the computed Humidex index
     * @return the corresponding comfort level
     */
    public static HumidexLevel fromIndex(double humidex) {
        if (humidex < 29) {
            return NO_DISCOMFORT;
        } else if (humidex < 40) {
            return SOME_DISCOMFORT;
        } else if (humidex < 45) {
            return GREAT_DISCOMFORT;
        } else if (humidex <= 54) {
            return DANGEROUS;
        } else {
            return HEAT_STROKE;
        }
    }
}
