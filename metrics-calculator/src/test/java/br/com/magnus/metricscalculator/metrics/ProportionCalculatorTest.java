package br.com.magnus.metricscalculator.metrics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProportionCalculatorTest {

    @Test
    @DisplayName("Calculates inverse proportion correctly when refactored is greater than original")
    void calculatesInverseProportionCorrectlyWhenRefactoredIsGreaterThanOriginal() {
        BigDecimal result = ProportionCalculator.calculateInverse(50, 100);
        assertEquals(BigInteger.valueOf(-50), result.toBigInteger());
    }

    @Test
    @DisplayName("Calculates inverse proportion correctly when refactored is less than original")
    void calculatesInverseProportionCorrectlyWhenRefactoredIsLessThanOriginal() {
        BigDecimal result = ProportionCalculator.calculateInverse(100, 50);
        assertEquals(BigInteger.valueOf(100), result.toBigInteger());
    }

    @Test
    @DisplayName("Calculates direct proportion correctly when refactored is greater than original")
    void calculatesDirectProportionCorrectlyWhenRefactoredIsGreaterThanOriginal() {
        BigDecimal result = ProportionCalculator.calculateDirect(50, 100);
        assertEquals(BigInteger.valueOf(100), result.toBigInteger());
    }

    @Test
    @DisplayName("Calculates direct proportion correctly when refactored is less than original")
    void calculatesDirectProportionCorrectlyWhenRefactoredIsLessThanOriginal() {
        BigDecimal result = ProportionCalculator.calculateDirect(100, 50);
        assertEquals(BigInteger.valueOf(-50), result.toBigInteger());
    }
}