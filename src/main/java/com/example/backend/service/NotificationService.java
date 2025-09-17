package com.example.backend.service;

import com.example.backend.entity.Notification;
import com.example.backend.entity.User;
import com.example.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Map<String, Long> getUnreadNotificationCount(Long userId) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return Map.of("unreadCount", count);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void createBudgetExceededNotification(User user, String categoryName) {
        String message = String.format("Cảnh báo: Bạn đã chi tiêu vượt ngân sách cho danh mục '%s'.", categoryName);

        // Xác định khoảng thời gian của tháng hiện tại
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startDate = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        // Chỉ tạo thông báo nếu chưa có thông báo tương tự trong tháng này
        if (!notificationRepository.existsByUserIdAndMessageAndCreatedAtBetween(user.getId(), message, startDate, endDate)) {
            Notification notification = Notification.builder()
                    .user(user)
                    .message(message)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        }
    }
}