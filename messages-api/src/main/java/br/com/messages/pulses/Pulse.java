package br.com.messages.pulses;

import br.com.messages.members.MemberType;
import lombok.ToString;

import java.io.Serializable;

@ToString
public class Pulse implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String memberId;
	
	private final MemberType memberType;

	public Pulse() {
		this("", null);
	}

	public Pulse(String memberId, MemberType memberType) {
		this.memberId = memberId;
		this.memberType = memberType;
	}

	public String getMemberId() {
		return memberId;
	}

	public MemberType getMemberType() {
		return memberType;
	}

}
