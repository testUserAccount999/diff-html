package org.sample;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlCompareUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlCompareUtil.class);
    private static final String STRUTS_TOKEN_KEY = "org.apache.struts.taglib.html.TOKEN";
    private static final String STRUTS_TOKEN_REGEX = STRUTS_TOKEN_KEY + "=[\\da-f]+";
    private static final String DATE_MIN_REGEX = "[\\d]{4}/[\\d]{2}/[\\d]{2} [\\d]{2}:[\\d]{2}";
    private static final Pattern DATE_MIN_PATTERN = Pattern.compile(DATE_MIN_REGEX);
    private static final String DATE_SEC_REGEX = "[\\d]{4}/[\\d]{2}/[\\d]{2} [\\d]{2}:[\\d]{2}:[\\d]{2}";
    private static final Pattern DATE_SEC_PATTERN = Pattern.compile(DATE_SEC_REGEX);

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
        String htmlString = Jsoup.parse(html, charset.name()).html();
        LOGGER.info(html.getCanonicalPath() + " :=\r\n" + htmlString);
        try (BufferedReader reader = new BufferedReader(new StringReader(htmlString))) {
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
        List<String> descriptions = new ArrayList<>();
        // tagのサイズを比較
        if (oldElements.size() != newElements.size()) {
            StringBuilder des = new StringBuilder("tag数が一致しません。old=");
            des.append(oldElements.size()).append(", new=").append(newElements.size());
            descriptions.add(des.toString());
            // サイズが異なると以降の精査できないので返却する
            return new CompareResult(o, n, true, descriptions);
        }
        // tagごとに比較
        for (int i = 0; i < oldElements.size(); i++) {
            Element oldElement = oldElements.get(i);
            Element newElement = newElements.get(i);
            String tag = oldElement.tagName();
            if ("#root".equals(tag) || "html".equals(tag) || "body".equals(tag)) {
                // 上記のtagはJsoupで付加されるのでスキップ
                continue;
            }
            // tag名の比較
            if (!oldElement.tagName().equals(newElement.tagName())) {
                StringBuilder des = new StringBuilder("tagの順序が異なります。index=");
                des.append(i).append(", old=").append(oldElement.tagName()).append(", new=")
                        .append(newElement.tagName());
                descriptions.add(des.toString());
                // tagが異なる場合、属性の比較をしても無駄なので次へ
                continue;
            }
            // tagのtextの比較
            if (!oldElement.text().equals(newElement.text())) {
                if (!isIgnoreText(oldElement, newElement)) {
                    StringBuilder des = new StringBuilder("tagのtextが異なります。tag=");
                    des.append(tag).append(", old=").append(oldElement.text()).append(", new=")
                            .append(newElement.text());
                    descriptions.add(des.toString());
                }
            }
            // 属性を取得
            Attributes oldAttributes = oldElement.attributes();
            Attributes newAttributes = newElement.attributes();
            // 属性のサイズ比較
            if (oldAttributes.size() != newAttributes.size()) {
                StringBuilder des = new StringBuilder("attribute数が異なります。tag=");
                des.append(tag).append(", old=").append(oldAttributes.size()).append(", new=")
                        .append(newAttributes.size());
                descriptions.add(des.toString());
                continue;
            }
            // 属性ごとに比較
            List<String> oldAttributeKey = getAttributeKeyList(oldAttributes);
            for (int j = 0; j < oldAttributeKey.size(); j++) {
                String key = oldAttributeKey.get(j);
                if (!oldAttributes.get(key).equals(newAttributes.get(key))) {
                    if (isIgnoreAttribute(tag, key, oldAttributes, newAttributes)) {
                        continue;
                    }
                    StringBuilder des = new StringBuilder("attributeの値が異なります。tag=");
                    des.append(tag).append(", attribute=").append(key).append(", old=").append(oldAttributes.get(key))
                            .append(", new=").append(newAttributes.get(key));
                    descriptions.add(des.toString());
                }
            }
            List<String> newAttributeKey = getAttributeKeyList(newAttributes);
            for (int j = 0; j < newAttributeKey.size(); j++) {
                String key = newAttributeKey.get(j);
                if (!oldAttributes.get(key).equals(newAttributes.get(key))) {
                    if (isIgnoreAttribute(tag, key, oldAttributes, newAttributes)) {
                        continue;
                    }
                    StringBuilder des = new StringBuilder("attributeの値が異なります。tag=");
                    des.append(tag).append(", attribute=").append(key).append(", old=").append(oldAttributes.get(key))
                            .append(", new=").append(newAttributes.get(key));
                    descriptions.add(des.toString());
                }
            }
        }
        return new CompareResult(o, n, !descriptions.isEmpty(), descriptions);
    }

    private static boolean isIgnoreText(Element oldElement, Element newElement) {
        // 日付を無視
        String oldText = oldElement.text();
        String newText = oldElement.text();
        Matcher oldDateSec = DATE_SEC_PATTERN.matcher(oldText);
        Matcher newDateSec = DATE_SEC_PATTERN.matcher(newText);
        if (oldDateSec.find() && newDateSec.find()) {
            if (oldText.replaceAll(DATE_SEC_REGEX, "").equals(newText.replaceAll(DATE_SEC_REGEX, ""))) {
                return true;
            }
        }
        Matcher oldDateMin = DATE_MIN_PATTERN.matcher(oldText);
        Matcher newDateMin = DATE_MIN_PATTERN.matcher(newText);
        if (oldDateMin.find() && newDateMin.find()) {
            if (oldText.replaceAll(DATE_MIN_REGEX, "").equals(newText.replaceAll(DATE_MIN_REGEX, ""))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isIgnoreAttribute(String tag, String key, Attributes oldAttributes,
            Attributes newAttributes) {
        // inputタグがTOKENの場合は無視
        if ("input".equals(tag) && "value".equals(key) && STRUTS_TOKEN_KEY.equals(oldAttributes.get("name"))
                && STRUTS_TOKEN_KEY.equals(newAttributes.get("name"))) {
            return true;
        }
        // 値にTOKENタグが含まれる場合にTOKENを除去して同じ結果ならば無視
        if (oldAttributes.get(key).contains(STRUTS_TOKEN_KEY) && newAttributes.get(key).contains(STRUTS_TOKEN_KEY)) {
            String oldText = oldAttributes.get(key).replaceAll(STRUTS_TOKEN_REGEX, "");
            String newText = newAttributes.get(key).replaceAll(STRUTS_TOKEN_REGEX, "");
            if (oldText.equals(newText)) {
                return true;
            }
        }
        // 日付を無視
        String oldText = oldAttributes.get(key);
        String newText = newAttributes.get(key);
        Matcher oldDateSec = DATE_SEC_PATTERN.matcher(oldText);
        Matcher newDateSec = DATE_SEC_PATTERN.matcher(newText);
        if (oldDateSec.find() && newDateSec.find()) {
            if (oldText.replaceAll(DATE_SEC_REGEX, "").equals(newText.replaceAll(DATE_SEC_REGEX, ""))) {
                return true;
            }
        }
        Matcher oldDateMin = DATE_MIN_PATTERN.matcher(oldText);
        Matcher newDateMin = DATE_MIN_PATTERN.matcher(newText);
        if (oldDateMin.find() && newDateMin.find()) {
            if (oldText.replaceAll(DATE_MIN_REGEX, "").equals(newText.replaceAll(DATE_MIN_REGEX, ""))) {
                return true;
            }
        }
        return false;
    }

    private static final List<String> getAttributeKeyList(Attributes attributes) {
        List<String> keyList = new ArrayList<>();
        for (Attribute attribute : attributes.asList()) {
            keyList.add(attribute.getKey());
        }
        return keyList;
    }

}
