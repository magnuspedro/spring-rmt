package br.com.detection.detectionagent.domain.identity;

import br.com.messages.members.Member;
import br.com.messages.members.MemberType;

import java.util.UUID;

public class Identity {

    public static final String ID = UUID.randomUUID().toString();

    public static final String HOST = "localhost";


    public static String PORT = "8081";

    private static final String AGENT_PATH = "detection-agent";

    public static Member getAsMember() {
        return new Member(Identity.ID, Identity.HOST, Identity.PORT, MemberType.PATTERNS_SPOTS_DETECTOR);
    }

}
