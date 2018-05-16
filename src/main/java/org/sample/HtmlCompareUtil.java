package org.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HtmlCompareUtil {

    public static final List<CompareResult> compare(File oldHtml, File newHtml, Charset charset) throws IOException {
        List<String> oldLines = parseHtml(oldHtml, charset);
        List<String> newLines = parseHtml(newHtml, charset);
        if (oldLines.size() != newLines.size()) {
            throw new IllegalArgumentException("oldとnewでparse時の行数が異なります。");
        }
        List<CompareResult> resultList = new ArrayList<>();
        for (int i = 0; i < oldLines.size(); i++) {
            CompareResult compareResult = compare(oldLines.get(i), newLines.get(i));
            if (compareResult.isDiff()) {
                resultList.add(compareResult);
            }
        }
        return resultList;
    }

    public static final List<String> parseHtml(File html, Charset charset) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(Jsoup.parse(html, charset.name()).html()))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static final CompareResult compare(String o, String n) {
        Elements oldElements = Jsoup.parse(o).getAllElements();
        Elements newElements = Jsoup.parse(n).getAllElements();
        // tagのサイズを比較
        if (oldElements.size() != newElements.size()) {
            StringBuilder des = new StringBuilder("tag数が一致しません。old=");
            des.append(oldElements.size()).append(", new=").append(newElements.size());
            return new CompareResult(o, n, true, des.toString());
        }
        // tagごとに比較
        for (int i = 0; i < oldElements.size(); i++) {
            Element oldElement = oldElements.get(i);
            Element newElement = newElements.get(i);
            // タグ名の比較
            if (!oldElement.tagName().equals(newElement.tagName())) {
                StringBuilder des = new StringBuilder("tagの順序が異なります。index=");
                des.append(i).append(", old=").append(oldElement.tagName()).append(", new=")
                        .append(newElement.tagName());
                return new CompareResult(o, n, true, des.toString());
            }
            String tag = oldElement.tagName();
            // tagのtextの比較
            if (!oldElement.text().equals(newElement.text())) {
                StringBuilder des = new StringBuilder("tagのtextが異なります。tag=");
                des.append(tag).append(", old=").append(oldElement.text()).append(", new=").append(newElement.text());
                return new CompareResult(o, n, true, des.toString());
            }
            // 属性を取得
            Attributes oldAttributes = oldElement.attributes();
            Attributes newAttributes = newElement.attributes();
            // 属性のサイズ比較
            if (oldAttributes.size() != newAttributes.size()) {
                StringBuilder des = new StringBuilder("attribute数が異なります。tag=");
                des.append(tag).append(", old=").append(oldAttributes.size()).append(", new=")
                        .append(newAttributes.size());
                return new CompareResult(o, n, true, des.toString());
            }
            // 属性ごとに比較
            List<String> oldAttributeKey = getAttributeKeyList(oldAttributes);
            for (int j = 0; j < oldAttributeKey.size(); j++) {
                String key = oldAttributeKey.get(j);
                if (!oldAttributes.get(key).equals(newAttributes.get(key))) {
                    StringBuilder des = new StringBuilder("attributeの値が異なります。tag=");
                    des.append(tag).append(", attribute=").append(key).append(", old=").append(oldAttributes.get(key))
                            .append(", new=").append(newAttributes.get(key));
                    return new CompareResult(o, n, true, des.toString());
                }
            }
            List<String> newAttributeKey = getAttributeKeyList(newAttributes);
            for (int j = 0; j < newAttributeKey.size(); j++) {
                String key = newAttributeKey.get(j);
                if (!oldAttributes.get(key).equals(newAttributes.get(key))) {
                    StringBuilder des = new StringBuilder("attributeの値が異なります。tag=");
                    des.append(tag).append(", attribute=").append(key).append(", old=").append(oldAttributes.get(key))
                            .append(", new=").append(newAttributes.get(key));
                    return new CompareResult(o, n, true, des.toString());
                }
            }
        }
        return new CompareResult();
    }

    private static final List<String> getAttributeKeyList(Attributes attributes) {
        List<String> keyList = new ArrayList<>();
        for (Attribute attribute : attributes.asList()) {
            keyList.add(attribute.getKey());
        }
        return keyList;
    }

}
