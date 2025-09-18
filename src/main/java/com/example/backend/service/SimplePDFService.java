package com.example.backend.service;

import com.example.backend.dto.response.ReportDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimplePDFService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generatePDFReport(ReportDataResponse reportData) throws IOException {
        // Tạo HTML đơn giản thay vì PDF để tránh lỗi dependencies
        String htmlContent = generateHTMLContent(reportData);
        return htmlContent.getBytes("UTF-8");
    }

    private String generateHTMLContent(ReportDataResponse reportData) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>").append(reportData.getReportTitle()).append("</title>");
        html.append("<style>");
        html.append(getCSSStyles());
        html.append("</style>");
        html.append("</head><body>");
        
        // Header
        html.append("<div class='header'>");
        html.append("<h1>").append(reportData.getReportTitle()).append("</h1>");
        html.append("<div class='report-info'>");
        html.append("<p><strong>Người dùng:</strong> ").append(reportData.getUserName()).append("</p>");
        html.append("<p><strong>Email:</strong> ").append(reportData.getUserEmail()).append("</p>");
        html.append("<p><strong>Khoảng thời gian:</strong> ")
           .append(reportData.getStartDate().format(DATE_ONLY_FORMATTER))
           .append(" - ")
           .append(reportData.getEndDate().format(DATE_ONLY_FORMATTER))
           .append("</p>");
        html.append("<p><strong>Ngày tạo báo cáo:</strong> ")
           .append(reportData.getGeneratedAt().format(DATE_FORMATTER))
           .append("</p>");
        html.append("</div>");
        html.append("</div>");
        
        // Summary section
        html.append("<div class='section'>");
        html.append("<h2>Tổng Quan Tài Chính</h2>");
        html.append("<div class='summary-grid'>");
        html.append("<div class='summary-item'>");
        html.append("<h3>Tổng Thu Nhập</h3>");
        html.append("<p class='amount income'>").append(formatCurrency(reportData.getTotalIncome())).append("</p>");
        html.append("</div>");
        html.append("<div class='summary-item'>");
        html.append("<h3>Tổng Chi Tiêu</h3>");
        html.append("<p class='amount expense'>").append(formatCurrency(reportData.getTotalExpense())).append("</p>");
        html.append("</div>");
        html.append("<div class='summary-item'>");
        html.append("<h3>Số Dư Ròng</h3>");
        html.append("<p class='amount ").append(reportData.getNetAmount().compareTo(java.math.BigDecimal.ZERO) >= 0 ? "income" : "expense").append("'>")
           .append(formatCurrency(reportData.getNetAmount())).append("</p>");
        html.append("</div>");
        html.append("<div class='summary-item'>");
        html.append("<h3>Tổng Số Giao Dịch</h3>");
        html.append("<p class='amount'>").append(reportData.getTotalTransactions()).append("</p>");
        html.append("</div>");
        html.append("</div>");
        html.append("</div>");
        
        // Transaction details (first 20 transactions)
        List<ReportDataResponse.TransactionReportData> transactions = reportData.getTransactions();
        if (!transactions.isEmpty()) {
            html.append("<div class='section'>");
            html.append("<h2>Chi Tiết Giao Dịch (20 giao dịch gần nhất)</h2>");
            html.append("<table class='data-table'>");
            html.append("<thead><tr>");
            html.append("<th>STT</th>");
            html.append("<th>Ngày</th>");
            html.append("<th>Loại</th>");
            html.append("<th>Số Tiền</th>");
            html.append("<th>Danh Mục</th>");
            html.append("<th>Ví</th>");
            html.append("<th>Mô Tả</th>");
            html.append("</tr></thead>");
            html.append("<tbody>");
            
            int limit = Math.min(20, transactions.size());
            for (int i = 0; i < limit; i++) {
                ReportDataResponse.TransactionReportData transaction = transactions.get(i);
                html.append("<tr>");
                html.append("<td>").append(i + 1).append("</td>");
                html.append("<td>").append(transaction.getDate().format(DATE_FORMATTER)).append("</td>");
                html.append("<td>").append(transaction.getType().toString()).append("</td>");
                html.append("<td class='amount ").append(transaction.getType().toString().equals("INCOME") ? "income" : "expense").append("'>")
                   .append(formatCurrency(transaction.getAmount())).append("</td>");
                html.append("<td>").append(transaction.getCategoryName()).append("</td>");
                html.append("<td>").append(transaction.getWalletName()).append("</td>");
                html.append("<td>").append(transaction.getDescription() != null ? transaction.getDescription() : "").append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody></table>");
            html.append("</div>");
        }
        
        html.append("</body></html>");
        
        return html.toString();
    }

    private String getCSSStyles() {
        return """
            body {
                font-family: 'Arial', sans-serif;
                margin: 0;
                padding: 20px;
                color: #333;
                line-height: 1.6;
            }
            
            .header {
                text-align: center;
                margin-bottom: 30px;
                border-bottom: 2px solid #4CAF50;
                padding-bottom: 20px;
            }
            
            .header h1 {
                color: #4CAF50;
                margin: 0;
                font-size: 28px;
            }
            
            .report-info {
                margin-top: 15px;
                text-align: left;
                display: inline-block;
            }
            
            .report-info p {
                margin: 5px 0;
                font-size: 14px;
            }
            
            .section {
                margin-bottom: 30px;
                page-break-inside: avoid;
            }
            
            .section h2 {
                color: #4CAF50;
                border-bottom: 1px solid #ddd;
                padding-bottom: 10px;
                margin-bottom: 20px;
            }
            
            .summary-grid {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 20px;
                margin-bottom: 20px;
            }
            
            .summary-item {
                text-align: center;
                padding: 20px;
                border: 1px solid #ddd;
                border-radius: 8px;
                background-color: #f9f9f9;
            }
            
            .summary-item h3 {
                margin: 0 0 10px 0;
                font-size: 14px;
                color: #666;
            }
            
            .amount {
                font-size: 18px;
                font-weight: bold;
                margin: 0;
            }
            
            .amount.income {
                color: #4CAF50;
            }
            
            .amount.expense {
                color: #f44336;
            }
            
            .data-table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 10px;
            }
            
            .data-table th,
            .data-table td {
                border: 1px solid #ddd;
                padding: 8px;
                text-align: left;
            }
            
            .data-table th {
                background-color: #4CAF50;
                color: white;
                font-weight: bold;
            }
            
            .data-table tr:nth-child(even) {
                background-color: #f2f2f2;
            }
            
            .data-table tr:hover {
                background-color: #f5f5f5;
            }
            """;
    }

    private String formatCurrency(java.math.BigDecimal amount) {
        return String.format("%,.0f VNĐ", amount.doubleValue());
    }
}
