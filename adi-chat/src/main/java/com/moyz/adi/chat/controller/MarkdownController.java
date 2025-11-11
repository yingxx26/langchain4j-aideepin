package com.moyz.adi.chat.controller;

import com.moyz.adi.common.service.PandocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "http://localhost:1002")
public class MarkdownController {

    @Autowired
    private PandocService pandocService;

    @PostMapping("/export/word")
    public ResponseEntity<byte[]> exportWord(@RequestBody HtmlRequest request) {
        try {
            byte[] wordBytes = pandocService.convertHtmlToWord(request.getHtml());

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=\"document.docx\"");
            headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

            return new ResponseEntity<>(wordBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 请求体实体类
    public static class HtmlRequest {
        private String html;

        public String getHtml() {
            return html;
        }

        public void setHtml(String html) {
            this.html = html;
        }
    }
}
