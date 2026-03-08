package org.example.secondbrainrag.infrastructure

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

/**
 * Infrastructure adapter for extracting plain text from PDF files using Apache PDFBox.
 * Includes text normalization to improve embedding quality.
 */
@Component
class PdfParsingAdapter {

    private val logger = LoggerFactory.getLogger(PdfParsingAdapter::class.java)

    /**
     * Extracts and normalizes plain text content from a PDF file.
     *
     * @param file The uploaded PDF MultipartFile
     * @return Normalized text content
     * @throws IllegalArgumentException if the PDF is empty or cannot be parsed
     */
    fun extractText(file: MultipartFile): String {
        logger.info("Extracting text from PDF: '{}'", file.originalFilename)

        val rawText = file.inputStream.use { inputStream ->
            PDDocument.load(inputStream).use { document ->
                if (document.numberOfPages == 0) {
                    throw IllegalArgumentException("PDF soubor neobsahuje žádné stránky.")
                }

                logger.info("PDF '{}' has {} pages", file.originalFilename, document.numberOfPages)

                val stripper = PDFTextStripper()
                stripper.getText(document)
            }
        }

        if (rawText.isBlank()) {
            throw IllegalArgumentException("Z PDF souboru se nepodařilo extrahovat žádný text. Soubor může obsahovat pouze obrázky.")
        }

        logger.info("Raw extraction: {} characters from PDF '{}'", rawText.length, file.originalFilename)

        // Normalize the extracted text
        val normalizedText = normalizeText(rawText)
        logger.info("After normalization: {} characters (reduced by {})",
            normalizedText.length, rawText.length - normalizedText.length)

        return normalizedText
    }

    /**
     * Normalizes extracted PDF text:
     * - Replaces multiple whitespace/tabs with a single space (per line)
     * - Strips invisible Unicode characters (zero-width spaces, soft hyphens, etc.)
     * - Removes excessive blank lines (keeps max 1 blank line between paragraphs)
     * - Trims each line
     */
    fun normalizeText(text: String): String {
        return text
            // Remove invisible Unicode characters (zero-width spaces, BOM, soft hyphens, etc.)
            .replace(Regex("[\\u200B\\u200C\\u200D\\uFEFF\\u00AD\\u2060]"), "")
            // Normalize line-level whitespace: tabs and multiple spaces → single space
            .lines()
            .map { line -> line.replace(Regex("[\\t ]+"), " ").trim() }
            // Remove excessive blank lines (collapse 3+ consecutive blank lines to 1)
            .joinToString("\n")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }
}
