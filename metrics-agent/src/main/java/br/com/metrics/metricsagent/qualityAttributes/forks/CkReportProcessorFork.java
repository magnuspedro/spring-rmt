package br.com.metrics.metricsagent.qualityAttributes.forks;

import br.com.metrics.metricsagent.domain.metrics.Metric;
import br.com.metrics.metricsagent.domain.metrics.report.CK;
import br.com.metrics.metricsagent.domain.metrics.report.CKReport;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CkReportProcessorFork implements QualityAttributeProcessorFork {

	private final Collection<Metric> metrics = Stream.of(Metric.values()).filter(Metric::isCkReportBased)
			.collect(Collectors.toList());

	@Override
	public Collection<Metric> getMetrics() {
		return metrics;
	}

	@Override
	public Map<Metric, Integer> process(Path projectPath) {
		final CKReport report = new CK().calculate(projectPath.toFile().getAbsolutePath());
		
		final Map<Metric, Integer> projectMetrics = new HashMap<>();
		for(Metric m : metrics) {
			projectMetrics.put(m, m.calculate(report));
		}
		
		return projectMetrics;
	}

}
