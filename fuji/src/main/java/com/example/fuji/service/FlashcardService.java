package com.example.fuji.service;

import com.example.fuji.entity.Flashcard;
import com.example.fuji.repository.FlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardRepository flashcardRepository;

    @Transactional
    public void importFromExcel(MultipartFile file, String lessonId) {
        try (InputStream inputStream = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            List<Flashcard> flashcards = new ArrayList<>();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Skip header row
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Flashcard flashcard = new Flashcard();

                // Assuming columns: Kanji, Hiragana, Meaning, Example, Lesson, Type
                flashcard.setKanji(formatter(currentRow.getCell(0)));
                flashcard.setHiragana(formatter(currentRow.getCell(1)));
                flashcard.setMeaning(formatter(currentRow.getCell(2)));
                flashcard.setExample(formatter(currentRow.getCell(3)));
                // Prioritize lessonId from param if available, otherwise read from file?
                // Requirement says "import process correctly assigns imported flashcards to a
                // selected lesson"
                // So we should use the lessonId passed from FE
                flashcard.setLesson(lessonId);
                flashcard.setType(formatter(currentRow.getCell(5)));
                flashcard.setViewCount(0);

                if (flashcard.getKanji() != null && !flashcard.getKanji().isEmpty()) {
                    flashcards.add(flashcard);
                }

                rowNumber++;
            }

            if (!flashcards.isEmpty()) {
                flashcardRepository.saveAll(flashcards);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to import Excel file: " + e.getMessage());
        }
    }

    private String formatter(Cell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public List<Flashcard> getAllFlashcards() {
        return flashcardRepository.findAll();
    }
}
