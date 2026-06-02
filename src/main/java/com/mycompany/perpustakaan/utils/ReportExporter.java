package com.mycompany.perpustakaan.utils;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mycompany.perpustakaan.api.InventoryReportRow;
import com.mycompany.perpustakaan.api.LoanReportRow;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReportExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Path exportInventory(List<InventoryReportRow> rows, String format, Path outputDirectory) throws IOException {
        Path target = buildTargetPath(outputDirectory, "laporan-inventory", format);
        if ("pdf".equals(format)) {
            exportInventoryPdf(rows, target);
            return target;
        }
        exportInventoryXlsx(rows, target);
        return target;
    }

    public Path exportLoans(List<LoanReportRow> rows, String format, Path outputDirectory) throws IOException {
        Path target = buildTargetPath(outputDirectory, "laporan-peminjaman", format);
        if ("pdf".equals(format)) {
            exportLoansPdf(rows, target);
            return target;
        }
        exportLoansXlsx(rows, target);
        return target;
    }

    private Path buildTargetPath(Path outputDirectory, String baseName, String format) throws IOException {
        Files.createDirectories(outputDirectory);
        String timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return outputDirectory.resolve(baseName + "-" + timestamp + "." + format);
    }

    private void exportInventoryXlsx(List<InventoryReportRow> rows, Path target) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream(target.toFile())) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Inventory");
            CellStyle headerStyle = createHeaderStyle(workbook);
            writeRow(sheet.createRow(0), headerStyle, "ID", "Kode", "Judul", "Penulis", "Penerbit", "Kategori", "Tahun", "Stok Tersedia", "Stok Total");

            int rowIndex = 1;
            for (InventoryReportRow row : rows) {
                writeRow(sheet.createRow(rowIndex++), null,
                        row.getIdBuku(),
                        row.getKodeBuku(),
                        row.getJudul(),
                        row.getPenulis(),
                        row.getPenerbit(),
                        row.getKategori(),
                        row.getTahunTerbit(),
                        row.getStokTersedia(),
                        row.getStokTotal());
            }
            autoSize(sheet, 9);
            workbook.write(outputStream);
        }
    }

    private void exportLoansXlsx(List<LoanReportRow> rows, Path target) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream outputStream = new FileOutputStream(target.toFile())) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Peminjaman");
            CellStyle headerStyle = createHeaderStyle(workbook);
            writeRow(sheet.createRow(0), headerStyle, "ID", "ID User", "Nama User", "Username", "ID Buku", "Kode Buku", "Judul Buku", "Tanggal Pinjam", "Jatuh Tempo", "Tanggal Kembali", "Status", "Denda");

            int rowIndex = 1;
            for (LoanReportRow row : rows) {
                writeRow(sheet.createRow(rowIndex++), null,
                        row.getIdPeminjaman(),
                        row.getIdUser(),
                        row.getNamaUser(),
                        row.getUsername(),
                        row.getIdBuku(),
                        row.getKodeBuku(),
                        row.getJudulBuku(),
                        formatDate(row.getTanggalPinjam()),
                        formatDate(row.getTanggalJatuhTempo()),
                        formatDate(row.getTanggalKembali()),
                        row.getStatus(),
                        row.getDenda());
            }
            autoSize(sheet, 12);
            workbook.write(outputStream);
        }
    }

    private void exportInventoryPdf(List<InventoryReportRow> rows, Path target) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(target.toFile())) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);
            document.open();
            writeTitle(document, "Laporan Inventory Buku");

            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            writePdfHeader(table, "ID", "Kode", "Judul", "Penulis", "Penerbit", "Kategori", "Tahun", "Tersedia", "Total");
            for (InventoryReportRow row : rows) {
                writePdfCells(table,
                        row.getIdBuku(),
                        row.getKodeBuku(),
                        row.getJudul(),
                        row.getPenulis(),
                        row.getPenerbit(),
                        row.getKategori(),
                        row.getTahunTerbit(),
                        row.getStokTersedia(),
                        row.getStokTotal());
            }
            document.add(table);
            document.close();
        }
    }

    private void exportLoansPdf(List<LoanReportRow> rows, Path target) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(target.toFile())) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);
            document.open();
            writeTitle(document, "Laporan Peminjaman");

            PdfPTable table = new PdfPTable(10);
            table.setWidthPercentage(100);
            writePdfHeader(table, "ID", "User", "Username", "Kode", "Judul", "Pinjam", "Jatuh Tempo", "Kembali", "Status", "Denda");
            for (LoanReportRow row : rows) {
                writePdfCells(table,
                        row.getIdPeminjaman(),
                        row.getNamaUser(),
                        row.getUsername(),
                        row.getKodeBuku(),
                        row.getJudulBuku(),
                        formatDate(row.getTanggalPinjam()),
                        formatDate(row.getTanggalJatuhTempo()),
                        formatDate(row.getTanggalKembali()),
                        row.getStatus(),
                        row.getDenda());
            }
            document.add(table);
            document.close();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        font.setUnderline(org.apache.poi.ss.usermodel.Font.U_SINGLE);
        style.setFont(font);
        return style;
    }

    private void writeRow(Row row, CellStyle style, Object... values) {
        for (int index = 0; index < values.length; index++) {
            Cell cell = row.createCell(index);
            if (style != null) {
                cell.setCellStyle(style);
            }
            Object value = values[index];
            if (value instanceof Number number) {
                cell.setCellValue(number.doubleValue());
            } else {
                cell.setCellValue(value == null ? "" : value.toString());
            }
        }
    }

    private void autoSize(org.apache.poi.ss.usermodel.Sheet sheet, int columns) {
        for (int index = 0; index < columns; index++) {
            sheet.autoSizeColumn(index);
        }
    }

    private void writeTitle(Document document, String title) {
        Paragraph paragraph = new Paragraph(title, new Font(Font.HELVETICA, 14, Font.BOLD));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(12);
        document.add(paragraph);
    }

    private void writePdfHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, new Font(Font.HELVETICA, 9, Font.BOLD)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private void writePdfCells(PdfPTable table, Object... values) {
        for (Object value : values) {
            table.addCell(new Phrase(formatValue(value), new Font(Font.HELVETICA, 8)));
        }
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : DATE_FORMATTER.format(date);
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal.toPlainString();
        }
        return value.toString();
    }
}
