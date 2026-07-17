package com.sensor.sensormcpserver.service;

import com.sensor.sensorcommon.dto.HumidexResultDTO;
import com.sensor.sensorcommon.enums.HumidexLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class HumidexCalculatorTest {

    // -----------------------------------------------------------------------
    // Level boundary tests
    // -----------------------------------------------------------------------

    @Test
    void calculate_shouldReturnNoDiscomfort_whenHumidexBelow29() {
        // 15 °C, 40 % RH → Humidex well below 29
        HumidexResultDTO result = HumidexCalculator.calculate(15.0, 40.0);

        assertThat(result.level()).isEqualTo(HumidexLevel.NO_DISCOMFORT);
        assertThat(result.humidexIndex()).isLessThan(29.0);
    }

    @Test
    void calculate_shouldReturnSomeDiscomfort_whenHumidexBetween30And39() {
        // 28 °C, 45 % RH → Humidex in the 30-39 range
        HumidexResultDTO result = HumidexCalculator.calculate(28.0, 45.0);

        assertThat(result.level()).isEqualTo(HumidexLevel.SOME_DISCOMFORT);
        assertThat(result.humidexIndex()).isBetween(29.0, 39.99);
    }

    @Test
    void calculate_shouldReturnGreatDiscomfort_whenHumidexBetween40And45() {
        // 30 °C, 75 % RH → Humidex in the 40-45 range
        HumidexResultDTO result = HumidexCalculator.calculate(30.0, 75.0);

        assertThat(result.level()).isEqualTo(HumidexLevel.GREAT_DISCOMFORT);
        assertThat(result.humidexIndex()).isBetween(40.0, 45.0);
    }

    @Test
    void calculate_shouldReturnDangerous_whenHumidexBetween45And54() {
        // 33 °C, 80 % RH → Humidex in the 45-54 range
        HumidexResultDTO result = HumidexCalculator.calculate(33.0, 80.0);

        assertThat(result.level()).isEqualTo(HumidexLevel.DANGEROUS);
        assertThat(result.humidexIndex()).isBetween(45.0, 54.0);
    }

    @Test
    void calculate_shouldReturnHeatStroke_whenHumidexAbove54() {
        // 40 °C, 90 % RH → Humidex above 54
        HumidexResultDTO result = HumidexCalculator.calculate(40.0, 90.0);

        assertThat(result.level()).isEqualTo(HumidexLevel.HEAT_STROKE);
        assertThat(result.humidexIndex()).isGreaterThan(54.0);
    }

    // -----------------------------------------------------------------------
    // Description is populated
    // -----------------------------------------------------------------------

    @Test
    void calculate_shouldPopulateDescription() {
        HumidexResultDTO result = HumidexCalculator.calculate(15.0, 40.0);

        assertThat(result.description()).isNotBlank();
        assertThat(result.description()).isEqualTo(result.level().getDescription());
    }

    // -----------------------------------------------------------------------
    // Numeric precision — result is rounded to 2 decimal places
    // -----------------------------------------------------------------------

    @Test
    void calculate_shouldRoundIndexToTwoDecimalPlaces() {
        HumidexResultDTO result = HumidexCalculator.calculate(25.0, 55.0);

        String indexStr = Double.toString(result.humidexIndex());
        int dotIndex = indexStr.indexOf('.');
        int decimalPlaces = dotIndex < 0 ? 0 : indexStr.length() - dotIndex - 1;
        assertThat(decimalPlaces).isLessThanOrEqualTo(2);
    }

    // -----------------------------------------------------------------------
    // HumidexLevel.fromIndex boundary checks
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "index={0} → {1}")
    @CsvSource({
            "28.9,  NO_DISCOMFORT",
            "29.0,  SOME_DISCOMFORT",
            "39.9,  SOME_DISCOMFORT",
            "40.0,  GREAT_DISCOMFORT",
            "44.9,  GREAT_DISCOMFORT",
            "45.0,  DANGEROUS",
            "54.0,  DANGEROUS",
            "54.1,  HEAT_STROKE",
            "60.0,  HEAT_STROKE"
    })
    void humidexLevel_fromIndex_shouldMapCorrectly(double index, HumidexLevel expectedLevel) {
        assertThat(HumidexLevel.fromIndex(index)).isEqualTo(expectedLevel);
    }
}
