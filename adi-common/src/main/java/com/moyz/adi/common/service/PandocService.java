package com.moyz.adi.common.service;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class PandocService {

    /**
     * 通过Pandoc将HTML转换为Word
     *
     * @param htmlContent 处理后的HTML内容（含图片）
     * @return Word文档字节数组
     */
    public byte[] convertHtmlToWord(String htmlContent) throws Exception {
        // 1. 创建临时目录
        Path tempDir = Files.createTempDirectory("pandoc-");

        // 2. 创建临时HTML文件
        String htmlFileName = UUID.randomUUID() + ".html";
        File htmlFile = new File(tempDir.toFile(), htmlFileName);
        FileUtils.writeStringToFile(htmlFile, htmlContent, StandardCharsets.UTF_8);

        // 3. 定义输出Word文件
        String docxFileName = UUID.randomUUID() + ".docx";
        File docxFile = new File(tempDir.toFile(), docxFileName);
        String pandocPath = "D:/Program Files/Pandoc/pandoc.exe"; // Windows示例
        // String pandocPath = "/usr/local/bin/pandoc"; // Linux/macOS示例
        // 4. 构建Pandoc命令
        // 命令格式：pandoc [输入文件] -o [输出文件] --from html --to docx
        String[] command = {
                pandocPath,
                htmlFile.getAbsolutePath(),
                "-o",
                docxFile.getAbsolutePath(),
                "--from", "html",
                "--to", "docx"
        };

        // 5. 执行命令
        Process process = new ProcessBuilder(command)
                .redirectErrorStream(true) // 合并错误流到输出流
                .start();

        // 6. 等待命令执行完成
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            // 读取错误信息
            String error = readStream(process.getInputStream());
            throw new RuntimeException("Pandoc转换失败: " + error);
        }

        // 7. 读取Word文件内容
        byte[] docxBytes = FileUtils.readFileToByteArray(docxFile);

        // 8. 清理临时文件
        FileUtils.deleteDirectory(tempDir.toFile());

        return docxBytes;
    }

    /**
     * 读取输入流内容
     */
    private String readStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }
}