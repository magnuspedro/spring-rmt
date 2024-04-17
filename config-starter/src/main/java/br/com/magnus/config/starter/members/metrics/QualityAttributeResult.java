package br.com.magnus.config.starter.members.metrics;

import java.math.BigDecimal;

public interface QualityAttributeResult {
	
	String getQualityAttributeName();
	
	BigDecimal getChangePercentage();

}
