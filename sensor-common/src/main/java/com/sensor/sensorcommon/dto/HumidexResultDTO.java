package com.sensor.sensorcommon.dto;

import com.sensor.sensorcommon.enums.HumidexLevel;

/**
 * Result of a Humidex index calculation.
 *
 * @param humidexIndex the computed Humidex value (rounded to two decimal places)
 * @param level        the comfort level derived from the index
 * @param description  human-readable description of the comfort level
 */
public record HumidexResultDTO(double humidexIndex, HumidexLevel level, String description) {
}
