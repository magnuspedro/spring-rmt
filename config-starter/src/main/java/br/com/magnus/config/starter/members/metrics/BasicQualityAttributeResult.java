package br.com.magnus.config.starter.members.metrics;

import java.math.BigDecimal;

public record BasicQualityAttributeResult(String qualityAttributeName,
                                          BigDecimal changePercentage) implements QualityAttributeResult {
}
