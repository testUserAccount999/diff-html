package org.sample;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultWriter.class);
    private static final short NORMAL_FONT_SIZE = 9;
    private static final String NORMAL_FONT_NAME = "ＭＳ ゴシック";
    private static final String OUTPUT_SHEET_NAME = "diff";
    private static final String[] JUDGE_ARRAY = { "OK", "NG" };
    private SXSSFWorkbook book;
    private Sheet sheet;
    private OutputStream outputStream;
    private CellStyle headerStyle;
    private CellStyle normalStyle;

    public ResultWriter(String outputPath) throws IOException {
        book = new SXSSFWorkbook();
        outputStream = new FileOutputStream(outputPath);
        headerStyle = createHeaderStyle(book);
        normalStyle = createNormalStyle(book);
        sheet = book.createSheet(OUTPUT_SHEET_NAME);
    }

    public void writeResult(Map<String, List<CompareResult>> resultMap) throws IOException {
        // header出力、columnサイズ調整
        writeHeader(sheet);
        // old, new , 説明
        for (int i = 1; i < 4; i++) {
            sheet.setColumnWidth(i, 60 * 256);
        }
        // 判定
        sheet.setColumnWidth(4, 4 * 256);
        // 備考
        sheet.setColumnWidth(5, 30 * 256);
        int rowIndex = 1;
        for (Map.Entry<String, List<CompareResult>> entry : resultMap.entrySet()) {
            String fileName = entry.getKey();
            for (CompareResult compareResult : entry.getValue()) {
                for (CompareDescription compareDescription : compareResult.getCompareDescriptions()) {
                    String description = compareDescription.getDescription();
                    String oldText = compareDescription.getOldElement();
                    String newText = compareDescription.getNewElement();
                    Row row = sheet.createRow(rowIndex++);
                    int columnIndex = 0;
                    Cell fileCell = row.createCell(columnIndex++);
                    fileCell.setCellStyle(normalStyle);
                    fileCell.setCellValue(fileName);
                    Cell oldCell = row.createCell(columnIndex++);
                    oldCell.setCellStyle(normalStyle);
                    oldCell.setCellValue(oldText);
                    Cell newCell = row.createCell(columnIndex++);
                    newCell.setCellStyle(normalStyle);
                    newCell.setCellValue(newText);
                    Cell descriptionCell = row.createCell(columnIndex++);
                    descriptionCell.setCellStyle(normalStyle);
                    descriptionCell.setCellValue(description);
                    Cell judgeCell = row.createCell(columnIndex++);
                    judgeCell.setCellStyle(normalStyle);
                    Cell remarksCell = row.createCell(columnIndex++);
                    remarksCell.setCellStyle(normalStyle);
                }
            }
        }
        if (rowIndex > 1) {
            DataValidationHelper helper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = helper.createExplicitListConstraint(JUDGE_ARRAY);
            CellRangeAddressList region = new CellRangeAddressList(1, rowIndex - 1, 4, 4);
            DataValidation validation = helper.createValidation(constraint, region);
            sheet.addValidationData(validation);
        } else {
            LOGGER.info("差分は見つかりませんでした。");
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
        int i = 0;
        Cell fileCell = header.createCell(i++);
        fileCell.setCellValue("file");
        fileCell.setCellStyle(headerStyle);
        Cell oldCell = header.createCell(i++);
        oldCell.setCellValue("old");
        oldCell.setCellStyle(headerStyle);
        Cell newCell = header.createCell(i++);
        newCell.setCellValue("new");
        newCell.setCellStyle(headerStyle);
        Cell descriptionCell = header.createCell(i++);
        descriptionCell.setCellValue("説明");
        descriptionCell.setCellStyle(headerStyle);
        Cell judgeCell = header.createCell(i++);
        judgeCell.setCellValue("判定");
        judgeCell.setCellStyle(headerStyle);
        Cell remarksCell = header.createCell(i++);
        remarksCell.setCellValue("備考");
        remarksCell.setCellStyle(headerStyle);
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
