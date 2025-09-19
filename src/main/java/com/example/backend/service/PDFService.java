package com.example.backend.service;

import com.example.backend.dto.response.ReportDataResponse;
import com.itextpdf.html2pdf.HtmlConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PDFService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generatePDFReport(ReportDataResponse reportData) throws IOException {
        String htmlContent = generateHTMLContent(reportData);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(htmlContent, outputStream);
        
        return outputStream.toByteArray();
    }

    private String generateHTMLContent(ReportDataResponse reportData) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Báo Cáo Tài Chính</title>");
        html.append("<style>");
        html.append(getCSSStyles());
        html.append("</style>");
        html.append("</head><body>");
        
        // Header
        html.append(createHeader(reportData));
        
        // Summary section
        html.append(createSummarySection(reportData));
        
        // Category stats section
        html.append(createCategoryStatsSection(reportData));
        
        // Wallet stats section
        html.append(createWalletStatsSection(reportData));
        
        // Transaction details section
        html.append(createTransactionDetailsSection(reportData));
        
        html.append("</body></html>");
        
        return html.toString();
    }

    private String createHeader(ReportDataResponse reportData) {
        return String.format("""
            <div class="header">
                <h1>%s</h1>
                <div class="header-info">
                    <p><strong>Người dùng:</strong> %s</p>
                    <p><strong>Email:</strong> %s</p>
                    <p><strong>Từ ngày:</strong> %s</p>
                    <p><strong>Đến ngày:</strong> %s</p>
                    <p><strong>Ngày tạo:</strong> %s</p>
                </div>
            </div>
            """, 
            reportData.getReportTitle(),
            reportData.getUserName(),
            reportData.getUserEmail(),
            reportData.getStartDate().format(DATE_ONLY_FORMATTER),
            reportData.getEndDate().format(DATE_ONLY_FORMATTER),
            reportData.getGeneratedAt().format(DATE_FORMATTER)
        );
    }

    private String createSummarySection(ReportDataResponse reportData) {
        return String.format("""
            <div class="section">
                <h2>Tổng Quan Tài Chính</h2>
                <div class="summary-grid">
                    <div class="summary-item income">
                        <h3>Tổng Thu Nhập</h3>
                        <p class="amount">%s</p>
                    </div>
                    <div class="summary-item expense">
                        <h3>Tổng Chi Tiêu</h3>
                        <p class="amount">%s</p>
                    </div>
                    <div class="summary-item net">
                        <h3>Số Dư Ròng</h3>
                        <p class="amount">%s</p>
                    </div>
                    <div class="summary-item count">
                        <h3>Tổng Giao Dịch</h3>
                        <p class="amount">%d</p>
                    </div>
                </div>
            </div>
            """,
            formatCurrency(reportData.getTotalIncome()),
            formatCurrency(reportData.getTotalExpense()),
            formatCurrency(reportData.getNetAmount()),
            reportData.getTotalTransactions()
        );
    }

    private String createCategoryStatsSection(ReportDataResponse reportData) {
        if (reportData.getCategoryStats().isEmpty()) {
            return "<div class='section'><h2>Thống Kê Theo Danh Mục</h2><p>Không có dữ liệu</p></div>";
        }

        StringBuilder html = new StringBuilder();
        html.append("<div class='section'><h2>Thống Kê Theo Danh Mục</h2>");
        html.append("<table class='data-table'>");
        html.append("<thead><tr><th>Danh mục</th><th>Loại</th><th>Tổng tiền</th><th>Số GD</th><th>Tỷ lệ %</th></tr></thead>");
        html.append("<tbody>");

        for (ReportDataResponse.CategoryReportData category : reportData.getCategoryStats()) {
            html.append(String.format("""
                <tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%d</td>
                    <td>%.2f%%</td>
                </tr>
                """,
                category.getCategoryName(),
                category.getType().toString(),
                formatCurrency(category.getTotalAmount()),
                category.getTransactionCount(),
                category.getPercentage().doubleValue()
            ));
        }

        html.append("</tbody></table></div>");
        return html.toString();
    }

    private String createWalletStatsSection(ReportDataResponse reportData) {
        if (reportData.getWalletStats().isEmpty()) {
            return "<div class='section'><h2>Thống Kê Theo Ví</h2><p>Không có dữ liệu</p></div>";
        }

        StringBuilder html = new StringBuilder();
        html.append("<div class='section'><h2>Thống Kê Theo Ví</h2>");
        html.append("<table class='data-table'>");
        html.append("<thead><tr><th>Tên ví</th><th>Thu nhập</th><th>Chi tiêu</th><th>Số dư ròng</th><th>Số GD</th><th>Số dư hiện tại</th></tr></thead>");
        html.append("<tbody>");

        for (ReportDataResponse.WalletReportData wallet : reportData.getWalletStats()) {
            html.append(String.format("""
                <tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%d</td>
                    <td>%s</td>
                </tr>
                """,
                wallet.getWalletName(),
                formatCurrency(wallet.getTotalIncome()),
                formatCurrency(wallet.getTotalExpense()),
                formatCurrency(wallet.getNetAmount()),
                wallet.getTransactionCount(),
                formatCurrency(wallet.getCurrentBalance())
            ));
        }

        html.append("</tbody></table></div>");
        return html.toString();
    }

    private String createTransactionDetailsSection(ReportDataResponse reportData) {
        List<ReportDataResponse.TransactionReportData> transactions = reportData.getTransactions();
        if (transactions.isEmpty()) {
            return "<div class='section'><h2>Chi Tiết Giao Dịch</h2><p>Không có giao dịch</p></div>";
        }

        // Giới hạn 50 giao dịch gần nhất
        List<ReportDataResponse.TransactionReportData> limitedTransactions = 
            transactions.size() > 50 ? transactions.subList(0, 50) : transactions;

        StringBuilder html = new StringBuilder();
        html.append("<div class='section'><h2>Chi Tiết Giao Dịch (50 giao dịch gần nhất)</h2>");
        html.append("<table class='data-table'>");
        html.append("<thead><tr><th>Ngày</th><th>Loại</th><th>Số tiền</th><th>Mô tả</th><th>Danh mục</th><th>Ví</th></tr></thead>");
        html.append("<tbody>");

        for (ReportDataResponse.TransactionReportData transaction : limitedTransactions) {
            html.append(String.format("""
                <tr>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                    <td>%s</td>
                </tr>
                """,
                convertInstantToLocalDateTime(transaction.getDate()).format(DATE_FORMATTER),
                transaction.getType().toString(),
                formatCurrency(transaction.getAmount()),
                transaction.getDescription(),
                transaction.getCategoryName(),
                transaction.getWalletName()
            ));
        }

        html.append("</tbody></table></div>");
        return html.toString();
    }

    private String getCSSStyles() {
        return """
            body {
                font-family: 'Arial', sans-serif;
                margin: 0;
                padding: 20px;
                background-color: #f5f5f5;
            }
            .header {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                padding: 30px;
                border-radius: 10px;
                margin-bottom: 30px;
                text-align: center;
            }
            .header h1 {
                margin: 0 0 20px 0;
                font-size: 28px;
            }
            .header-info {
                display: flex;
                justify-content: space-around;
                flex-wrap: wrap;
            }
            .header-info p {
                margin: 5px 0;
                font-size: 14px;
            }
            .section {
                background: white;
                margin: 20px 0;
                padding: 25px;
                border-radius: 10px;
                box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            }
            .section h2 {
                color: #333;
                border-bottom: 3px solid #667eea;
                padding-bottom: 10px;
                margin-bottom: 20px;
            }
            .summary-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                gap: 20px;
                margin: 20px 0;
            }
            .summary-item {
                text-align: center;
                padding: 20px;
                border-radius: 8px;
                color: white;
            }
            .summary-item.income {
                background: linear-gradient(135deg, #4CAF50, #45a049);
            }
            .summary-item.expense {
                background: linear-gradient(135deg, #f44336, #d32f2f);
            }
            .summary-item.net {
                background: linear-gradient(135deg, #2196F3, #1976D2);
            }
            .summary-item.count {
                background: linear-gradient(135deg, #FF9800, #F57C00);
            }
            .summary-item h3 {
                margin: 0 0 10px 0;
                font-size: 16px;
            }
            .summary-item .amount {
                font-size: 24px;
                font-weight: bold;
                margin: 0;
            }
            .data-table {
                width: 100%;
                border-collapse: collapse;
                margin: 20px 0;
            }
            .data-table th,
            .data-table td {
                padding: 12px;
                text-align: left;
                border-bottom: 1px solid #ddd;
            }
            .data-table th {
                background-color: #f8f9fa;
                font-weight: bold;
                color: #333;
            }
            .data-table tr:hover {
                background-color: #f5f5f5;
            }
            .data-table tr:nth-child(even) {
                background-color: #f9f9f9;
            }
            """;
    }

    private String formatCurrency(java.math.BigDecimal amount) {
        return String.format("%,.0f VND", amount.doubleValue());
    }

    private LocalDateTime convertInstantToLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
