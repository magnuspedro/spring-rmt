package br.com.magnus.metricscalculator.metrics;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ProportionCalculator {

		public static BigDecimal calculateInverse(int original, int refactored) {
			return BigDecimal.valueOf(original, 2).multiply(BigDecimal.valueOf(100))
					.divide(BigDecimal.valueOf(refactored, 2), RoundingMode.HALF_EVEN)
					.subtract(BigDecimal.valueOf(100));
		}
		public static BigDecimal calculateDirect(int original, int refactored) {
			return BigDecimal.valueOf(refactored, 2).multiply(BigDecimal.valueOf(100))
					.divide(BigDecimal.valueOf(original, 2), RoundingMode.HALF_EVEN)
					.subtract(BigDecimal.valueOf(100));
		}
}
