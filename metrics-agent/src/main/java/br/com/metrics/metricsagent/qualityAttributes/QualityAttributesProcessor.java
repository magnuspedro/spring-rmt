package br.com.metrics.metricsagent.qualityAttributes;


import br.com.magnus.config.starter.members.metrics.QualityAttributeResult;

import java.nio.file.Path;
import java.util.List;

public interface QualityAttributesProcessor {

	List<QualityAttributeResult> extract(Path originalPath, Path refactoredPath);

}
