package org.sample;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiffMain.class);
    private static final int ERROR_CODE = 1;
    private static final String DEFAULT_CHARSET = "MS932";
    private static final String CHARSET_KEY = "DiffMain.charset";
    private static final String DATE_FORMAT = "yyyyMMddHHmmss";

    public static void main(String[] args) {
        LOGGER.info("Start");
        try {
            validateArgs(args);
        } catch (Throwable t) {
            LOGGER.error("引数精査でエラー発生", t);
            System.exit(ERROR_CODE);
        }
        File oldDir = new File(args[0]);
        File newDir = new File(args[1]);
        try {
            LOGGER.info("old directory=" + oldDir.getCanonicalPath() + ", new directory=" + newDir.getCanonicalPath());
        } catch (IOException e) {
            LOGGER.error("エラーが発生しました。", e);
            System.exit(ERROR_CODE);
        }
        Map<String, File> oldFiles = searchHtml(oldDir);
        Map<String, File> newFiles = searchHtml(newDir);
        Map<String, List<CompareResult>> resultMap = new TreeMap<>();
        String charsetName = System.getProperty(CHARSET_KEY, DEFAULT_CHARSET);
        Charset charset = Charset.forName(charsetName);
        for (Map.Entry<String, File> entry : oldFiles.entrySet()) {
            String key = entry.getKey();
            LOGGER.info("ファイル：" + key + "の比較を行います。");
            try {
                List<CompareResult> result = HtmlCompareUtil.compare(oldFiles.get(key), newFiles.get(key), charset);
                resultMap.put(key, result);
            } catch (IllegalArgumentException e) {
                LOGGER.warn(key + "はnewとoldで行数が異なります。", e);
            } catch (Throwable ｔ) {
                LOGGER.error("エラーが発生しました。", ｔ);
                System.exit(ERROR_CODE);
            }
        }
        String outputPath = outputFilePath(args[0]);
        LOGGER.info("結果を出力します。output=" + outputPath);
        try {
            ResultWriter writer = new ResultWriter(outputPath);
            writer.writeResult(resultMap);
            writer.close();
        } catch (Throwable ｔ) {
            LOGGER.error("エラーが発生しました。", ｔ);
            System.exit(ERROR_CODE);
        }
        LOGGER.info("End");
    }

    private static String outputFilePath(String arg0) {
        String parent = new File(arg0).getParent();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        LocalDateTime ldt = LocalDateTime.now();
        return parent + "/" + formatter.format(ldt) + "_DiffHtml.xlsx";
    }

    private static void validateArgs(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("引数が不足しています。args.length=" + args.length);
        }
        File oldDir = new File(args[0]);
        if (!oldDir.exists() || !oldDir.isDirectory()) {
            throw new IllegalArgumentException("引数で指定されたディレクトリが存在しません。args[0]=" + args[0]);
        }
        File newDir = new File(args[1]);
        if (!newDir.exists() || !newDir.isDirectory()) {
            throw new IllegalArgumentException("引数で指定されたディレクトリが存在しません。args[1]=" + args[1]);
        }
        Map<String, File> oldHtmls = searchHtml(oldDir);
        if (oldHtmls.isEmpty()) {
            throw new IllegalArgumentException("HTMLが存在しません。args[0]=" + args[0]);
        }
        Map<String, File> newHtmls = searchHtml(newDir);
        if (newHtmls.isEmpty()) {
            throw new IllegalArgumentException("HTMLが存在しません。args[1]=" + args[1]);
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, File> entry : oldHtmls.entrySet()) {
            if (!newHtmls.containsKey(entry.getKey())) {
                sb.append("args[1]=").append(args[1]).append("には").append(entry.getKey()).append("が存在しません。")
                        .append(System.lineSeparator());
            }
        }
        for (Map.Entry<String, File> entry : newHtmls.entrySet()) {
            if (!oldHtmls.containsKey(entry.getKey())) {
                sb.append("args[0]=").append(args[0]).append("には").append(entry.getKey()).append("が存在しません。")
                        .append(System.lineSeparator());
            }
        }
        if (sb.length() != 0) {
            sb.insert(0, "ファイル差分が存在します。" + System.lineSeparator());
            throw new IllegalArgumentException(sb.toString());
        }
    }

    private static Map<String, File> searchHtml(File dir) {
        File[] htmls = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".html");
            }
        });
        Map<String, File> map = new TreeMap<>();
        for (File f : htmls) {
            map.put(f.getName(), f);
        }
        return map;
    }
}
