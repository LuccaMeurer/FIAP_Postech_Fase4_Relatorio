package fiap.postech.fase4.relatorio.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import fiap.postech.fase4.relatorio.SpringContext;
import fiap.postech.fase4.relatorio.dto.RelatorioResumoDTO;
import fiap.postech.fase4.relatorio.service.RelatorioService;
import fiap.postech.fase4.relatorio.util.ExcelRelatorioBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class RelatorioFunction {

    private Integer parseDias(HttpRequestMessage<Optional<String>> request) {
        String diasStr = request.getQueryParameters().get("dias");
        if (diasStr == null || diasStr.isBlank()) return null;
        return Integer.parseInt(diasStr);
    }

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
            RelatorioService service =
                    SpringContext.getBean(RelatorioService.class);

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
            RelatorioService service =
                    SpringContext.getBean(RelatorioService.class);

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
            RelatorioService service =
                    SpringContext.getBean(RelatorioService.class);

            Integer dias = parseDias(request);
            RelatorioResumoDTO resumo = service.gerarResumo(dias);

            // 🔥 ÚNICO LUGAR QUE GERA O EXCEL
            byte[] excel = ExcelRelatorioBuilder.gerar(resumo);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header(
                            "Content-Type",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    )
                    .header(
                            "Content-Disposition",
                            "attachment; filename=relatorio.xlsx"
                    )
                    .body(excel)
                    .build();

        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Erro Excel: " + e.getMessage()).getBytes(StandardCharsets.UTF_8))
                    .build();
        }
    }
}
