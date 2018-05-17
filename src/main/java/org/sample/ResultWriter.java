package org.sample;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class ResultWriter {
    private static final short NORMAL_FONT_SIZE = 9;
    private static final String NORMAL_FONT_NAME = "ＭＳ ゴシック";
    private SXSSFWorkbook book;
    private OutputStream outputStream;
    private CellStyle headerStyle;
    private CellStyle normalStyle;

    public ResultWriter(String outputPath) throws IOException {
        book = new SXSSFWorkbook();
        outputStream = new FileOutputStream(outputPath);
        headerStyle = createHeaderStyle(book);
        normalStyle = createNormalStyle(book);
    }

    public void writeResult(Map<String, List<CompareResult>> resultMap) throws IOException {
        for (Map.Entry<String, List<CompareResult>> entry : resultMap.entrySet()) {
            Sheet sheet = book.createSheet(entry.getKey());
            for (int i = 0; i < 3; i++) {
                sheet.setColumnWidth(i, 60*256);
            }
            writeHeader(sheet);
            int rowIndex = 1;
            for (CompareResult compareResult : entry.getValue()) {
                String oldText = compareResult.getOldText();
                String newText = compareResult.getNewText();
                for (String description : compareResult.getDescriptions()) {
                    Row row = sheet.createRow(rowIndex++);
                    Cell a = row.createCell(0);
                    a.setCellStyle(normalStyle);
                    a.setCellValue(oldText);
                    Cell b = row.createCell(1);
                    b.setCellStyle(normalStyle);
                    b.setCellValue(newText);
                    Cell c = row.createCell(2);
                    c.setCellStyle(normalStyle);
                    c.setCellValue(description);
                }
            }
        }
    }

    public void close() throws IOException {
        if (book != null) {
            book.write(outputStream);
            book.dispose();
        }
        if (outputStream != null) {
            outputStream.close();
        }
    }

    private void writeHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        Cell aCell = header.createCell(0);
        aCell.setCellValue("old");
        aCell.setCellStyle(headerStyle);
        Cell bCell = header.createCell(1);
        bCell.setCellValue("new");
        bCell.setCellStyle(headerStyle);
        Cell cCell = header.createCell(2);
        cCell.setCellValue("description");
        cCell.setCellStyle(headerStyle);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle cellStyle = createNormalStyle(workbook);
        cellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle;
    }

    private CellStyle createNormalStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        Font font = workbook.createFont();
        font.setFontName(NORMAL_FONT_NAME);
        font.setFontHeightInPoints(NORMAL_FONT_SIZE);
        cellStyle.setFont(font);
        cellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        cellStyle.setWrapText(true);
        return cellStyle;
    }
}
