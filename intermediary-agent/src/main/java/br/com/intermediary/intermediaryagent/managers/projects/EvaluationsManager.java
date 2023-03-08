package br.com.intermediary.intermediaryagent.managers.projects;

import java.io.Serializable;

import br.com.messages.members.Member;

public interface EvaluationsManager extends Serializable {

	void start(EvaluatonResponseHandler responseHandler,Member member, String projectId);

}
