package br.com.magnus.config.starter.members.metrics;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FileMetricsTest {

    @Test
    void shouldKeepMetricEntriesInBuilder() {
        var fileMetrics = FileMetrics.builder()
                .metrics(List.of(new BasicQualityAttributeResult("MAINTAINABILITY", BigDecimal.TEN)))
                .build();

        assertThat(fileMetrics.getMetrics())
                .extracting(BasicQualityAttributeResult::qualityAttributeName)
                .containsExactly("MAINTAINABILITY");
    }
}
