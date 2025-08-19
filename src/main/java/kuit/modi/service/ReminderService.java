package kuit.modi.service;

import jakarta.persistence.EntityNotFoundException;
import kuit.modi.domain.Diary;
import kuit.modi.domain.Location;
import kuit.modi.domain.Member;
import kuit.modi.domain.Reminder;
import kuit.modi.dto.reminder.DiaryReminderResponse;
import kuit.modi.dto.reminder.RecentReminderResponse;
import kuit.modi.repository.DiaryRepository;
import kuit.modi.repository.LocationRepository;
import kuit.modi.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {
    private final LocationRepository locationRepository;
    private final DiaryRepository diaryRepository;
    private final ReminderRepository reminderRepository;

    private static final String DEFAULT_THUMBNAIL = "https://cdn.modi.com/diary/default-thumb.jpg";

    public List<DiaryReminderResponse> getRemindersByAddress(Member member, String address) {
        // 1. 주소 → Location 엔티티 조회
        Optional<Location> optionalLocation = locationRepository.findByAddress(address);
        if (optionalLocation.isEmpty()) {
            return List.of(); // 주소 자체가 없으면 빈 리스트 반환
        }
        Location location = optionalLocation.get();

        // 2. 해당 유저가 해당 위치에 쓴 다이어리 목록 최신순 조회
        List<Diary> diaries = diaryRepository.findByMemberAndLocationOrderByDateDesc(member, location);
        if (diaries.isEmpty()) {
            return List.of(); // 기록이 없으면 리마인더도 생성하지 않음
        }

        // 3. 가장 최근 기록 기반으로 Reminder 생성 및 저장
        Diary latest = diaries.get(0);
        Reminder reminder = new Reminder(
                null,
                null,
                location,
                latest.getDate(),
                latest.getEmotion(),
                member
        );
        reminderRepository.save(reminder);

        // 4. 응답 생성
        return diaries.stream()
                .map(diary -> new DiaryReminderResponse(
                        diary.getId(),
                        diary.getDate(),
                        diary.getImage() != null ? diary.getImage().getUrl() : DEFAULT_THUMBNAIL,
                        diary.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<RecentReminderResponse> getRecentReminders(Member member) {
        LocalDateTime aWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<Reminder> reminders = reminderRepository.findByMemberAndCreatedAtAfterOrderByCreatedAtDesc(member, aWeekAgo);

        return reminders.stream()
                .map(reminder -> new RecentReminderResponse(
                        reminder.getId(),
                        reminder.getCreatedAt(),
                        reminder.getLocation().getAddress(),
                        reminder.getLastVisit(),
                        reminder.getEmotion().getName()
                ))
                .collect(Collectors.toList());
    }
}
