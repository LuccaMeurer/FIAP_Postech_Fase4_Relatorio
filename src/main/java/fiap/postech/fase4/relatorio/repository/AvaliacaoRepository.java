package fiap.postech.fase4.relatorio.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class AvaliacaoRepository {

    private final JdbcTemplate jdbcTemplate;

    public AvaliacaoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<Integer, Integer> contarPorNota(Integer dias) {

        StringBuilder sql = new StringBuilder("""
            SELECT nota, COUNT(*) qtd
            FROM avaliacao
        """);

        Object[] params;

        if (dias != null) {
            sql.append(" WHERE data_envio >= DATE_SUB(NOW(), INTERVAL ? DAY)");
            params = new Object[]{dias};
        } else {
            params = new Object[]{};
        }

        sql.append(" GROUP BY nota");

        return jdbcTemplate.query(
                sql.toString(),
                params,
                rs -> {
                    Map<Integer, Integer> map = new HashMap<>();
                    while (rs.next()) {
                        map.put(
                                rs.getInt("nota"),
                                rs.getInt("qtd")
                        );
                    }
                    return map;
                }
        );
    }
}