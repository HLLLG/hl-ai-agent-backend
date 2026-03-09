package com.hl.hlaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.hl.hlaiagent.constant.FileConstant;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * PDF 生成工具类，提供 PDF 文件生成功能。
 */
@Slf4j
public class PDFGenerationTool {

    private static final String PDF_DIR = FileConstant.FILE_SAVE_PATH + "/pdf";

    /**
     * 生成 PDF 文件。
     *
     * @param fileName PDF 文件名
     * @param content  PDF 内容
     * @return 生成结果
     */
    @Tool(description = "Generate a PDF file from text content")
    public String generatePdf(@ToolParam(description = "Name of the PDF file to create") String fileName,
                              @ToolParam(description = "Text content to write into the PDF file") String content) {
        if (StrUtil.isBlank(fileName)) {
            return "File name must not be blank.";
        }
        if (StrUtil.isBlank(content)) {
            return "PDF content must not be blank.";
        }

        String normalizedFileName = normalizeFileName(fileName);
        String filePath = PDF_DIR + "/" + normalizedFileName;

        try {
            try (PdfWriter writer = new PdfWriter(filePath); PdfDocument pdfDocument = new PdfDocument(writer); Document document = new Document(pdfDocument)) {
                // 创建目录
                FileUtil.mkdir(PDF_DIR);
                // 自定义字体
//            String fontPath = Paths.get("src/main/resources/fonts/STSong-Light.ttf").toString();
//            PdfFont pdfFont = PdfFontFactory.createFont(fontPath, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                // 设置字体，支持中文
                PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
                document.setFont(font);
                // 写入内容并关闭文档
                document.add(new Paragraph(content));
                return "PDF generated successfully: " + filePath;
            }
        } catch (Exception e) {
            log.error("Failed to generate PDF file: {}", normalizedFileName, e);
            return "Failed to generate PDF file: " + e.getMessage();
        }
    }

    private String normalizeFileName(String fileName) {
        String trimmedFileName = fileName.trim();
        return trimmedFileName.toLowerCase().endsWith(".pdf") ? trimmedFileName : trimmedFileName + ".pdf";
    }
}
