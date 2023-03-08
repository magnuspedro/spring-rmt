package br.com.metrics.metricsagent.qualityAttributes.forks;

import br.com.metrics.metricsagent.domain.metrics.Metric;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

public interface QualityAttributeProcessorFork {
	
	Collection<Metric> getMetrics();
	
	Map<Metric, Integer> process(Path projectPath);

}
