package com.example.backend.service;

import com.example.backend.dto.request.ReportRequest;
import com.example.backend.dto.response.ReportDataResponse;
import com.example.backend.entity.ReportEmailSetting;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportEmailScheduler {

    private final ReportService reportService;
    private final PDFService pdfService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final ReportEmailSettingService settingService;

    @Value("${app.report.email.enabled:false}")
    private boolean reportEmailEnabled;

    @Value("${app.report.email.cron:0 0 8 * * *}")
    private String cronExpression;

    @Scheduled(cron = "${app.report.email.cron:0 0/5 * * * *}")
    public void dispatchReports() {
        if (!reportEmailEnabled) {
            return;
        }
        try {
            userRepository.findAll().forEach(user -> {
                try {
                    ReportEmailSetting setting = settingService.getByUserId(user.getId()).orElse(null);
                    if (setting == null) {
                        return; // Chưa cài đặt
                    }

                    LocalDateTime now = LocalDateTime.now();
                    if (setting.getSendHour() != null && setting.getSendMinute() != null) {
                        if (!(now.getHour() == setting.getSendHour() && now.getMinute() == setting.getSendMinute())) {
                            return; // Không phải thời gian gửi
                        }
                    }

                    // Daily
                    if (setting.isDailyEnabled()) {
                        LocalDateTime start = now.minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                        LocalDateTime end = now.minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(0);
                        sendReport(user.getId(), setting.getTargetEmail() != null ? setting.getTargetEmail() : user.getEmail(), start, end,
                                "Báo cáo tài chính ngày " + start.toLocalDate(),
                                "Đính kèm là báo cáo tài chính ngày " + start.toLocalDate() + ".");
                    }

                    // Weekly
                    if (setting.isWeeklyEnabled() && setting.getWeeklyDayOfWeek() != null && now.getDayOfWeek().getValue() == setting.getWeeklyDayOfWeek()) {
                        LocalDateTime start = now.minusWeeks(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                        LocalDateTime end = now.minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(0);
                        sendReport(user.getId(), setting.getTargetEmail() != null ? setting.getTargetEmail() : user.getEmail(), start, end,
                                "Báo cáo tài chính tuần qua",
                                "Đính kèm là báo cáo tài chính tuần qua.");
                    }

                    // Monthly
                    if (setting.isMonthlyEnabled() && setting.getMonthlyDayOfMonth() != null && now.getDayOfMonth() == setting.getMonthlyDayOfMonth()) {
                        LocalDateTime start = now.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                        LocalDateTime end = start.plusMonths(1).minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(0);
                        sendReport(user.getId(), setting.getTargetEmail() != null ? setting.getTargetEmail() : user.getEmail(), start, end,
                                "Báo cáo tài chính tháng",
                                "Đính kèm là báo cáo tài chính tháng.");
                    }
                } catch (Exception e) {
                    log.error("Lỗi khi gửi báo cáo cho user {}: {}", user.getId(), e.getMessage(), e);
                }
            });
        } catch (Exception ex) {
            log.error("Lỗi scheduler gửi báo cáo: {}", ex.getMessage(), ex);
        }
    }

    private void sendReport(Long userId, String toEmail, LocalDateTime start, LocalDateTime end, String subject, String body) throws Exception {
        ReportRequest request = new ReportRequest();
        request.setStartDate(start);
        request.setEndDate(end);
        request.setReportType("PDF");
        request.setReportFormat("DETAILED");

        ReportDataResponse data = reportService.generateReportData(request, userId);
        byte[] pdf = pdfService.generatePDFReport(data);
        String fileName = "BaoCaoTaiChinh_" + start.toLocalDate() + "_" + end.toLocalDate() + ".pdf";
        emailService.sendEmailWithAttachment(toEmail, subject, body, pdf, fileName, "application/pdf");
    }
}
