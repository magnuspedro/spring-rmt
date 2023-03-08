package br.com.intermediary.intermediaryagent.managers.projects;

import java.util.List;

import br.com.messages.patterns.DesignPattern;

public interface EvaluatonResponseHandler {

	void handleEvaluationResponse(String projectId, List<DesignPattern> detectedPatterns);
}
