package br.com.ecociente.cadastro.core.domain;

public enum StatusAgendamento {

  AGENDADO(1),
  CONFIRMADO(2),
  RECUSADO(3),
  REALIZADO(4),
  CANCELADO(5);

  private final int id;

  StatusAgendamento(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }
}