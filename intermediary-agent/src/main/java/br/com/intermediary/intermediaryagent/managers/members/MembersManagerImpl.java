package br.com.intermediary.intermediaryagent.managers.members;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


import br.com.intermediary.intermediaryagent.members.AliveMember;
import br.com.intermediary.intermediaryagent.managers.members.exceptions.pulses.PulseException;
import br.com.messages.members.Member;
import br.com.messages.members.MemberType;
import br.com.messages.members.detectors.methods.Reference;
import br.com.messages.pulses.Pulse;
import br.com.messages.requests.pulses.exceptions.PulseExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MembersManagerImpl implements MembersManager {
	private final Map<MemberType, List<AliveMember>> members;

	private final DetectorRequestsManager detectorRequests;

	@Scheduled(fixedRate = 60000)
	public void checkExpiredMembers() {
		synchronized (members) {
			final LocalDateTime current = LocalDateTime.now();

			final List<AliveMember> dueMembers = members.values().stream().flatMap(Collection::stream)
					.filter(al -> al.isExpired(current)).toList();

			for (MemberType mt : members.keySet()) {
				members.get(mt).removeAll(dueMembers);
			}
		}
	}

	@Override
	public void renewAvailability(Pulse pulse) throws PulseException {
		synchronized (members) {

			if (!this.members.containsKey(pulse.getMemberType())) {
				throw new PulseException(PulseExceptionCode.UNREGISTERED_MEMBER.name());
			}

			if (!this.members.get(pulse.getMemberType()).stream()
					.anyMatch(al -> al.getMemberId().equals(pulse.getMemberId()))) {
				throw new PulseException(PulseExceptionCode.UNREGISTERED_MEMBER.name());
			}

			this.members.get(pulse.getMemberType()).stream().filter(al -> al.getMemberId().equals(pulse.getMemberId()))
					.findFirst().ifPresent(AliveMember::renew);
		}
	}

	@Override
	public void register(Member member) {
		synchronized (members) {

			if (!this.members.containsKey(member.getMemberType())) {
				this.members.put(member.getMemberType(), new ArrayList<>());
			}

			this.members.get(member.getMemberType()).add(new AliveMember(member));
		}
	}

	@Override
	public List<Member> getAliveMembers() {
		final List<Member> aliveMembers = new ArrayList<>();
		synchronized (members) {
			aliveMembers.addAll(this.members.values().stream().flatMap(col -> col.stream()).map(AliveMember::getMember)
					.collect(Collectors.toList()));
		}

		return aliveMembers;
	}

	@Override
	public List<Reference> getDetectorReferences(Member member) {
		return detectorRequests.getReferences(member);
	}

	@Override
	public Member getNextDetector() {
		synchronized (members) {
			return putFirstToLast(MemberType.PATTERNS_SPOTS_DETECTOR);
		}
	}

	@Override
	public Member getNextMetrics() {
		synchronized (members) {
			return putFirstToLast(MemberType.PATTERNS_METRICS_EVALUATOR);
		}
	}

	private Member putFirstToLast(MemberType memberType) {
		synchronized (members) {
			if (!this.members.containsKey(memberType) || this.members.get(memberType).isEmpty()) {
				return null;
			}

			final AliveMember aliveMember = this.members.get(memberType).remove(0);

			this.members.get(memberType).add(aliveMember);

			return aliveMember.getMember();
		}
	}

}
