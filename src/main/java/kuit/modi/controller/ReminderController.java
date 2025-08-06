package kuit.modi.controller;

import kuit.modi.domain.Member;
import kuit.modi.dto.reminder.DiaryReminderResponse;
import kuit.modi.dto.reminder.RecentReminderResponse;
import kuit.modi.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {
    private final ReminderService reminderService;

    @GetMapping
    public ResponseEntity<List<DiaryReminderResponse>> getRemindersByAddress(
            @RequestParam String address,
            @AuthenticationPrincipal Member member) {
        List<DiaryReminderResponse> reminders = reminderService.getRemindersByAddress(member, address);
        return ResponseEntity.ok(reminders);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentReminderResponse>> getRecentReminders(
            @AuthenticationPrincipal Member member) {
        List<RecentReminderResponse> reminders = reminderService.getRecentReminders(member);
        return ResponseEntity.ok(reminders);
    }
}
