package kuit.modi.service;

import kuit.modi.domain.Diary;
import kuit.modi.domain.Reminder;
import kuit.modi.dto.reminder.DiaryReminderResponse;
import kuit.modi.dto.reminder.RecentReminderResponse;
import kuit.modi.repository.DiaryQueryRepository;
import kuit.modi.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {
    private final DiaryQueryRepository diaryQueryRepository;
    private final ReminderRepository reminderRepository;

    private static final String DEFAULT_THUMBNAIL = "https://cdn.modi.com/diary/default-thumb.jpg";

    public List<DiaryReminderResponse> getRemindersByAddress(String address) {
        List<Diary> diaries = diaryQueryRepository.findByAddress(address);

        return diaries.stream()
                .map(diary -> new DiaryReminderResponse(
                        diary.getId(),
                        diary.getDate(),
                        diary.getImage() != null ? diary.getImage().getUrl() : DEFAULT_THUMBNAIL,
                        diary.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<RecentReminderResponse> getRecentReminders() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<Reminder> reminders = reminderRepository.findByCreatedAtAfter(oneWeekAgo);

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
