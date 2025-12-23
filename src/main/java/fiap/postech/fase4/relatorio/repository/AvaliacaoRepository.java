package fiap.postech.fase4.relatorio.repository;

import fiap.postech.fase4.relatorio.Model.Avaliacao;
import fiap.postech.fase4.relatorio.dto.NotaQuantidadeDTO;
import fiap.postech.fase4.relatorio.dto.TotalMediaDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    @Query("""
        select count(a.id) as total,
               avg(a.nota)  as media
        from Avaliacao a
        where (:dataInicio is null or a.dataEnvio >= :dataInicio)
    """)
    TotalMediaDTO totalEAvg(
            @Param("dataInicio") LocalDateTime dataInicio
    );

    @Query("""
        select a.nota as nota,
               count(a.id) as quantidade
        from Avaliacao a
        where (:dataInicio is null or a.dataEnvio >= :dataInicio)
        group by a.nota
    """)
    List<NotaQuantidadeDTO> contarPorNota(
            @Param("dataInicio") LocalDateTime dataInicio
    );

    @Query("""
        select a.nota
        from Avaliacao a
        where (:dataInicio is null or a.dataEnvio >= :dataInicio)
        order by a.nota
    """)
    List<Integer> listarNotasOrdenadas(
            @Param("dataInicio") LocalDateTime dataInicio
    );
}


