package org.sample;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;
import java.util.TreeMap;

public class DiffMain {

    public static void main(String[] args) {
        validateArgs(args);
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
