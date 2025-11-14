package kuit.modi.service;

import kuit.modi.domain.Diary;
import kuit.modi.domain.Location;
import kuit.modi.domain.Member;
import kuit.modi.domain.Reminder;
import kuit.modi.dto.reminder.DiaryReminderResponse;
import kuit.modi.dto.reminder.ReminderPagedResponse;
import kuit.modi.dto.reminder.ReminderQueryParams;
import kuit.modi.dto.reminder.ReminderResponse;
import kuit.modi.repository.DiaryRepository;
import kuit.modi.repository.LocationRepository;
import kuit.modi.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {
    private final LocationRepository locationRepository;
    private final DiaryRepository diaryRepository;
    private final ReminderRepository reminderRepository;

    private static final String DEFAULT_THUMBNAIL = "https://cdn.modi.com/diary/default-thumb.jpg";

    public ReminderResponse createReminder(Member member, String address) {
        // 주소 → Location 조회
        Location location = locationRepository.findByAddress(address)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소가 존재하지 않습니다."));

        // 해당 위치에서 사용자가 작성한 다이어리 최신 1개 조회
        List<Diary> diaries =
                diaryRepository.findByMemberAndLocationOrderByDateDesc(member, location);

        if (diaries.isEmpty()) {
            throw new IllegalArgumentException("해당 위치에 대한 다이어리 기록이 없습니다.");
        }

        Diary latest = diaries.get(0);

        // Reminder 생성 및 저장
        Reminder reminder = new Reminder(
                null,
                null,
                location,
                latest.getDate(),
                latest.getEmotion(),
                member
        );
        reminderRepository.save(reminder);

        // 응답 생성
        return new ReminderResponse(reminder.getId(),
                reminder.getCreatedAt(),
                reminder.getLocation().getAddress(),
                reminder.getLastVisit(),
                reminder.getEmotion().getName());
    }

    public ReminderPagedResponse getRemindersByAddress(
            Member member,
            ReminderQueryParams params
    ) {
        // 1. address → Location 조회
        Location location = locationRepository.findByAddress(params.address())
                .orElse(null);

        if (location == null) {
            return new ReminderPagedResponse(List.of(), null);
        }

        // 2. limit 기본값 설정
        int limit = (params.limit() == null) ? 20 : params.limit();
        limit = Math.min(limit, 100);

        // pageable 준비 (+1개 더 조회)
        Pageable pageable = PageRequest.of(0, limit + 1);

        // 3. cursor 디코딩
        Long cursorId = null;
        Instant cursorCreatedAt = null;

        if (params.cursor() != null) {
            try {
                String decoded = new String(Base64.getDecoder().decode(params.cursor()));
                JSONObject json = new JSONObject(decoded);
                cursorId = json.getLong("id");
                cursorCreatedAt = Instant.parse(json.getString("created_at"));
            } catch (Exception e) {
                // 잘못된 cursor는 무시
            }
        }

        // 4. DB 조회
        List<Diary> diaries = diaryRepository.findPagedDiaries(
                member.getId(),
                location.getId(),
                cursorCreatedAt,
                cursorId,
                pageable
        );

        // 5. nextCursor 생성
        String nextCursor = null;
        if (diaries.size() > limit) {
            Diary last = diaries.get(limit);

            JSONObject json = new JSONObject();
            json.put("id", last.getId());
            json.put("created_at", last.getCreatedAt().toString());

            nextCursor = Base64.getEncoder().encodeToString(json.toString().getBytes());

            diaries = diaries.subList(0, limit);
        }

        // 6. items 변환
        List<DiaryReminderResponse> items = diaries.stream()
                .map(diary -> new DiaryReminderResponse(
                        diary.getId(),
                        diary.getDate(),
                        diary.getImage() != null ? diary.getImage().getUrl() : DEFAULT_THUMBNAIL,
                        diary.getCreatedAt()
                ))
                .toList();

        // 7. 반환
        return new ReminderPagedResponse(items, nextCursor);
    }

    public List<ReminderResponse> getRecentReminders(Member member) {
        LocalDateTime aWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<Reminder> reminders = reminderRepository.findByMemberAndCreatedAtAfterOrderByCreatedAtDesc(member, aWeekAgo);

        return reminders.stream()
                .map(reminder -> new ReminderResponse(
                        reminder.getId(),
                        reminder.getCreatedAt(),
                        reminder.getLocation().getAddress(),
                        reminder.getLastVisit(),
                        reminder.getEmotion().getName()
                ))
                .collect(Collectors.toList());
    }
}
