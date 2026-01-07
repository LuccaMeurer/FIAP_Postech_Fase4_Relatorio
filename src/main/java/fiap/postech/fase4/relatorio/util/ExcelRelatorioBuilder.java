package fiap.postech.fase4.relatorio.util;

import fiap.postech.fase4.relatorio.dto.RelatorioResumoDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;

public class ExcelRelatorioBuilder {

    public static byte[] gerar(RelatorioResumoDTO resumo) throws Exception {

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
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // =========================
        // ABA 1 — RESUMO
        // =========================
        Sheet resumoSheet = workbook.createSheet("Resumo");

        Row titleRow = resumoSheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RELATÓRIO DE AVALIAÇÕES");
        titleCell.setCellStyle(titleStyle);
        resumoSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        Row h = resumoSheet.createRow(2);
        h.createCell(0).setCellValue("Métrica");
        h.createCell(1).setCellValue("Valor");
        h.getCell(0).setCellStyle(headerStyle);
        h.getCell(1).setCellStyle(headerStyle);

        resumoSheet.createRow(3).createCell(0).setCellValue("Total");
        resumoSheet.getRow(3).createCell(1).setCellValue(resumo.estatisticas().total());

        resumoSheet.createRow(4).createCell(0).setCellValue("Média");
        resumoSheet.getRow(4).createCell(1).setCellValue(resumo.estatisticas().media());

        resumoSheet.createRow(5).createCell(0).setCellValue("Mediana");
        resumoSheet.getRow(5).createCell(1).setCellValue(resumo.estatisticas().mediana());

        resumoSheet.createRow(6).createCell(0).setCellValue("Score");
        resumoSheet.getRow(6).createCell(1).setCellValue(resumo.estatisticas().score());

        resumoSheet.autoSizeColumn(0);
        resumoSheet.autoSizeColumn(1);

        // =========================
        // ABA 2 — NOTAS
        // =========================
        Sheet notasSheet = workbook.createSheet("Notas");

        Row nh = notasSheet.createRow(0);
        nh.createCell(0).setCellValue("Nota");
        nh.createCell(1).setCellValue("Quantidade");
        nh.createCell(2).setCellValue("Percentual");

        for (int i = 0; i < 3; i++) {
            nh.getCell(i).setCellStyle(headerStyle);
        }

        int row = 1;
        for (var n : resumo.percentualPorNota()) {
            Row r = notasSheet.createRow(row++);
            r.createCell(0).setCellValue(n.nota());
            r.createCell(1).setCellValue(n.quantidade());
            r.createCell(2).setCellValue(n.percentual());
        }

        notasSheet.autoSizeColumn(0);
        notasSheet.autoSizeColumn(1);
        notasSheet.autoSizeColumn(2);

        // =========================
        // ABA 3 — GRUPOS
        // =========================
        Sheet gruposSheet = workbook.createSheet("Grupos");

        Row gh = gruposSheet.createRow(0);
        gh.createCell(0).setCellValue("Grupo");
        gh.createCell(1).setCellValue("Percentual");
        gh.getCell(0).setCellStyle(headerStyle);
        gh.getCell(1).setCellStyle(headerStyle);

        row = 1;
        for (var g : resumo.percentualPorGrupo().entrySet()) {
            Row r = gruposSheet.createRow(row++);
            r.createCell(0).setCellValue(g.getKey());
            r.createCell(1).setCellValue(g.getValue());
        }

        gruposSheet.autoSizeColumn(0);
        gruposSheet.autoSizeColumn(1);

        // =========================
        // ABA 4 — TOP NOTAS
        // =========================
        Sheet topSheet = workbook.createSheet("Top Notas");

        Row th = topSheet.createRow(0);
        th.createCell(0).setCellValue("Nota");
        th.createCell(1).setCellValue("Quantidade");
        th.createCell(2).setCellValue("Percentual");

        for (int i = 0; i < 3; i++) {
            th.getCell(i).setCellStyle(headerStyle);
        }

        row = 1;
        for (var n : resumo.top3Notas()) {
            Row r = topSheet.createRow(row++);
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

        return out.toByteArray();
    }
}
