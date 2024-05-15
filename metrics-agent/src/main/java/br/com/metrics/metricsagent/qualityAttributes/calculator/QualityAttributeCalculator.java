package br.com.metrics.metricsagent.qualityAttributes.calculator;

import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;
import br.com.metrics.metricsagent.metrics.Metric;

import java.math.BigDecimal;
import java.util.Map;

public interface QualityAttributeCalculator {

    QualityAttributeResult calculate(Map<Metric, BigDecimal> metrics);
}
