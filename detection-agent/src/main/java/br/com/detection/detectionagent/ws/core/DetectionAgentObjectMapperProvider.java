package br.com.detection.detectionagent.ws.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;


@Provider
public class DetectionAgentObjectMapperProvider implements ContextResolver<ObjectMapper> {
	ObjectMapper mapper;

	public DetectionAgentObjectMapperProvider() {
		mapper = new ObjectMapper();
		mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return mapper;
	}
}
