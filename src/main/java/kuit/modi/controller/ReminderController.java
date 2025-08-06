package kuit.modi.controller;

import kuit.modi.dto.reminder.DiaryReminderResponse;
import kuit.modi.dto.reminder.RecentReminderResponse;
import kuit.modi.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<DiaryReminderResponse>> getRemindersByAddress(@RequestParam String address) {
        List<DiaryReminderResponse> reminders = reminderService.getRemindersByAddress(address);
        return ResponseEntity.ok(reminders);
    }
/*
    @GetMapping("/recent")
    public ResponseEntity<List<RecentReminderResponse>> getRecentReminders() {
        List<RecentReminderResponse> response = reminderService.getRecentReminders();
        return ResponseEntity.ok(response);
    }

 */
}
