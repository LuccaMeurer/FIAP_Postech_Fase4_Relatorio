package fiap.postech.fase4.relatorio;

import fiap.postech.fase4.relatorio.service.EmailService;
import fiap.postech.fase4.relatorio.service.RelatorioService;
import fiap.postech.fase4.relatorio.util.ExcelRelatorioBuilder;

public class RelatorioJobMain {

    public static void main(String[] args) throws Exception {

        System.out.println("🚀 JOB DE RELATÓRIO INICIADO");

        RelatorioService relatorioService =
                SpringContext.getBean(RelatorioService.class);

        EmailService emailService =
                SpringContext.getBean(EmailService.class);

        var resumo = relatorioService.gerarResumo(7);

        byte[] excel = ExcelRelatorioBuilder.gerar(resumo);

        emailService.enviarRelatorio(
                excel,
                System.getenv("EMAIL_TO")
        );

        System.out.println("✅ EMAIL ENVIADO COM SUCESSO");
    }
}
