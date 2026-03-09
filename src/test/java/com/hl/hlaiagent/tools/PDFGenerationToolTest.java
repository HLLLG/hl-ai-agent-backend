package com.hl.hlaiagent.tools;

import com.hl.hlaiagent.constant.FileConstant;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PDFGenerationToolTest {

    @Test
    void generatePdf() throws Exception {
        String fileName = "tool-test.pdf";
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();

        String result = pdfGenerationTool.generatePdf(fileName, "Hello PDF!");
        Path pdfPath = Path.of(FileConstant.FILE_SAVE_PATH, "pdf", fileName);

        assertTrue(result.startsWith("PDF generated successfully:"));
        assertTrue(Files.exists(pdfPath));
    }
}
