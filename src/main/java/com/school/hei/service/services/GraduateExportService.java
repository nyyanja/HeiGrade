package com.school.hei.service.services;

import static java.io.File.createTempFile;

import com.school.hei.file.bucket.BucketComponent;
import com.school.hei.model.GraduateRanking;
import com.school.hei.security.CourseAccessService;
import java.io.File;
import java.io.FileOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class GraduateExportService {

    private static final String[] HEADERS = {
            "Rang", "Reference", "Prenom", "Nom", "Groupe", "Moyenne generale", "Credits totaux"
    };

    private final GraduateService graduateService;
    private final BucketComponent bucketComponent;
    private final CourseAccessService courseAccessService;

    @SneakyThrows
    public String exportGraduatesToExcel(UUID promotionId) {
        if (!courseAccessService.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "only admin can export graduates");
        }

        List<GraduateRanking> rankings = graduateService.getGraduatesByPromotion(promotionId);

        File file = createTempFile("graduates-" + promotionId, ".xlsx");
        try {
            writeWorkbook(rankings, file);

            String bucketKey = "graduates/" + promotionId + "-" + Instant.now().toEpochMilli() + ".xlsx";
            bucketComponent.upload(file, bucketKey);

            return bucketComponent.presign(bucketKey, Duration.ofMinutes(15)).toString();
        } finally {
            file.delete();
        }
    }

    private void writeWorkbook(List<GraduateRanking> rankings, File file) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(file)) {

            XSSFSheet sheet = workbook.createSheet("Diplomes");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (GraduateRanking ranking : rankings) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(ranking.getRank());
                row.createCell(1).setCellValue(nullToEmpty(ranking.getReference()));
                row.createCell(2).setCellValue(nullToEmpty(ranking.getFirstName()));
                row.createCell(3).setCellValue(nullToEmpty(ranking.getLastName()));
                row.createCell(4).setCellValue(nullToEmpty(ranking.getGroupName()));
                row.createCell(5).setCellValue(ranking.getGeneralAverage());
                row.createCell(6).setCellValue(ranking.getTotalCredit());
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}