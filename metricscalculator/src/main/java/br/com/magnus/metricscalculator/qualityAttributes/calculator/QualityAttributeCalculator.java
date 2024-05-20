package br.com.magnus.metricscalculator.qualityAttributes.calculator;

import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import br.com.magnus.metricscalculator.metrics.Metric;

import java.math.BigDecimal;
import java.util.Map;

public interface QualityAttributeCalculator {

    QualityAttributeResult calculate(Map<Metric, BigDecimal> metrics);
}
