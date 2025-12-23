package fiap.postech.fase4.relatorio.dto;

import java.util.List;
import java.util.Map;

public record RelatorioResumoDTO(
        EstatisticasComMedianaDTO estatisticas,
        List<NotaPercentualDTO> percentualPorNota,
        Map<String, Double> percentualPorGrupo,
        List<NotaPercentualDTO> top3Notas
) {}

