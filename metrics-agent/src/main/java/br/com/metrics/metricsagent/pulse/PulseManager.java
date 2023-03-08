package br.com.metrics.metricsagent.pulse;

import java.io.Serializable;

public interface PulseManager extends Serializable {

	void registerAsMember();
	
}
