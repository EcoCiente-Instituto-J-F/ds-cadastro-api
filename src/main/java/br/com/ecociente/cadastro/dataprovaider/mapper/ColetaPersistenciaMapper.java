package br.com.ecociente.cadastro.dataprovaider.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Coleta;
import br.com.ecociente.cadastro.core.domain.StatusAgendamento;

@Component
public class ColetaPersistenciaMapper implements RowMapper<Coleta> {

  @Override
  public Coleta mapRow(ResultSet rs, int rowNum)
      throws SQLException {

    return new Coleta(
        rs.getInt("id_agendamento_coleta"),
        rs.getInt("condominio_id"),
        rs.getInt("cooperativa_id"),
        status(rs.getInt("status_agendamento_id")),
        rs.getObject("data_inicio", OffsetDateTime.class),
        rs.getObject("data_fim", OffsetDateTime.class),
        rs.getBoolean("possui_recorrencia"),
        rs.getObject("solicitado_por_usuario_id", Integer.class),
        rs.getString("origem_solicitacao"),
        rs.getObject("respondido_por_usuario_id", Integer.class),
        rs.getObject("respondido_em", OffsetDateTime.class));
  }

  private StatusAgendamento status(int id) {
    return switch (id) {
      case 1, 14 -> StatusAgendamento.AGENDADO;
      case 2, 12 -> StatusAgendamento.CONFIRMADO;
      case 3, 15 -> StatusAgendamento.RECUSADO;
      case 4, 13 -> StatusAgendamento.REALIZADO;
      case 5, 11 -> StatusAgendamento.CANCELADO;
      default -> throw new IllegalStateException(
          "Status de agendamento desconhecido: " + id);
    };
  }
}