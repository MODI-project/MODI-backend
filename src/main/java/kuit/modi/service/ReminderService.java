package kuit.modi.service;

import kuit.modi.domain.Diary;
import kuit.modi.dto.reminder.DiaryReminderResponse;
import kuit.modi.repository.DiaryQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReminderService {
    private final DiaryQueryRepository diaryQueryRepository;

    private static final String DEFAULT_THUMBNAIL = "https://cdn.modi.com/diary/default-thumb.jpg";

    public List<DiaryReminderResponse> getRemindersByAddress(String address) {
        List<Diary> diaries = diaryQueryRepository.findByAddress(address);

        return diaries.stream()
                .map(diary -> new DiaryReminderResponse(
                        diary.getId(),
                        diary.getDate(),
                        diary.getImage() != null ? diary.getImage().getUrl() : DEFAULT_THUMBNAIL
                ))
                .collect(Collectors.toList());
    }


}
