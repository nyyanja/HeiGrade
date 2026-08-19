package com.school.hei.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.school.hei.file.bucket.BucketComponent;
import com.school.hei.model.GraduateRanking;
import com.school.hei.security.CourseAccessService;
import com.school.hei.service.services.GraduateExportService;
import com.school.hei.service.services.GraduateService;
import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class GraduateExportServiceTest {

  @Mock private GraduateService graduateService;

  @Mock private BucketComponent bucketComponent;

  @Mock private CourseAccessService courseAccessService;

  private GraduateExportService graduateExportService;

  private UUID promotionId;

  @BeforeEach
  void setUp() {
    promotionId = UUID.randomUUID();

    graduateExportService =
        new GraduateExportService(graduateService, bucketComponent, courseAccessService);
  }

  @Test
  void should_export_graduates_to_excel_for_admin() throws Exception {
    GraduateRanking first =
        GraduateRanking.builder()
            .rank(1)
            .reference("STD001")
            .firstName("John")
            .lastName("Doe")
            .groupName("EL-2026-A")
            .generalAverage(16.5)
            .totalCredit(60)
            .build();

    GraduateRanking second =
        GraduateRanking.builder()
            .rank(2)
            .reference("STD002")
            .firstName("Jane")
            .lastName("Smith")
            .groupName("EL-2026-B")
            .generalAverage(15.2)
            .totalCredit(60)
            .build();

    when(courseAccessService.isAdmin()).thenReturn(true);
    when(graduateService.getGraduatesByPromotion(promotionId)).thenReturn(List.of(first, second));

    when(bucketComponent.presign(anyString(), eq(Duration.ofMinutes(15))))
        .thenReturn(new java.net.URL("https://example.com/graduates.xlsx"));

    doAnswer(
            invocation -> {
              File file = invocation.getArgument(0);

              assertThat(file).exists();
              assertThat(file.length()).isGreaterThan(0);
              assertThat(file.getName()).endsWith(".xlsx");

              try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(file)) {

                assertThat(workbook.getNumberOfSheets()).isEqualTo(1);

                var sheet = workbook.getSheet("Diplomes");
                assertThat(sheet).isNotNull();

                var header = sheet.getRow(0);

                assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Rang");
                assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Reference");
                assertThat(header.getCell(2).getStringCellValue()).isEqualTo("Prenom");
                assertThat(header.getCell(3).getStringCellValue()).isEqualTo("Nom");
                assertThat(header.getCell(4).getStringCellValue()).isEqualTo("Groupe");
                assertThat(header.getCell(5).getStringCellValue()).isEqualTo("Moyenne generale");
                assertThat(header.getCell(6).getStringCellValue()).isEqualTo("Credits totaux");

                var row1 = sheet.getRow(1);

                assertThat(row1.getCell(0).getNumericCellValue()).isEqualTo(1);
                assertThat(row1.getCell(1).getStringCellValue()).isEqualTo("STD001");
                assertThat(row1.getCell(2).getStringCellValue()).isEqualTo("John");
                assertThat(row1.getCell(3).getStringCellValue()).isEqualTo("Doe");
                assertThat(row1.getCell(4).getStringCellValue()).isEqualTo("EL-2026-A");
                assertThat(row1.getCell(5).getNumericCellValue()).isEqualTo(16.5);
                assertThat(row1.getCell(6).getNumericCellValue()).isEqualTo(60);

                var row2 = sheet.getRow(2);

                assertThat(row2.getCell(0).getNumericCellValue()).isEqualTo(2);
                assertThat(row2.getCell(1).getStringCellValue()).isEqualTo("STD002");
                assertThat(row2.getCell(2).getStringCellValue()).isEqualTo("Jane");
                assertThat(row2.getCell(3).getStringCellValue()).isEqualTo("Smith");
                assertThat(row2.getCell(4).getStringCellValue()).isEqualTo("EL-2026-B");
                assertThat(row2.getCell(5).getNumericCellValue()).isEqualTo(15.2);
                assertThat(row2.getCell(6).getNumericCellValue()).isEqualTo(60);
              }

              return null;
            })
        .when(bucketComponent)
        .upload(any(File.class), anyString());

    String result = graduateExportService.exportGraduatesToExcel(promotionId);

    assertThat(result).isEqualTo("https://example.com/graduates.xlsx");

    verify(courseAccessService).isAdmin();
    verify(graduateService).getGraduatesByPromotion(promotionId);
    verify(bucketComponent).upload(any(File.class), anyString());

    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

    verify(bucketComponent).presign(keyCaptor.capture(), eq(Duration.ofMinutes(15)));

    assertThat(keyCaptor.getValue()).startsWith("graduates/" + promotionId + "-").endsWith(".xlsx");
  }

  @Test
  void should_export_empty_excel_when_there_are_no_graduates() throws Exception {
    when(courseAccessService.isAdmin()).thenReturn(true);
    when(graduateService.getGraduatesByPromotion(promotionId)).thenReturn(List.of());

    when(bucketComponent.presign(anyString(), eq(Duration.ofMinutes(15))))
        .thenReturn(new java.net.URL("https://example.com/empty.xlsx"));

    doAnswer(
            invocation -> {
              File file = invocation.getArgument(0);

              try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(file)) {

                assertThat(workbook.getNumberOfSheets()).isEqualTo(1);

                var sheet = workbook.getSheet("Diplomes");

                assertThat(sheet).isNotNull();
                assertThat(sheet.getLastRowNum()).isEqualTo(0);

                var header = sheet.getRow(0);

                assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Rang");
                assertThat(header.getCell(6).getStringCellValue()).isEqualTo("Credits totaux");
              }

              return null;
            })
        .when(bucketComponent)
        .upload(any(File.class), anyString());

    String result = graduateExportService.exportGraduatesToExcel(promotionId);

    assertThat(result).isEqualTo("https://example.com/empty.xlsx");

    verify(graduateService).getGraduatesByPromotion(promotionId);
    verify(bucketComponent).upload(any(File.class), anyString());
    verify(bucketComponent).presign(anyString(), eq(Duration.ofMinutes(15)));
  }

  @Test
  void should_forbid_non_admin_from_exporting_graduates() {
    when(courseAccessService.isAdmin()).thenReturn(false);
    assertThatThrownBy(() -> graduateExportService.exportGraduatesToExcel(promotionId))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("only admin can export graduates");

    verify(courseAccessService).isAdmin();
    verifyNoInteractions(graduateService, bucketComponent);
  }

  @Test
  void should_export_empty_cells_when_graduate_fields_are_null() throws Exception {
    GraduateRanking ranking =
        GraduateRanking.builder()
            .rank(1)
            .reference(null)
            .firstName(null)
            .lastName(null)
            .groupName(null)
            .generalAverage(12.5)
            .totalCredit(60)
            .build();

    when(courseAccessService.isAdmin()).thenReturn(true);
    when(graduateService.getGraduatesByPromotion(promotionId)).thenReturn(List.of(ranking));

    when(bucketComponent.presign(anyString(), eq(Duration.ofMinutes(15))))
        .thenReturn(new java.net.URL("https://example.com/null-fields.xlsx"));

    doAnswer(
            invocation -> {
              File file = invocation.getArgument(0);

              try (XSSFWorkbook workbook = (XSSFWorkbook) WorkbookFactory.create(file)) {

                var row = workbook.getSheet("Diplomes").getRow(1);

                assertThat(row.getCell(0).getNumericCellValue()).isEqualTo(1);

                assertThat(row.getCell(1).getStringCellValue()).isEmpty();

                assertThat(row.getCell(2).getStringCellValue()).isEmpty();

                assertThat(row.getCell(3).getStringCellValue()).isEmpty();

                assertThat(row.getCell(4).getStringCellValue()).isEmpty();

                assertThat(row.getCell(5).getNumericCellValue()).isEqualTo(12.5);

                assertThat(row.getCell(6).getNumericCellValue()).isEqualTo(60);
              }

              return null;
            })
        .when(bucketComponent)
        .upload(any(File.class), anyString());

    String result = graduateExportService.exportGraduatesToExcel(promotionId);

    assertThat(result).isEqualTo("https://example.com/null-fields.xlsx");

    verify(graduateService).getGraduatesByPromotion(promotionId);
    verify(bucketComponent).upload(any(File.class), anyString());
  }
}
