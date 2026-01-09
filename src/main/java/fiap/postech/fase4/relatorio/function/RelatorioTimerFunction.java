package fiap.postech.fase4.relatorio.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import fiap.postech.fase4.relatorio.SpringContext;
import fiap.postech.fase4.relatorio.service.RelatorioService;
import fiap.postech.fase4.relatorio.service.EmailService;
import fiap.postech.fase4.relatorio.util.ExcelRelatorioBuilder;

public class RelatorioTimerFunction {

    @FunctionName("relatorioSemanal")
    public void executar(
            @TimerTrigger(
                    name = "timer",
                    schedule = "0 0 11 * * 1" // Executa a cada 5 minutos
                    // Executa toda segunda-feira às 08:00 (horário do Brasil - UTC-3)
                    // schedule = "0 */5 * * * *"
            )
            String timerInfo,
            ExecutionContext context
    ) {

        context.getLogger().severe("TIMER RELATORIO SEMANAL DISPAROU");

        try {
            context.getLogger().severe("⏳ Iniciando processo de relatório...");

            RelatorioService relatorioService =
                    SpringContext.getBean(RelatorioService.class);

            EmailService emailService =
                    SpringContext.getBean(EmailService.class);

            context.getLogger().severe("📊 Gerando resumo...");
            var resumo = relatorioService.gerarResumo(7);

            context.getLogger().severe("📄 Gerando Excel...");
            byte[] excel = ExcelRelatorioBuilder.gerar(resumo);

            String emailTo = System.getenv("EMAIL_TO");
            context.getLogger().severe("📧 EMAIL_TO = " + emailTo);

            emailService.enviarRelatorio(excel, emailTo);

            context.getLogger().severe("✅ EMAIL ENVIADO COM SUCESSO");

        } catch (Exception e) {
            context.getLogger().severe("❌ ERRO NO TIMER");
            e.printStackTrace();
        }
    }
}
