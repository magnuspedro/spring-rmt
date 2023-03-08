package br.com.metrics.metricsagent.qualityAttributes;

import br.com.messages.members.metrics.QualityAttributeResult;
import br.com.messages.projects.Project;

import java.util.Collection;

public interface QualityAttributesProcessor {

	Collection<QualityAttributeResult> extract(Project project, Project refactoredProject);

}
