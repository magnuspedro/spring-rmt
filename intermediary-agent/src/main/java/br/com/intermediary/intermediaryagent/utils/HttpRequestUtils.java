package br.com.intermediary.intermediaryagent.utils;

import br.com.intermediary.intermediaryagent.ws.core.IntermediaryAgentObjectMapperProvider;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.client.ClientBuilder;

public class HttpRequestUtils {

	public static WebTarget getTarget(String host, String port, String path) {

		final String target = String.format("http://%s:%s", host, port);

		final WebTarget wt = ClientBuilder.newClient().register(IntermediaryAgentObjectMapperProvider.class).target(target).path(path);

		return wt;
	}

	public static String createPath(String... steps) {
		final StringBuilder builder = new StringBuilder();
		for (String s : steps) {
			builder.append(s);
		}
		return builder.toString();
	}
	
}
