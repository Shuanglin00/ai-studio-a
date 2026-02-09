package com.shuanglin.ai.utils;

import lombok.extern.slf4j.Slf4j;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class FileReadUtil {

    public static String readFileContent(String filePath) throws IOException, InvalidFormatException {
        Objects.requireNonNull(filePath, "文件路径不能为空");
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("文件不存在: " + filePath);
        }
        return readFileContent(file);
    }

    public static String readFileContent(File file) throws IOException, InvalidFormatException {
        String fileExtension = getFileExtension(file.getPath()).toLowerCase();
        StringBuilder content = new StringBuilder();

        switch (fileExtension) {
            case "txt":
                content.append(readTxtFile(file));
                break;
            case "xls":
            case "xlsx":
                content.append(readExcelFile(file));
                break;
            case "docx":
                content.append(readDocxFile(file));
                break;
            case "epub":
                content.append(readEpubFile(file));
                break;
            default:
                throw new UnsupportedOperationException("不支持的文件类型: ." + fileExtension);
        }
        return content.toString();
    }

    private static String readTxtFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        return content.toString();
    }

    private static String readExcelFile(File file) throws IOException, InvalidFormatException {
        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file)) {
            Workbook workbook;
            String fileName = file.getName();

            if (fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (fileName.endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            } else {
                throw new IllegalArgumentException("文件类型不支持: " + fileName + " (仅支持 .xls 或 .xlsx)");
            }

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                content.append("--- Sheet: ").append(sheet.getSheetName()).append(" ---").append(System.lineSeparator());

                for (Row row : sheet) {
                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        content.append(cellValue).append("\t");
                    }
                    content.append(System.lineSeparator());
                }
                content.append(System.lineSeparator());
            }
        }
        return content.toString();
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            case ERROR:
                return "ERROR:" + cell.getErrorCellValue();
            default:
                return cell.toString();
        }
    }

    private static String readDocxFile(File file) throws IOException, InvalidFormatException {
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument docx = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(docx)) {
            return extractor.getText();
        }
    }

    public static List<ParseResult> readEpubFile(File file) {
        List<ParseResult> results = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file)) {
            Book book = new EpubReader().readEpub(fis);
            if (book.getTableOfContents() != null) {
                results = extractTocRecursively(book.getContents());
            }
        } catch (Exception e) {
            log.error("读取 EPUB 文件失败: {}", file.getAbsolutePath(), e);
        }
        return results;
    }

    private static List<ParseResult> extractTocRecursively(List<Resource> contents) throws IOException {
        List<ParseResult> results = new ArrayList<>();
        for (Resource resource : contents) {
            ParseResult parseResult = parseHtml(new String(resource.getData(), "UTF-8"));
            results.add(parseResult);
        }
        return results;
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    public static ParseResult parseHtml(String htmlString) {
        try {
            Document document = Jsoup.parse(htmlString);
            String title = document.title();
            Elements pElements = document.select("p");
            List<String> contentList = new ArrayList<>();

            for (Element pElement : pElements) {
                String text = pElement.text().trim();
                if (!text.isEmpty()) {
                    contentList.add(text);
                }
            }

            return new ParseResult(title, contentList);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class ParseResult {
        private String title;
        private List<String> contentList;

        public ParseResult(String title, List<String> contentList) {
            this.title = title;
            this.contentList = contentList;
        }

        public String getTitle() {
            return title;
        }

        public List<String> getContentList() {
            return contentList;
        }

        public String getContent() {
            if (this.contentList == null || this.contentList.isEmpty()) {
                return "";
            }
            return this.contentList.stream()
                    .filter(paragraph -> paragraph != null && !paragraph.trim().isEmpty())
                    .reduce((p1, p2) -> p1 + "\n" + p2)
                    .orElse("");
        }
    }
}
