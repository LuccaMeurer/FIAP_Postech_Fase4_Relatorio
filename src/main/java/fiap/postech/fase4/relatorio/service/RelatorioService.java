package fiap.postech.fase4.relatorio.service;

import fiap.postech.fase4.relatorio.dto.*;
import fiap.postech.fase4.relatorio.repository.AvaliacaoRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;


import java.util.List;
import java.util.Map;

@Service
public class RelatorioService {

    private final AvaliacaoRepository repo;

    public RelatorioService(AvaliacaoRepository repo) {
        this.repo = repo;
    }

    public RelatorioResumoDTO gerarResumo(Integer dias) {

        LocalDateTime dataInicio = null;
        if (dias != null) {
            dataInicio = LocalDateTime.now().minusDays(dias);
        }

        TotalMediaDTO totalMedia = repo.totalEAvg(dataInicio);

        long total = totalMedia.total();
        double media = totalMedia.media() != null ? totalMedia.media() : 0.0;

        double score = media * 10.0;

        double mediana = calcularMediana(
                repo.listarNotasOrdenadas(dataInicio)
        );

        var estatisticas = new EstatisticasComMedianaDTO(
                total,
                arred2(media),
                arred2(mediana),
                arred2(score)
        );

        var porNota = repo.contarPorNota(dataInicio);

        var percentualPorNota = porNota.stream()
                .map(n -> new NotaPercentualDTO(
                        n.nota(),
                        n.quantidade(),
                        total == 0 ? 0.0 : arred2(n.quantidade() * 100.0 / total)
                ))
                .toList();

        var percentualPorGrupo = calcularPercentualPorGrupo(porNota, total);

        var top3 = percentualPorNota.stream()
                .sorted((a, b) -> Long.compare(b.quantidade(), a.quantidade()))
                .limit(3)
                .toList();

        return new RelatorioResumoDTO(
                estatisticas,
                percentualPorNota,
                percentualPorGrupo,
                top3
        );
    }


    private Map<String, Double> calcularPercentualPorGrupo(List<NotaQuantidadeDTO> porNota, long total) {
        long baixo = 0, medio = 0, alto = 0;

        for (var n : porNota) {
            int nota = n.nota() == null ? 0 : n.nota();
            long qtd = n.quantidade() == null ? 0 : n.quantidade();

            if (nota >= 1 && nota <= 4) baixo += qtd;
            else if (nota >= 5 && nota <= 7) medio += qtd;
            else if (nota >= 8 && nota <= 10) alto += qtd;
        }

        if (total == 0) {
            return Map.of("BAIXO", 0.0, "MEDIO", 0.0, "ALTO", 0.0);
        }

        return Map.of(
                "BAIXO", arred2(baixo * 100.0 / total),
                "MEDIO", arred2(medio * 100.0 / total),
                "ALTO", arred2(alto * 100.0 / total)
        );
    }

    private double calcularMediana(List<Integer> notasOrdenadas) {
        if (notasOrdenadas == null || notasOrdenadas.isEmpty()) return 0.0;

        int n = notasOrdenadas.size();
        if (n % 2 == 1) {
            return notasOrdenadas.get(n / 2);
        } else {
            int a = notasOrdenadas.get((n / 2) - 1);
            int b = notasOrdenadas.get(n / 2);
            return (a + b) / 2.0;
        }
    }

    private double arred2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}

