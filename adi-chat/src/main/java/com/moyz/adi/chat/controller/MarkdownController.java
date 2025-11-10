package com.moyz.adi.chat.controller;

import com.moyz.adi.common.service.PandocService;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.*;
import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
@RestController
@RequestMapping("/api/export")
public class MarkdownController {

    @Autowired
    private PandocService pandocService;

    @PostMapping("/word")
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

        public String getHtml() { return html; }
        public void setHtml(String html) { this.html = html; }
    }
}
