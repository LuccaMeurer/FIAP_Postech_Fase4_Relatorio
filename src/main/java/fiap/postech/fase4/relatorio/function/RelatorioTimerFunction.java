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
                    schedule = "0 */1 * * * *" // a cada 1 minuto
            )
            String timerInfo,
            ExecutionContext context
    ) {

        // 🔥 LOG ABSOLUTO – SE NÃO APARECER, O TIMER NÃO EXECUTOU
        context.getLogger().severe("🔥 TIMER RELATORIO SEMANAL DISPAROU 🔥");

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
