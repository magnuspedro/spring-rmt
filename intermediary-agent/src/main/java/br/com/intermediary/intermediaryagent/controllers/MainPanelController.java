package br.com.intermediary.intermediaryagent.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import br.com.intermediary.intermediaryagent.managers.members.MembersManager;
import br.com.intermediary.intermediaryagent.managers.projects.ProjectsPool;
import br.com.messages.members.Member;
import br.com.messages.members.MemberType;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.projects.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MainPanelController implements Serializable {

	private static final long serialVersionUID = 1L;

	private final MembersManager membersManager;

	private final ProjectsPool projectsPool;

	private Member selectedMember;

	public List<Member> getMembers() {
		return this.membersManager.getAliveMembers();
	}

	public List<Project> getProjects() {
		return new ArrayList<>(this.projectsPool.getAll());
	}

	public boolean isADetectionMember(Member m) {
		return MemberType.PATTERNS_SPOTS_DETECTOR.equals(m.getMemberType());
	}

	public List<Reference> getReferences() {
		if (selectedMember == null) {
			return Collections.emptyList();
		}

		return this.membersManager.getDetectorReferences(selectedMember);
	}

	public Member getSelectedMember() {
		return selectedMember;
	}

	public void updateSelectedMember(Member selectedMember) {
		this.selectedMember = selectedMember;
	}

}
