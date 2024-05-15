package br.com.metrics.metricsagent.qualityAttributes;

import br.com.metrics.metricsagent.metrics.MetricCalculator;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum QualityAttribute {
    MAINTAINABILITY,
    RELIABILITY,
    REUSABILITY;
}
