package com.moyz.adi.common.util;

import org.apache.poi.ss.usermodel.Table;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MdToWordProcessor {
    // 匹配<img>标签中的Base64图片（src="data:image/xxx;base64,xxx"）
    private static final Pattern IMAGE_PATTERN = Pattern.compile("<img.*?src=\"data:(.*?);base64,(.*?)\".*?>");

    public XWPFDocument process(String markdown) {
        XWPFDocument doc = new XWPFDocument();
        // 1. 解析Markdown节点（标题、段落、图片等）
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        // 2. 递归处理节点，生成Word内容
        processNode(document, doc);
        return doc;
    }

    // 递归处理Markdown节点
    private void processNode(Node node, XWPFDocument doc) {
        if (node == null) return;

        // 处理标题
        if (node instanceof Heading heading) {
            XWPFParagraph para = doc.createParagraph();
            XWPFRun run = para.createRun();
            run.setText(heading.getText().toString());
            run.setBold(true);
            run.setFontSize(16 - heading.getLevel() * 2); // 标题级别对应字号
        }

        // 处理段落（含图片标签）
        else if (node instanceof Paragraph paraNode) {
            //String paraText = paraNode.getText().toString();
            String paraText = node.getNodeName();
            XWPFParagraph para = doc.createParagraph();

            // 检查段落中是否有图片标签
            Matcher imageMatcher = IMAGE_PATTERN.matcher(paraText);
            int lastEnd = 0;

            while (imageMatcher.find()) {
                // 1. 添加图片前的文本
                String textBefore = paraText.substring(lastEnd, imageMatcher.start());
                if (!textBefore.isEmpty()) {
                    XWPFRun textRun = para.createRun();
                    textRun.setText(textBefore);
                }

                // 2. 提取Base64图片并插入
                String base64Data = imageMatcher.group(2);
                try {
                    byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                    InputStream imageStream = new ByteArrayInputStream(imageBytes);
                    // 插入图片到Word
                    XWPFRun imageRun = para.createRun();
                    imageRun.addPicture(
                            imageStream,
                            XWPFDocument.PICTURE_TYPE_PNG, // 支持SVG/PNG/JPG
                            "chart.png",
                            Units.toEMU(400), // 宽度
                            Units.toEMU(300)  // 高度
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }

                lastEnd = imageMatcher.end();
            }

            // 3. 添加图片后的文本
            if (lastEnd < paraText.length()) {
                XWPFRun textRun = para.createRun();
                textRun.setText(paraText.substring(lastEnd));
            }
        }

        // 处理表格（简化版，需根据实际需求扩展）
        else if (node instanceof Table tableNode) {
            XWPFTable table = doc.createTable();
            // 解析表格行和列（略，需遍历tableNode的子节点）
        }

        // 递归处理子节点
        for (Node child : node.getChildren()) {
            processNode(child, doc);
        }
    }
}