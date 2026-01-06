package fiap.postech.fase4.relatorio.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import fiap.postech.fase4.relatorio.SpringContext;
import fiap.postech.fase4.relatorio.service.RelatorioService;
import fiap.postech.fase4.relatorio.service.EmailService;
import fiap.postech.fase4.relatorio.util.ExcelRelatorioBuilder;

public class RelatorioTimerFunction {

    @FunctionName("relatorioSemanal")
    public void executar(
            @TimerTrigger(
                    name = "timer",
                    schedule = "0 0 11 * * 1" // segunda 08h BR
            )
            String timerInfo,
            ExecutionContext context
    ) {
        try {
            RelatorioService relatorioService =
                    SpringContext.getBean(RelatorioService.class);
            EmailService emailService =
                    SpringContext.getBean(EmailService.class);

            var resumo = relatorioService.gerarResumo(null);
            byte[] excel = ExcelRelatorioBuilder.gerar(resumo);

            emailService.enviarRelatorio(
                    excel,
                    System.getenv("EMAIL_TO")
            );

            context.getLogger()
                    .info("Relatório semanal enviado com sucesso");

        } catch (Exception e) {
            context.getLogger()
                    .severe("Erro no relatório semanal: " + e.getMessage());
        }
    }
}
