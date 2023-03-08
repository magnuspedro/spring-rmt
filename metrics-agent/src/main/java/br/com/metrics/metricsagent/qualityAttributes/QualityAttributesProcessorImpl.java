package br.com.metrics.metricsagent.qualityAttributes;

import br.com.messages.members.metrics.QualityAttributeResult;
import br.com.messages.projects.Project;
import br.com.metrics.metricsagent.domain.metrics.Metric;
import br.com.metrics.metricsagent.domain.qualityAttributes.QualityAttribute;
import br.com.metrics.metricsagent.domain.qualityAttributes.QualityAttributeMetric;
import br.com.metrics.metricsagent.qualityAttributes.forks.QualityAttributeProcessorFork;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.zeroturnaround.zip.ZipUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
@RequiredArgsConstructor
public class QualityAttributesProcessorImpl implements QualityAttributesProcessor {

	private final List<QualityAttributeProcessorFork> qualityAttributeForks;

	public QualityAttributesProcessorImpl() {
		this.qualityAttributeForks = new ArrayList<>();
	}


	@Override
	public Collection<QualityAttributeResult> extract(Project project, Project refactoredProject) {
		try {

			final Map<Metric, Integer> projectMetricsResult = getMetrics(project);

			final Map<Metric, Integer> refactoredProjectMetricsResult = getMetrics(refactoredProject);

			return extract(projectMetricsResult, refactoredProjectMetricsResult);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Collection<QualityAttributeResult> extract(Map<Metric, Integer> projectMetricsResult,
			Map<Metric, Integer> refactoredProjectMetricsResult) {

		final List<QualityAttributeResult> qualityAttributes = new ArrayList<>();
		for (QualityAttribute qualityAttribute : QualityAttribute.values()) {
			final BigDecimal result = calculateQualityAttributeResult(qualityAttribute.getQualityAttributeMetric(),
					projectMetricsResult, refactoredProjectMetricsResult);

			qualityAttributes.add(wrapResult(qualityAttribute, result));
		}

		return qualityAttributes;
	}

	private QualityAttributeResult wrapResult(QualityAttribute qualityAttribute, BigDecimal result) {
		return new QualityAttributeResult() {
			@Override
			public String getQualityAttributeName() {
				return qualityAttribute.name();
			}

			@Override
			public BigDecimal getChangePercentage() {
				return result;
			}
		};
	}

	private BigDecimal calculateQualityAttributeResult(Collection<QualityAttributeMetric> qaMetrics,
			Map<Metric, Integer> projectMetricsResult, Map<Metric, Integer> refactoredProjectMetricsResult) {

		final Map<Metric, BigDecimal> overallResults = new HashMap<>();
		for (QualityAttributeMetric m : qaMetrics) {

			final int originalMetric = projectMetricsResult.get(m.getMetrics());
			final int refactoredMetric = refactoredProjectMetricsResult.get(m.getMetrics());

			final BigDecimal result = Optional
					.ofNullable(m.getProportion().calculateResult(originalMetric, refactoredMetric))
					.orElse(BigDecimal.ZERO);

			overallResults.put(m.getMetrics(), result);
		}

		final BigDecimal totalPercentage = overallResults.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		int amountOfMetricsInvolved = overallResults.keySet().size();

		final BigDecimal average = amountOfMetricsInvolved == 0 ? BigDecimal.ZERO
				: totalPercentage.divide(BigDecimal.valueOf(amountOfMetricsInvolved), RoundingMode.HALF_EVEN);

		return average;
	}

	private Map<Metric, Integer> getMetrics(Project project) throws IOException {
		final Path projectPath = this.openProject(project);

		final Map<Metric, Integer> metricsResult = new HashMap<>();
		for (final QualityAttributeProcessorFork fork : this.qualityAttributeForks) {
			final Map<Metric, Integer> metricResult = fork.process(projectPath);

			metricResult.keySet().forEach(k -> metricsResult.put(k, metricResult.get(k)));
		}

		this.removeFile(projectPath);

		return metricsResult;
	}

	private Path openProject(Project project) throws IOException {
		final Path projectPath = Paths.get(project.getId());

		try (InputStream pIs = project.getStream()) {
			this.removeFile(projectPath);
			Files.createFile(projectPath);

			try (FileOutputStream pFos = new FileOutputStream(projectPath.toFile())) {
				IOUtils.copy(pIs, pFos);
			}

			ZipUtil.explode(projectPath.toFile());

			return projectPath;
		}
	}

	private void removeFile(Path p) throws IOException {
		if (Files.exists(p)) {
			org.apache.commons.io.FileUtils.forceDelete(p.toFile());
		}
	}

}
