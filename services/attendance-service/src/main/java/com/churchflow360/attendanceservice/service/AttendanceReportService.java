package com.churchflow360.attendanceservice.service;

import com.churchflow360.attendanceservice.model.AttendanceRecord;
import com.churchflow360.attendanceservice.model.ServiceEvent;
import com.churchflow360.attendanceservice.repository.AttendanceRepository;
import com.churchflow360.attendanceservice.repository.ServiceEventRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Generates PDF attendance reports using iText 8.
 *
 * <p>Two report types:
 * <ul>
 *   <li>Service report — all attendees for a single service event</li>
 *   <li>Range report   — attendance summary across a date range</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceReportService {

    private final ServiceEventRepository serviceEventRepository;
    private final AttendanceRepository attendanceRepository;

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    private static final DateTimeFormatter DT_FMT    = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");
    private static final DeviceRgb HEADER_COLOR      = new DeviceRgb(41, 82, 163);   // church blue
    private static final DeviceRgb ALT_ROW_COLOR     = new DeviceRgb(240, 245, 255);
    private static final DeviceRgb ACCENT_COLOR      = new DeviceRgb(34, 139, 34);

    // ── Single service event report ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] generateServiceReport(UUID serviceEventId) {
        ServiceEvent event = serviceEventRepository.findById(serviceEventId)
                .orElseThrow(() -> new RuntimeException("Service event not found: " + serviceEventId));

        List<AttendanceRecord> records =
                attendanceRepository.findByServiceEventIdOrderByCheckedInAtAsc(serviceEventId);

        log.info("Generating service report for event: {} ({} attendees)",
                event.getTitle(), records.size());

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            try (PdfDocument pdf = new PdfDocument(writer);
                 Document doc = new Document(pdf)) {

            // ── Header ──────────────────────────────────────────────────────────
            addReportHeader(doc, "Attendance Report", "360 Church Flow");

            // ── Event details ────────────────────────────────────────────────────
            doc.add(new Paragraph(event.getTitle())
                    .setFontSize(16).setBold()
                    .setFontColor(HEADER_COLOR)
                    .setMarginTop(10));

            doc.add(new Paragraph(
                    event.getServiceType().name().replace("_", " ") + "  |  " +
                            event.getServiceDate().format(DATE_FMT) +
                            (event.getLocation() != null ? "  |  " + event.getLocation() : ""))
                    .setFontSize(11).setFontColor(ColorConstants.DARK_GRAY));

            // ── Summary box ──────────────────────────────────────────────────────
            Table summary = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                    .useAllAvailableWidth().setMarginTop(12).setMarginBottom(16);

            addSummaryCell(summary, "Total Attendees", String.valueOf(records.size()));
            long qrCount     = records.stream().filter(r -> r.getCheckinMethod().name().equals("QR_CODE")).count();
            long manualCount = records.size() - qrCount;
            addSummaryCell(summary, "QR Check-ins",     String.valueOf(qrCount));
            addSummaryCell(summary, "Manual Check-ins", String.valueOf(manualCount));
            doc.add(summary);

            // ── Attendee table ───────────────────────────────────────────────────
            if (records.isEmpty()) {
                doc.add(new Paragraph("No attendees recorded for this service.")
                        .setFontSize(11).setItalic().setFontColor(ColorConstants.GRAY));
            } else {
                doc.add(new Paragraph("Attendee List").setFontSize(13).setBold().setMarginBottom(6));

                Table table = new Table(UnitValue.createPercentArray(new float[]{0.5f, 3, 2, 2}))
                        .useAllAvailableWidth();

                addTableHeader(table, "#", "Member Name", "Check-in Method", "Check-in Time");

                for (int i = 0; i < records.size(); i++) {
                    AttendanceRecord r = records.get(i);
                    boolean alt = (i % 2 == 1);
                    addTableRow(table, alt,
                            String.valueOf(i + 1),
                            r.getMemberName() != null ? r.getMemberName() : r.getMembershipNumber(),
                            r.getCheckinMethod().name().replace("_", " "),
                            r.getCheckedInAt().format(DT_FMT));
                }
                doc.add(table);
            }

                addFooter(doc);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return out.toByteArray();
    }

    // ── Date range summary report ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] generateRangeReport(LocalDate from, LocalDate to) {
        List<ServiceEvent> events = serviceEventRepository.findByDateRange(from, to);

        log.info("Generating range report: {} to {} ({} services)",
                from, to, events.size());

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            try (PdfDocument pdf = new PdfDocument(writer);
                 Document doc = new Document(pdf)) {

            addReportHeader(doc, "Attendance Summary Report", "360 Church Flow");

            doc.add(new Paragraph(
                    "Period: " + from.format(DATE_FMT) + " — " + to.format(DATE_FMT))
                    .setFontSize(12).setFontColor(HEADER_COLOR).setMarginTop(8));

            // ── Overall stats ────────────────────────────────────────────────────
            long totalCheckIns = events.stream()
                    .mapToLong(e -> attendanceRepository.countByServiceEventId(e.getId()))
                    .sum();
            double average = events.isEmpty() ? 0 : (double) totalCheckIns / events.size();

            Table summary = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                    .useAllAvailableWidth().setMarginTop(12).setMarginBottom(16);
            addSummaryCell(summary, "Total Services",  String.valueOf(events.size()));
            addSummaryCell(summary, "Total Check-ins", String.valueOf(totalCheckIns));
            addSummaryCell(summary, "Avg per Service", String.format("%.1f", average));
            doc.add(summary);

            // ── Per-service breakdown ────────────────────────────────────────────
            doc.add(new Paragraph("Service Breakdown").setFontSize(13).setBold().setMarginBottom(6));

            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 1.5f}))
                    .useAllAvailableWidth();
            addTableHeader(table, "Service", "Type", "Date", "Attendance");

            for (int i = 0; i < events.size(); i++) {
                ServiceEvent e = events.get(i);
                long count = attendanceRepository.countByServiceEventId(e.getId());
                addTableRow(table, i % 2 == 1,
                        e.getTitle(),
                        e.getServiceType().name().replace("_", " "),
                        e.getServiceDate().format(DATE_FMT),
                        String.valueOf(count));
            }
            doc.add(table);
                addFooter(doc);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return out.toByteArray();
    }

    // ── PDF helpers ────────────────────────────────────────────────────────────

    private void addReportHeader(Document doc, String title, String subtitle) {
        doc.add(new Paragraph(subtitle)
                .setFontSize(10).setFontColor(ColorConstants.GRAY).setMarginBottom(2));
        doc.add(new Paragraph(title)
                .setFontSize(22).setBold().setFontColor(HEADER_COLOR).setMarginBottom(4));
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine())
                .setMarginBottom(8));
    }

    private void addSummaryCell(Table table, String label, String value) {
        Cell cell = new Cell()
                .add(new Paragraph(label).setFontSize(9).setFontColor(ColorConstants.GRAY))
                .add(new Paragraph(value).setFontSize(18).setBold().setFontColor(HEADER_COLOR))
                .setPadding(10)
                .setBackgroundColor(new DeviceRgb(235, 242, 255))
                .setBorder(null);
        table.addCell(cell);
    }

    private void addTableHeader(Table table, String... headers) {
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setFontSize(10).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(HEADER_COLOR)
                    .setPadding(6)
                    .setBorder(null));
        }
    }

    private void addTableRow(Table table, boolean alt, String... values) {
        DeviceRgb bg = alt ? ALT_ROW_COLOR : new DeviceRgb(255, 255, 255);
        for (String v : values) {
            table.addCell(new Cell()
                    .add(new Paragraph(v).setFontSize(9))
                    .setBackgroundColor(bg)
                    .setPadding(5)
                    .setBorder(null));
        }
    }

    private void addFooter(Document doc) {
        doc.add(new Paragraph(
                "Generated on: " + LocalDate.now().format(DATE_FMT) +
                        "  |  360 Church Flow Attendance System")
                .setFontSize(8).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(20));
    }
}