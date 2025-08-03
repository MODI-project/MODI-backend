package kuit.modi.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import kuit.modi.domain.CharacterType;
import kuit.modi.domain.Member;
import kuit.modi.dto.member.MemberRequest;
import kuit.modi.repository.CharacterTypeRepository;
import kuit.modi.repository.DiaryRepository;
import kuit.modi.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final CharacterTypeRepository characterTypeRepository;
    private final DiaryRepository diaryRepository;

    public Member update(Long memberId, MemberRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        CharacterType characterType = characterTypeRepository.findByName(request.getCharacter())
                .orElseThrow(() -> new IllegalArgumentException("잘못된 캐릭터 유형입니다."));

        member.setNickname(request.getNickname());
        member.setCharacterType(characterType);

        return memberRepository.save(member);
    }

    public Optional<Member> findById(Long memberId) {
        return memberRepository.findById(memberId);
    }

    @Transactional
    public void deleteById(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new EntityNotFoundException("회원을 찾을 수 없습니다.");
        }

        // 해당 멤버가 작성한 일기 모두 삭제 후 멤버 삭제
        diaryRepository.deleteAllByMemberId(memberId);
        memberRepository.deleteById(memberId);
    }

}
