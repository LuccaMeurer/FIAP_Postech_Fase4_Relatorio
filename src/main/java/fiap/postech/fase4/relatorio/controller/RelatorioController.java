package fiap.postech.fase4.relatorio.controller;

import fiap.postech.fase4.relatorio.dto.RelatorioResumoDTO;
import fiap.postech.fase4.relatorio.service.RelatorioService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/relatorio")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @GetMapping("/resumo")
    public RelatorioResumoDTO resumo(
            @org.springframework.web.bind.annotation.RequestParam(required = false)
            Integer dias
    ) {
        return service.gerarResumo(dias);
    }

    @GetMapping(value = "/csv", produces = "text/csv")
    public void csv(
            @org.springframework.web.bind.annotation.RequestParam(required = false)
            Integer dias,
            jakarta.servlet.http.HttpServletResponse response
    ) throws Exception {

        RelatorioResumoDTO resumo = service.gerarResumo(dias);

        response.setContentType("text/csv");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=relatorio.csv"
        );

        var w = response.getWriter();

        w.println("TOTAL,MEDIA,MEDIANA,SCORE");
        w.printf(
                "%d,%.2f,%.2f,%.2f%n",
                resumo.estatisticas().total(),
                resumo.estatisticas().media(),
                resumo.estatisticas().mediana(),
                resumo.estatisticas().score()
        );

        w.println();
        w.println("NOTA,QUANTIDADE,PERCENTUAL");

        for (var n : resumo.percentualPorNota()) {
            w.printf(
                    "%d,%d,%.2f%n",
                    n.nota(),
                    n.quantidade(),
                    n.percentual()
            );
        }

        w.flush();
    }

    @GetMapping("/excel")
    public void excel(
            @org.springframework.web.bind.annotation.RequestParam(required = false)
            Integer dias,
            jakarta.servlet.http.HttpServletResponse response
    ) throws Exception {

        RelatorioResumoDTO resumo = service.gerarResumo(dias);

        var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();


        var titleStyle = workbook.createCellStyle();
        var titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);

        var headerStyle = workbook.createCellStyle();
        var headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(
                org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex()
        );
        headerStyle.setFillPattern(
                org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND
        );
        headerStyle.setAlignment(
                org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER
        );

        var numberStyle = workbook.createCellStyle();
        numberStyle.setDataFormat(
                workbook.createDataFormat().getFormat("0.00")
        );


        var sheet = workbook.createSheet("Resumo");
        int rowIdx = 0;

        // Título
        Row titleRow = sheet.createRow(rowIdx++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RELATÓRIO DE AVALIAÇÕES");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(
                new CellRangeAddress(0, 0, 0, 1)
        );

        rowIdx++;

        // Cabeçalho
        Row header = sheet.createRow(rowIdx++);
        Cell h1 = header.createCell(0);
        Cell h2 = header.createCell(1);

        h1.setCellValue("Métrica");
        h2.setCellValue("Valor");

        h1.setCellStyle(headerStyle);
        h2.setCellStyle(headerStyle);

        // Dados
        Row r1 = sheet.createRow(rowIdx++);
        r1.createCell(0).setCellValue("Total");
        r1.createCell(1).setCellValue(resumo.estatisticas().total());

        Row r2 = sheet.createRow(rowIdx++);
        r2.createCell(0).setCellValue("Média");
        Cell c2 = r2.createCell(1);
        c2.setCellValue(resumo.estatisticas().media());
        c2.setCellStyle(numberStyle);

        Row r3 = sheet.createRow(rowIdx++);
        r3.createCell(0).setCellValue("Mediana");
        Cell c3 = r3.createCell(1);
        c3.setCellValue(resumo.estatisticas().mediana());
        c3.setCellStyle(numberStyle);

        Row r4 = sheet.createRow(rowIdx++);
        r4.createCell(0).setCellValue("Score (0–100)");
        Cell c4 = r4.createCell(1);
        c4.setCellValue(resumo.estatisticas().score());
        c4.setCellStyle(numberStyle);

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);


        var notasSheet = workbook.createSheet("Notas");

        Row notasHeader = notasSheet.createRow(0);
        Cell nh0 = notasHeader.createCell(0);
        Cell nh1 = notasHeader.createCell(1);
        Cell nh2 = notasHeader.createCell(2);

        nh0.setCellValue("Nota");
        nh1.setCellValue("Quantidade");
        nh2.setCellValue("Percentual");

        nh0.setCellStyle(headerStyle);
        nh1.setCellStyle(headerStyle);
        nh2.setCellStyle(headerStyle);

        int i = 1;
        for (var n : resumo.percentualPorNota()) {
            Row r = notasSheet.createRow(i++);
            r.createCell(0).setCellValue(n.nota());
            r.createCell(1).setCellValue(n.quantidade());

            Cell p = r.createCell(2);
            p.setCellValue(n.percentual());
            p.setCellStyle(numberStyle);
        }

        notasSheet.autoSizeColumn(0);
        notasSheet.autoSizeColumn(1);
        notasSheet.autoSizeColumn(2);

        var gruposSheet = workbook.createSheet("Grupos");

        Row gh = gruposSheet.createRow(0);
        Cell g0 = gh.createCell(0);
        Cell g1 = gh.createCell(1);
        Cell g2 = gh.createCell(2);

        g0.setCellValue("Grupo");
        g1.setCellValue("Intervalo");
        g2.setCellValue("Percentual");

        g0.setCellStyle(headerStyle);
        g1.setCellStyle(headerStyle);
        g2.setCellStyle(headerStyle);

        Map<String, String> intervalos = Map.of(
                "BAIXO", "1–4",
                "MEDIO", "5–7",
                "ALTO", "8–10"
        );

        int gr = 1;
        for (var e : resumo.percentualPorGrupo().entrySet()) {
            Row r = gruposSheet.createRow(gr++);
            r.createCell(0).setCellValue(e.getKey());
            r.createCell(1).setCellValue(intervalos.get(e.getKey()));

            Cell p = r.createCell(2);
            p.setCellValue(e.getValue());
            p.setCellStyle(numberStyle);
        }

        gruposSheet.autoSizeColumn(0);
        gruposSheet.autoSizeColumn(1);
        gruposSheet.autoSizeColumn(2);

        var topSheet = workbook.createSheet("Top Notas");

        Row th = topSheet.createRow(0);
        Cell t0 = th.createCell(0);
        Cell t1 = th.createCell(1);
        Cell t2 = th.createCell(2);

        t0.setCellValue("Nota");
        t1.setCellValue("Quantidade");
        t2.setCellValue("Percentual");

        t0.setCellStyle(headerStyle);
        t1.setCellStyle(headerStyle);
        t2.setCellStyle(headerStyle);

        int tr = 1;
        for (var n : resumo.top3Notas()) {
            Row r = topSheet.createRow(tr++);
            r.createCell(0).setCellValue(n.nota());
            r.createCell(1).setCellValue(n.quantidade());

            Cell p = r.createCell(2);
            p.setCellValue(n.percentual());
            p.setCellStyle(numberStyle);
        }

        topSheet.autoSizeColumn(0);
        topSheet.autoSizeColumn(1);
        topSheet.autoSizeColumn(2);

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=relatorio.xlsx"
        );

        workbook.write(response.getOutputStream());
        workbook.close();
    }



}

