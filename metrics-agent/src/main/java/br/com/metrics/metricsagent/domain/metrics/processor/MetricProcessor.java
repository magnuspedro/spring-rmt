package br.com.metrics.metricsagent.domain.metrics.processor;

import br.com.metrics.metricsagent.domain.metrics.report.CKNumber;
import br.com.metrics.metricsagent.domain.metrics.report.CKReport;
import org.eclipse.jdt.core.dom.CompilationUnit;

public interface MetricProcessor {

	void execute(CompilationUnit cu, CKNumber result, CKReport report);
	
	void setResult(CKNumber result);
}
