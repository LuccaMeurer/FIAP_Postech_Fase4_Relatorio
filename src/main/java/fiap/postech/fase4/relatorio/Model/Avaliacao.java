package fiap.postech.fase4.relatorio.Model;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacao")
@Getter
public class Avaliacao {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "nota")
    private Integer nota;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    protected Avaliacao() {}
}

