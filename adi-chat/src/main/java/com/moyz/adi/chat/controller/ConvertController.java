package com.moyz.adi.chat.controller;

import com.moyz.adi.common.util.MdToWordProcessor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ConvertController {

    @PostMapping("/md-to-word")
    public ResponseEntity<byte[]> convert(@RequestBody Map<String, String> request) throws Exception {
        String processedMd = request.get("content"); // 前端处理后的Markdown（含图片）
        // 生成Word文档
        MdToWordProcessor processor = new MdToWordProcessor();
        XWPFDocument doc = processor.process(processedMd);

        // 转为字节数组返回
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.write(out);
            byte[] bytes = out.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=report.docx");
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        }
    }
}