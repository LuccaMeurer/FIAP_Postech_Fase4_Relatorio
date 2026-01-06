package fiap.postech.fase4.relatorio.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import fiap.postech.fase4.relatorio.SpringContext;
import fiap.postech.fase4.relatorio.dto.RelatorioResumoDTO;
import fiap.postech.fase4.relatorio.service.RelatorioService;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class RelatorioFunction {

    private Integer parseDias(HttpRequestMessage<Optional<String>> request) {
        String diasStr = request.getQueryParameters().get("dias");
        if (diasStr == null || diasStr.isBlank()) return null;
        return Integer.parseInt(diasStr);
    }

    // =========================
    // JSON
    // =========================
    @FunctionName("relatorioResumo")
    public HttpResponseMessage resumo(
            @HttpTrigger(
                    name = "req",
                    methods = HttpMethod.GET,
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "relatorio/resumo"
            )
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context
    ) {
        try {
            context.getLogger().info("relatorioResumo chamada");

            RelatorioService service = SpringContext.getBean(RelatorioService.class);
            Integer dias = parseDias(request);

            RelatorioResumoDTO resumo = service.gerarResumo(dias);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(resumo)
                    .build();

        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro: " + e.getMessage())
                    .build();
        }
    }

    // =========================
    // CSV
    // =========================
    @FunctionName("relatorioCsv")
    public HttpResponseMessage csv(
            @HttpTrigger(
                    name = "req",
                    methods = HttpMethod.GET,
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "relatorio/csv"
            )
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context
    ) {
        try {
            RelatorioService service = SpringContext.getBean(RelatorioService.class);
            Integer dias = parseDias(request);

            RelatorioResumoDTO resumo = service.gerarResumo(dias);

            StringBuilder csv = new StringBuilder();
            csv.append("TOTAL,MEDIA,MEDIANA,SCORE\n");
            csv.append(String.format(
                    "%d,%.2f,%.2f,%.2f%n",
                    resumo.estatisticas().total(),
                    resumo.estatisticas().media(),
                    resumo.estatisticas().mediana(),
                    resumo.estatisticas().score()
            ));

            csv.append("\nNOTA,QUANTIDADE,PERCENTUAL\n");
            resumo.percentualPorNota().forEach(n ->
                    csv.append(String.format(
                            "%d,%d,%.2f%n",
                            n.nota(), n.quantidade(), n.percentual()
                    ))
            );

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "text/csv; charset=utf-8")
                    .header("Content-Disposition", "attachment; filename=relatorio.csv")
                    .body(csv.toString().getBytes(StandardCharsets.UTF_8))
                    .build();

        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erro CSV: " + e.getMessage()).getBytes(StandardCharsets.UTF_8))
                    .build();
        }
    }

    // =========================
    // EXCEL
    // =========================
    @FunctionName("relatorioExcel")
    public HttpResponseMessage excel(
            @HttpTrigger(
                    name = "req",
                    methods = HttpMethod.GET,
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "relatorio/excel"
            )
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context
    ) {
        try {
            RelatorioService service = SpringContext.getBean(RelatorioService.class);
            Integer dias = parseDias(request);
            RelatorioResumoDTO resumo = service.gerarResumo(dias);

            Workbook workbook = new XSSFWorkbook();

            // =========================
            // ESTILOS
            // =========================
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(
                    workbook.createDataFormat().getFormat("0.00")
            );

            // =========================
            // ABA: RESUMO
            // =========================
            Sheet resumoSheet = workbook.createSheet("Resumo");
            int rowIdx = 0;

            Row titleRow = resumoSheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("RELATÓRIO DE AVALIAÇÕES");
            titleCell.setCellStyle(titleStyle);
            resumoSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

            rowIdx++;

            Row header = resumoSheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("Métrica");
            header.createCell(1).setCellValue("Valor");
            header.getCell(0).setCellStyle(headerStyle);
            header.getCell(1).setCellStyle(headerStyle);

            Row r1 = resumoSheet.createRow(rowIdx++);
            r1.createCell(0).setCellValue("Total");
            r1.createCell(1).setCellValue(resumo.estatisticas().total());

            Row r2 = resumoSheet.createRow(rowIdx++);
            r2.createCell(0).setCellValue("Média");
            r2.createCell(1).setCellValue(resumo.estatisticas().media());

            Row r3 = resumoSheet.createRow(rowIdx++);
            r3.createCell(0).setCellValue("Mediana");
            r3.createCell(1).setCellValue(resumo.estatisticas().mediana());

            Row r4 = resumoSheet.createRow(rowIdx++);
            r4.createCell(0).setCellValue("Score");
            r4.createCell(1).setCellValue(resumo.estatisticas().score());

            resumoSheet.autoSizeColumn(0);
            resumoSheet.autoSizeColumn(1);

            // =========================
            // ABA: NOTAS
            // =========================
            Sheet notasSheet = workbook.createSheet("Notas");

            Row nh = notasSheet.createRow(0);
            nh.createCell(0).setCellValue("Nota");
            nh.createCell(1).setCellValue("Quantidade");
            nh.createCell(2).setCellValue("Percentual");

            for (int i = 0; i < 3; i++) {
                nh.getCell(i).setCellStyle(headerStyle);
            }

            int nRow = 1;
            for (var n : resumo.percentualPorNota()) {
                Row r = notasSheet.createRow(nRow++);
                r.createCell(0).setCellValue(n.nota());
                r.createCell(1).setCellValue(n.quantidade());
                r.createCell(2).setCellValue(n.percentual());
            }

            notasSheet.autoSizeColumn(0);
            notasSheet.autoSizeColumn(1);
            notasSheet.autoSizeColumn(2);

            // =========================
            // ABA: GRUPOS
            // =========================
            Sheet gruposSheet = workbook.createSheet("Grupos");

            Row gh = gruposSheet.createRow(0);
            gh.createCell(0).setCellValue("Grupo");
            gh.createCell(1).setCellValue("Percentual");

            gh.getCell(0).setCellStyle(headerStyle);
            gh.getCell(1).setCellStyle(headerStyle);

            int gRow = 1;
            for (var e : resumo.percentualPorGrupo().entrySet()) {
                Row r = gruposSheet.createRow(gRow++);
                r.createCell(0).setCellValue(e.getKey());
                r.createCell(1).setCellValue(e.getValue());
            }

            gruposSheet.autoSizeColumn(0);
            gruposSheet.autoSizeColumn(1);

            // =========================
            // ABA: TOP NOTAS
            // =========================
            Sheet topSheet = workbook.createSheet("Top Notas");

            Row th = topSheet.createRow(0);
            th.createCell(0).setCellValue("Nota");
            th.createCell(1).setCellValue("Quantidade");
            th.createCell(2).setCellValue("Percentual");

            for (int i = 0; i < 3; i++) {
                th.getCell(i).setCellStyle(headerStyle);
            }

            int tRow = 1;
            for (var n : resumo.top3Notas()) {
                Row r = topSheet.createRow(tRow++);
                r.createCell(0).setCellValue(n.nota());
                r.createCell(1).setCellValue(n.quantidade());
                r.createCell(2).setCellValue(n.percentual());
            }

            topSheet.autoSizeColumn(0);
            topSheet.autoSizeColumn(1);
            topSheet.autoSizeColumn(2);

            // =========================
            // OUTPUT
            // =========================
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            workbook.close();

            return request.createResponseBuilder(HttpStatus.OK)
                    .header(
                            "Content-Type",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
                    .header(
                            "Content-Disposition",
                            "attachment; filename=relatorio.xlsx"
                    )
                    .body(out.toByteArray())
                    .build();

        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erro Excel: " + e.getMessage()).getBytes())
                    .build();
        }
    }

}
