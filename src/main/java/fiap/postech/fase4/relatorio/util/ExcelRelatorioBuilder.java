package fiap.postech.fase4.relatorio.util;

import fiap.postech.fase4.relatorio.dto.RelatorioResumoDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.Map;

public class ExcelRelatorioBuilder {

    public static byte[] gerar(RelatorioResumoDTO resumo) throws Exception {
        Workbook workbook = new XSSFWorkbook();

        // ===== Resumo =====
        Sheet resumoSheet = workbook.createSheet("Resumo");

        resumoSheet.createRow(0).createCell(0)
                .setCellValue("RELATÓRIO DE AVALIAÇÕES");

        resumoSheet.createRow(2).createCell(0).setCellValue("Total");
        resumoSheet.getRow(2).createCell(1)
                .setCellValue(resumo.estatisticas().total());

        resumoSheet.createRow(3).createCell(0).setCellValue("Média");
        resumoSheet.getRow(3).createCell(1)
                .setCellValue(resumo.estatisticas().media());

        resumoSheet.createRow(4).createCell(0).setCellValue("Mediana");
        resumoSheet.getRow(4).createCell(1)
                .setCellValue(resumo.estatisticas().mediana());

        resumoSheet.createRow(5).createCell(0).setCellValue("Score");
        resumoSheet.getRow(5).createCell(1)
                .setCellValue(resumo.estatisticas().score());

        resumoSheet.autoSizeColumn(0);
        resumoSheet.autoSizeColumn(1);

        // ===== Notas =====
        Sheet notasSheet = workbook.createSheet("Notas");

        notasSheet.createRow(0).createCell(0).setCellValue("Nota");
        notasSheet.getRow(0).createCell(1).setCellValue("Quantidade");
        notasSheet.getRow(0).createCell(2).setCellValue("Percentual");

        int i = 1;
        for (var n : resumo.percentualPorNota()) {
            Row r = notasSheet.createRow(i++);
            r.createCell(0).setCellValue(n.nota());
            r.createCell(1).setCellValue(n.quantidade());
            r.createCell(2).setCellValue(n.percentual());
        }

        notasSheet.autoSizeColumn(0);
        notasSheet.autoSizeColumn(1);
        notasSheet.autoSizeColumn(2);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }
}
