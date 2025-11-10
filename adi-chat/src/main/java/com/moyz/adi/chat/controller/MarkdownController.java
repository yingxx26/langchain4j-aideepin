package com.moyz.adi.chat.controller;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.*;
import java.util.Base64;

@RestController
@RequestMapping("/api")
public class MarkdownController {

    @PostMapping("/convert")
    public ResponseEntity<byte[]> convert(@RequestBody String markdown) throws Exception {
        // 生成 Word 文档
        byte[] wordBytes = generateWordDocument(markdown);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=document.docx")
                .body(wordBytes);
    }

    private byte[] generateWordDocument(String markdown) throws Exception {
        XWPFDocument document = new XWPFDocument();

        // 将 Markdown 转换为 HTML
        String html = convertMarkdownToHtml(markdown);

        // 解析 HTML 并插入图片
        Document htmlDoc = Jsoup.parse(html);
        for (Element img : htmlDoc.select("img")) {
            String src = img.attr("src");
            if (src.startsWith("data:image/png;base64,")) {
                String base64 = src.substring("data:image/png;base64,".length());
                byte[] imageBytes = Base64.getDecoder().decode(base64);
                int pictureIdx = Integer.parseInt(document.addPictureData(imageBytes, XWPFDocument.PICTURE_TYPE_PNG));
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                InputStream inputStream = new ByteArrayInputStream(imageBytes);
                run.addPicture(inputStream, pictureIdx, "image.png", 200 * 24, 200 * 24);
            }
        }

        // 保存 Word 文档
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.write(out);
        return out.toByteArray();
    }

    private String convertMarkdownToHtml(String markdown) {
        // 仅做简单转换，实际可使用其他库如 flexmark-java
        return "<html><body>" + markdown + "</body></html>";
    }
}
