package org.sample;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

public class HtmlTest {
    @Test
    public void executeTest() throws Exception {
        List<String> inputList = new ArrayList<>();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("./test.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                inputList.add(line);
            }
        }

        String oldInput = inputList.get(0);
        String newInput = inputList.get(1);
        Elements oldElements = Jsoup.parse(oldInput).getAllElements();
        Elements newElements = Jsoup.parse(newInput).getAllElements();
        // 要素のサイズを比較
        System.out.println(oldElements.size() == newElements.size());
        // 要素ごとに比較
        for (int i = 0; i < oldElements.size(); i++) {
            Element oldElement = oldElements.get(i);
            Element newElement = newElements.get(i);
            // タグ名の比較
            System.out.println(oldElement.tagName().equals(newElement.tagName()));
            // テキストの比較
            System.out.println(oldElement.text().equals(newElement.text()));
            // 属性を取得
            Attributes oldAttributes = oldElement.attributes();
            Attributes newAttributes = newElement.attributes();
            // 属性のサイズ比較
            System.out.println(oldAttributes.size() == newAttributes.size());
            // 属性ごとに比較
            List<String> oldAttributeKey = getAttributeKeyList(oldAttributes);
            for (int j = 0; j < oldAttributeKey.size(); j++) {
                String key = oldAttributeKey.get(j);
                System.out.println(oldAttributes.get(key).equals(newAttributes.get(key)));
            }
            List<String> newAttributeKey = getAttributeKeyList(newAttributes);
            for (int j = 0; j < newAttributeKey.size(); j++) {
                String key = newAttributeKey.get(j);
                System.out.println(oldAttributes.get(key).equals(newAttributes.get(key)));
            }
        }
    }

    private List<String> getAttributeKeyList(Attributes attributes) {
        List<String> keyList = new ArrayList<>();
        for (Attribute attribute : attributes.asList()) {
            keyList.add(attribute.getKey());
        }
        return keyList;
    }
}
