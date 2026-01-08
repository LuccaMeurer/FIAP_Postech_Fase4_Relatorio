package fiap.postech.fase4.relatorio.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import fiap.postech.fase4.relatorio.SpringContext;
import fiap.postech.fase4.relatorio.dto.RelatorioResumoDTO;
import fiap.postech.fase4.relatorio.service.EmailService;
import fiap.postech.fase4.relatorio.service.RelatorioService;
import fiap.postech.fase4.relatorio.util.ExcelRelatorioBuilder;

import java.util.Optional;

public class EmailTestFunction {

    @FunctionName("emailTeste")
    public HttpResponseMessage emailTeste(
            @HttpTrigger(
                    name = "req",
                    methods = HttpMethod.GET,
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "email/teste"
            )
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context
    ) {

        try {
            RelatorioService relatorioService =
                    SpringContext.getBean(RelatorioService.class);

            EmailService emailService =
                    SpringContext.getBean(EmailService.class);

            RelatorioResumoDTO resumo =
                    relatorioService.gerarResumo(null);

            byte[] excel =
                    ExcelRelatorioBuilder.gerar(resumo);

            context.getLogger().info("Excel bytes = " + excel.length);

            emailService.enviarRelatorio(
                    excel,
                    "lucca.meurer@sempreceub.com"
            );

            return request.createResponseBuilder(HttpStatus.OK)
                    .body("Email enviado com sucesso!")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao enviar email: " + e.getMessage())
                    .build();
        }
    }
}
