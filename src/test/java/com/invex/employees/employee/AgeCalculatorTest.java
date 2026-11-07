package com.invex.employees.employee;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class AgeCalculatorTest {

    // "Today" is 6 November 2026 for every test in this class
    private final AgeCalculator ageCalculator = new AgeCalculator(
            Clock.fixed(Instant.parse("2026-11-06T12:00:00Z"), ZoneOffset.UTC));

    @Test
    void shouldCountBirthdayThatIsToday() {
        assertThat(ageCalculator.ageOf(LocalDate.of(1990, 11, 6))).isEqualTo(36);
    }

    @Test
    void shouldNotCountBirthdayThatIsTomorrow() {
        assertThat(ageCalculator.ageOf(LocalDate.of(1990, 11, 7))).isEqualTo(35);
    }

    @Test
    void shouldHandleLeapDayBirthdays() {
        assertThat(ageCalculator.ageOf(LocalDate.of(2000, 2, 29))).isEqualTo(26);
    }

    @Test
    void shouldMatchOnlyTheRealAge() {
        assertThat(ageCalculator.matches(36, LocalDate.of(1990, 5, 15))).isTrue();
        assertThat(ageCalculator.matches(35, LocalDate.of(1990, 5, 15))).isFalse();
    }
}
