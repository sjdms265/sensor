package com.sensor.sensormcpserver.service;

import com.sensor.sensorcommon.dto.HumidexResultDTO;
import com.sensor.sensorcommon.enums.HumidexLevel;

/**
 * Stateless utility that computes the Humidex index from temperature and
 * relative humidity using the standard meteorological formula.
 *
 * <p>Formula:
 * <pre>
 *   H = T + 0.5555 × (e - 10)
 *
 *   where e = 6.11 × exp(5417.7530 × (1/273.16 − 1/(273.15 + Td)))
 *   and   Td = dew-point temperature derived from relative humidity via
 *              the Magnus approximation:
 *              Td = (243.04 × (ln(RH/100) + 17.625×T/(243.04+T)))
 *                   / (17.625 − (ln(RH/100) + 17.625×T/(243.04+T)))
 * </pre>
 */
public final class HumidexCalculator {

    private HumidexCalculator() {
        // utility class
    }

    /**
     * Computes the Humidex index and returns a {@link HumidexResultDTO}.
     *
     * @param temperatureCelsius temperature in degrees Celsius
     * @param relativeHumidity   relative humidity as a percentage (0–100)
     * @return a {@link HumidexResultDTO} with the index value and comfort level
     */
    public static HumidexResultDTO calculate(double temperatureCelsius, double relativeHumidity) {
        double dewPoint = computeDewPoint(temperatureCelsius, relativeHumidity);
        double e = 6.11 * Math.exp(5417.7530 * (1.0 / 273.16 - 1.0 / (273.15 + dewPoint)));
        double humidex = temperatureCelsius + 0.5555 * (e - 10.0);
        double rounded = Math.round(humidex * 100.0) / 100.0;

        HumidexLevel level = HumidexLevel.fromIndex(rounded);
        return new HumidexResultDTO(temperatureCelsius, relativeHumidity, rounded, level, level.getDescription());
    }

    /**
     * Magnus approximation for dew-point temperature.
     */
    private static double computeDewPoint(double temperature, double relativeHumidity) {
        final double a = 17.625;
        final double b = 243.04;
        double gamma = Math.log(relativeHumidity / 100.0) + (a * temperature) / (b + temperature);
        return (b * gamma) / (a - gamma);
    }
}
