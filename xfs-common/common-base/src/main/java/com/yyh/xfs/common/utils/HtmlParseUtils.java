package com.yyh.xfs.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yyh
 * @date 2024-02-19
 */
public class HtmlParseUtils {

    /**
     * 将html转换为纯文本
     *
     * @param html html
     * @return 纯文本
     */
    public static String htmlToText(String html) {
        // 移除样式、脚本、iframe标签及其内容
        String cleanedHtml = html.replaceAll("<(style|script|iframe)[^>]*?>[\\s\\S]+?</\\1\\s*>", "");
        // 提取图片标签中的alt属性内容
        Pattern imgPattern = Pattern.compile("<img[^>]+alt=[\"']([^\"']+)[\"'][^>]*>");
        String textWithAlt = getString(imgPattern, cleanedHtml);
        // 移除其他HTML标签
        String textWithoutTags = textWithAlt.replaceAll("<[^>]+?>", "");
        // 将多个连续的空白字符替换为单个空格
        String textWithSingleSpace = textWithoutTags.replaceAll("\\s+", " ").trim();
        // 移除多余的大于号
        return textWithSingleSpace.replaceAll(" >", ">");
    }

    private static String getString(Pattern imgPattern, String cleanedHtml) {
        Matcher imgMatcher = imgPattern.matcher(cleanedHtml);
        StringBuffer resultBuffer = new StringBuffer();
        while (imgMatcher.find()) {
            imgMatcher.appendReplacement(resultBuffer, " " + imgMatcher.group(1) + " ");
        }
        imgMatcher.appendTail(resultBuffer);
        return resultBuffer.toString();
    }
}
