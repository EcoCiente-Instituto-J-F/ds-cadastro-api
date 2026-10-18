INSERT INTO tb_lkp_tipos_usuarios (
    id_tipo_usuario, nome_tipo
)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'Síndico'),
    (2, 'Cooperativa'),
    (3, 'Morador');

INSERT INTO tb_lkp_tipos_condominios (
    id_tipo_condominio, nome_tipo
)
OVERRIDING SYSTEM VALUE
VALUES (1, 'Residencial');

INSERT INTO tb_lkp_status_agendamentos (
    id_status, nome_status
)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'Agendado'),
    (2, 'Confirmado'),
    (3, 'Recusado'),
    (4, 'Realizado'),
    (5, 'Cancelado'),
    (11, 'CANCELADO'),
    (12, 'CONFIRMADO'),
    (13, 'REALIZADO'),
    (14, 'AGENDADO'),
    (15, 'RECUSADO');

INSERT INTO tb_enderecos (
    id_endereco, cep, estado, cidade, logradouro, numero
)
OVERRIDING SYSTEM VALUE
VALUES
    (1, '01001000', 'SP', 'São Paulo', 'Rua Um', '10'),
    (2, '01001000', 'SP', 'São Paulo', 'Rua Dois', '20');

INSERT INTO tb_usuarios (
    id_usuario, nome_usuario, email_usuario,
    senha_hash, ativo, tipo_usuario_id, endereco_id
)
OVERRIDING SYSTEM VALUE
VALUES
    (10, 'Síndico Um', 'sindico1@teste.com',
     'hash-teste', TRUE, 1, 1),
    (20, 'Cooperativa Um', 'coop1@teste.com',
     'hash-teste', TRUE, 2, 1),
    (30, 'Morador', 'morador@teste.com',
     'hash-teste', TRUE, 3, 1),
    (40, 'Síndico Dois', 'sindico2@teste.com',
     'hash-teste', TRUE, 1, 2),
    (50, 'Cooperativa Dois', 'coop2@teste.com',
     'hash-teste', TRUE, 2, 2);

INSERT INTO tb_sindicos (id_sindico, usuario_id)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 10),
    (2, 40);

INSERT INTO tb_condominios (
    id_condominio, nome_condominio,
    ativo, tipo_condominio_id, sindico_id, endereco_id
)
OVERRIDING SYSTEM VALUE
VALUES
    (1, 'Condomínio Um', TRUE, 1, 1, 1),
    (2, 'Condomínio Dois', TRUE, 1, 2, 2);

INSERT INTO tb_cooperativas (
    id_cooperativa, cnpj_cooperativa,
    nome_cooperativa, usuario_id
)
OVERRIDING SYSTEM VALUE
VALUES
    (1, '34238864000168', 'Cooperativa Um', 20),
    (2, '11222333000181', 'Cooperativa Dois', 50);