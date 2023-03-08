package br.com.metrics.metricsagent.domain.identity;

import br.com.messages.members.Member;
import br.com.messages.members.MemberType;

import java.util.UUID;

public class Identity {

	public static final String ID = UUID.randomUUID().toString();

	public static final String HOST = "localhost";

	public static final String PORT = "8080";

	public static Member getAsMember() {
		return new Member(Identity.ID, Identity.HOST, Identity.PORT, MemberType.PATTERNS_METRICS_EVALUATOR);
	}

}
