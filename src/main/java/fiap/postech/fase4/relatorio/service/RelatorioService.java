package fiap.postech.fase4.relatorio.service;

import fiap.postech.fase4.relatorio.dto.EstatisticasComMedianaDTO;
import fiap.postech.fase4.relatorio.dto.NotaPercentualDTO;
import fiap.postech.fase4.relatorio.dto.RelatorioResumoDTO;
import fiap.postech.fase4.relatorio.repository.AvaliacaoRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RelatorioService {

    private final AvaliacaoRepository repository;

    public RelatorioService(AvaliacaoRepository repository) {
        this.repository = repository;
    }

    public RelatorioResumoDTO gerarResumo(Integer dias) {

        Map<Integer, Integer> notas = repository.contarPorNota(dias);

        int total = notas.values().stream().mapToInt(Integer::intValue).sum();
        double media = calcularMedia(notas, total);
        double mediana = calcularMediana(notas, total);
        double score = media * 10;

        List<NotaPercentualDTO> percentualPorNota = new ArrayList<>();
        for (var e : notas.entrySet()) {
            double percentual = total == 0 ? 0 : (e.getValue() * 100.0) / total;
            percentualPorNota.add(
                    new NotaPercentualDTO(
                            e.getKey(),
                            e.getValue().longValue(),
                            percentual
                    )
            );
        }

        percentualPorNota.sort(
                Comparator.comparing(NotaPercentualDTO::quantidade).reversed()
        );

        List<NotaPercentualDTO> top3Notas =
                percentualPorNota.stream().limit(3).toList();

        Map<String, Double> percentualPorGrupo = Map.of(
                "BAIXO", calcularGrupo(percentualPorNota, 1, 4),
                "MEDIO", calcularGrupo(percentualPorNota, 5, 7),
                "ALTO", calcularGrupo(percentualPorNota, 8, 10)
        );

        EstatisticasComMedianaDTO estatisticas =
                new EstatisticasComMedianaDTO(total, media, mediana, score);

        return new RelatorioResumoDTO(
                estatisticas,
                percentualPorNota,
                percentualPorGrupo,
                top3Notas
        );
    }

    private double calcularMedia(Map<Integer, Integer> notas, int total) {
        if (total == 0) return 0;
        return notas.entrySet().stream()
                .mapToDouble(e -> e.getKey() * e.getValue())
                .sum() / total;
    }

    private double calcularMediana(Map<Integer, Integer> notas, int total) {
        if (total == 0) return 0;

        int meio = total / 2;
        int acumulado = 0;

        for (var e : notas.entrySet()) {
            acumulado += e.getValue();
            if (acumulado >= meio) {
                return e.getKey();
            }
        }
        return 0;
    }

    private double calcularGrupo(
            List<NotaPercentualDTO> lista,
            int min,
            int max
    ) {
        return lista.stream()
                .filter(n -> n.nota() >= min && n.nota() <= max)
                .mapToDouble(NotaPercentualDTO::percentual)
                .sum();
    }
}
