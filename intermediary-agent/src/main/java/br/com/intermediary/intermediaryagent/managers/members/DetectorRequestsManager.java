package br.com.intermediary.intermediaryagent.managers.members;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import br.com.intermediary.intermediaryagent.ws.core.DetectionClient;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.builder.EqualsBuilder;

import br.com.messages.members.Member;
import br.com.messages.members.MemberType;
import br.com.messages.members.candidates.RefactoringCandidadeDTO;
import br.com.messages.members.detectors.methods.Reference;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DetectorRequestsManager {

    private final DetectionClient detectionClient;

    public List<Reference> getReferences(Member member) {
        if (!new EqualsBuilder().append(MemberType.PATTERNS_SPOTS_DETECTOR, member.getMemberType()).isEquals()) {
            return Collections.emptyList();
        }
        var uri = URI.create("http://%s:%s".formatted(member.getHost(), member.getPort()));

        return detectionClient.getReferences(uri);
    }

    public List<RefactoringCandidadeDTO> startDetection(Member member, String projectId) {
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }
        var uri = URI.create("http://%s:%s".formatted(member.getHost(), member.getPort()));

        return detectionClient.detect(uri, projectId.trim());
    }

    public String startRefactoring(Member member, String projectId, List<RefactoringCandidadeDTO> candidates) {
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }
        var uri = URI.create("http://%s:%s".formatted(member.getHost(), member.getPort()));

        return detectionClient.refactor(uri, projectId.trim(), candidates);
    }

}
