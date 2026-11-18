-- =====================================================================
-- Base de Dados: OLHO VIVO  (Plataforma de denuncias comunitarias - PRM)
-- Compativel com MariaDB 10.4+ (XAMPP) / MySQL 8
--
-- COMO EXECUTAR (escolher UMA opcao):
--  A) XAMPP > Shell:  mysql -u root < caminho/olho_vivo.sql
--  B) phpMyAdmin > separador "Importar" > escolher este ficheiro > Executar
--  C) MySQL Workbench: File > Open SQL Script > Execute All (icone do raio)
-- NAO executar linha a linha: os blocos DELIMITER // ... // tem de correr inteiros.
-- Ferramentas que nao entendem DELIMITER falham neste ficheiro.
-- =====================================================================
SET NAMES utf8mb4;
DROP DATABASE IF EXISTS olho_vivo;
CREATE DATABASE olho_vivo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE olho_vivo;

-- ---------------------------------------------------------------------
-- 1. TABELAS
-- ---------------------------------------------------------------------
CREATE TABLE utilizador (
  id_utilizador  INT AUTO_INCREMENT PRIMARY KEY,
  nome           VARCHAR(60)  NOT NULL,
  contacto       VARCHAR(14)  NOT NULL UNIQUE,
  email          VARCHAR(60)  NULL UNIQUE,
  senha_hash     VARCHAR(255) NOT NULL,
  perfil         ENUM('CIDADAO','AGENTE','ADMINISTRADOR') NOT NULL,
  activo         TINYINT(1)   NOT NULL DEFAULT 1,
  data_registo   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE cidadao (
  id_cidadao          INT AUTO_INCREMENT PRIMARY KEY,
  id_utilizador       INT NOT NULL UNIQUE,
  pontuacao_confianca INT NOT NULL DEFAULT 0,
  CONSTRAINT ck_confianca CHECK (pontuacao_confianca >= 0),
  CONSTRAINT fk_cidadao_utilizador FOREIGN KEY (id_utilizador)
    REFERENCES utilizador(id_utilizador)
) ENGINE=InnoDB;

CREATE TABLE administrador (
  id_administrador INT AUTO_INCREMENT PRIMARY KEY,
  id_utilizador    INT NOT NULL UNIQUE,
  CONSTRAINT fk_admin_utilizador FOREIGN KEY (id_utilizador)
    REFERENCES utilizador(id_utilizador)
) ENGINE=InnoDB;

CREATE TABLE esquadra (
  id_esquadra      INT AUTO_INCREMENT PRIMARY KEY,
  nome             VARCHAR(60)  NOT NULL UNIQUE,
  contacto         VARCHAR(14)  NOT NULL,
  endereco         VARCHAR(100) NOT NULL,
  area_jurisdicao  VARCHAR(100) NOT NULL,
  latitude         DECIMAL(9,6) NOT NULL,
  longitude        DECIMAL(9,6) NOT NULL,
  CONSTRAINT ck_esq_lat CHECK (latitude  BETWEEN -90  AND 90),
  CONSTRAINT ck_esq_lon CHECK (longitude BETWEEN -180 AND 180)
) ENGINE=InnoDB;

CREATE TABLE agente_policial (
  matricula      VARCHAR(12) PRIMARY KEY,
  id_utilizador  INT NOT NULL UNIQUE,
  cargo          VARCHAR(30) NOT NULL,
  id_esquadra    INT NOT NULL,
  CONSTRAINT fk_agente_utilizador FOREIGN KEY (id_utilizador)
    REFERENCES utilizador(id_utilizador),
  CONSTRAINT fk_agente_esquadra FOREIGN KEY (id_esquadra)
    REFERENCES esquadra(id_esquadra)
) ENGINE=InnoDB;

CREATE TABLE categoria_ocorrencia (
  id_categoria      INT AUTO_INCREMENT PRIMARY KEY,
  nome_categoria    VARCHAR(40) NOT NULL UNIQUE,
  nivel_gravidade   TINYINT NOT NULL,          -- 1 Baixa, 2 Media, 3 Alta
  CONSTRAINT ck_nivel CHECK (nivel_gravidade BETWEEN 1 AND 3)
) ENGINE=InnoDB;

CREATE TABLE denuncia (
  id_denuncia      INT AUTO_INCREMENT PRIMARY KEY,
  id_cidadao       INT NULL,                   -- NULL quando anonima
  id_categoria     INT NOT NULL,
  id_esquadra      INT NULL,                   -- preenchido automaticamente (trigger)
  data_hora        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  descricao        VARCHAR(500) NOT NULL,
  lat_ocorrencia   DECIMAL(9,6) NOT NULL,
  lon_ocorrencia   DECIMAL(9,6) NOT NULL,
  lat_denunciante  DECIMAL(9,6) NULL,
  lon_denunciante  DECIMAL(9,6) NULL,
  gravidade        TINYINT NULL,               -- por omissao = nivel da categoria
  emergencia       TINYINT(1) NOT NULL DEFAULT 0,
  anonima          TINYINT(1) NOT NULL DEFAULT 0,
  estado           ENUM('Pendente','Em Analise','Validada','Rejeitada','Resolvida')
                   NOT NULL DEFAULT 'Pendente',
  CONSTRAINT ck_den_gravidade CHECK (gravidade IS NULL OR gravidade BETWEEN 1 AND 3),
  CONSTRAINT ck_den_anonima CHECK (
      (anonima = 1 AND id_cidadao IS NULL) OR (anonima = 0 AND id_cidadao IS NOT NULL)),
  CONSTRAINT ck_den_loc CHECK (
      (lat_denunciante IS NULL AND lon_denunciante IS NULL) OR
      (lat_denunciante IS NOT NULL AND lon_denunciante IS NOT NULL)),
  CONSTRAINT fk_den_cidadao   FOREIGN KEY (id_cidadao)   REFERENCES cidadao(id_cidadao),
  CONSTRAINT fk_den_categoria FOREIGN KEY (id_categoria) REFERENCES categoria_ocorrencia(id_categoria),
  CONSTRAINT fk_den_esquadra  FOREIGN KEY (id_esquadra)  REFERENCES esquadra(id_esquadra),
  INDEX idx_den_estado (estado),
  INDEX idx_den_esquadra (id_esquadra)
) ENGINE=InnoDB;

CREATE TABLE anexo (
  id_anexo     INT AUTO_INCREMENT PRIMARY KEY,
  id_denuncia  INT NOT NULL,
  tipo         ENUM('FOTO','AUDIO','VIDEO') NOT NULL,
  caminho      VARCHAR(255) NOT NULL,
  CONSTRAINT fk_anexo_denuncia FOREIGN KEY (id_denuncia)
    REFERENCES denuncia(id_denuncia) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE accao_policial (
  id_accao      INT AUTO_INCREMENT PRIMARY KEY,
  id_denuncia   INT NOT NULL,
  matricula     VARCHAR(12) NOT NULL,
  descricao     VARCHAR(300) NOT NULL,
  novo_estado   ENUM('Pendente','Em Analise','Validada','Rejeitada','Resolvida') NULL,
  data_hora     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_accao_denuncia FOREIGN KEY (id_denuncia) REFERENCES denuncia(id_denuncia),
  CONSTRAINT fk_accao_agente   FOREIGN KEY (matricula)   REFERENCES agente_policial(matricula)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. FUNCAO: distancia (km) entre dois pontos - formula de Haversine
-- ---------------------------------------------------------------------
DELIMITER //
CREATE FUNCTION fn_distancia_km(lat1 DOUBLE, lon1 DOUBLE, lat2 DOUBLE, lon2 DOUBLE)
RETURNS DOUBLE DETERMINISTIC NO SQL
BEGIN
  RETURN 6371 * 2 * ASIN(LEAST(1, SQRT(
      POWER(SIN(RADIANS(lat2 - lat1) / 2), 2) +
      COS(RADIANS(lat1)) * COS(RADIANS(lat2)) *
      POWER(SIN(RADIANS(lon2 - lon1) / 2), 2))));
END//
DELIMITER ;

-- ---------------------------------------------------------------------
-- 3. TRIGGERS
-- ---------------------------------------------------------------------
DELIMITER //

-- 3.1 O perfil do utilizador tem de corresponder a tabela de especializacao
CREATE TRIGGER trg_cidadao_perfil BEFORE INSERT ON cidadao
FOR EACH ROW
BEGIN
  IF (SELECT perfil FROM utilizador WHERE id_utilizador = NEW.id_utilizador) <> 'CIDADAO' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'O utilizador nao tem perfil CIDADAO.';
  END IF;
END//

CREATE TRIGGER trg_agente_perfil BEFORE INSERT ON agente_policial
FOR EACH ROW
BEGIN
  IF (SELECT perfil FROM utilizador WHERE id_utilizador = NEW.id_utilizador) <> 'AGENTE' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'O utilizador nao tem perfil AGENTE.';
  END IF;
END//

CREATE TRIGGER trg_admin_perfil BEFORE INSERT ON administrador
FOR EACH ROW
BEGIN
  IF (SELECT perfil FROM utilizador WHERE id_utilizador = NEW.id_utilizador) <> 'ADMINISTRADOR' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'O utilizador nao tem perfil ADMINISTRADOR.';
  END IF;
END//

-- 3.2 Encaminhamento automatico: esquadra mais proxima da ocorrencia,
--     gravidade herdada da categoria e marcacao de emergencia
CREATE TRIGGER trg_denuncia_encaminhar BEFORE INSERT ON denuncia
FOR EACH ROW
BEGIN
  DECLARE v_nivel TINYINT;

  -- Assercao (CREATE ASSERTION nao existe no MariaDB/MySQL): um cidadao nao
  -- pode ter mais de 5 denuncias Pendentes ao mesmo tempo (anti-spam).
  IF NEW.id_cidadao IS NOT NULL AND
     (SELECT COUNT(*) FROM denuncia
       WHERE id_cidadao = NEW.id_cidadao AND estado = 'Pendente') >= 5 THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Limite atingido: ja tem 5 denuncias pendentes.';
  END IF;

  IF NEW.id_esquadra IS NULL THEN
    SET NEW.id_esquadra = (
      SELECT e.id_esquadra FROM esquadra e
      ORDER BY fn_distancia_km(NEW.lat_ocorrencia, NEW.lon_ocorrencia,
                               e.latitude, e.longitude) ASC
      LIMIT 1);
  END IF;

  SELECT nivel_gravidade INTO v_nivel
    FROM categoria_ocorrencia WHERE id_categoria = NEW.id_categoria;
  IF NEW.gravidade IS NULL THEN
    SET NEW.gravidade = v_nivel;
  END IF;

  SET NEW.emergencia = IF(NEW.gravidade = 3, 1, 0);
END//

-- 3.3 Apenas agentes da esquadra responsavel podem registar accoes
CREATE TRIGGER trg_accao_jurisdicao BEFORE INSERT ON accao_policial
FOR EACH ROW
BEGIN
  IF (SELECT id_esquadra FROM agente_policial WHERE matricula = NEW.matricula)
     <> (SELECT id_esquadra FROM denuncia WHERE id_denuncia = NEW.id_denuncia) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'O agente nao pertence a esquadra responsavel por esta denuncia.';
  END IF;
END//

-- 3.4 A accao actualiza o estado da denuncia; se Validada e nao anonima,
--     aumenta a pontuacao de confianca do cidadao
CREATE TRIGGER trg_accao_actualiza_estado AFTER INSERT ON accao_policial
FOR EACH ROW
BEGIN
  DECLARE v_cidadao INT;
  DECLARE v_estado_ant VARCHAR(20);

  IF NEW.novo_estado IS NOT NULL THEN
    SELECT id_cidadao, estado INTO v_cidadao, v_estado_ant
      FROM denuncia WHERE id_denuncia = NEW.id_denuncia;

    UPDATE denuncia SET estado = NEW.novo_estado WHERE id_denuncia = NEW.id_denuncia;

    IF NEW.novo_estado = 'Validada' AND v_estado_ant <> 'Validada'
       AND v_cidadao IS NOT NULL THEN
      UPDATE cidadao SET pontuacao_confianca = pontuacao_confianca + 1
       WHERE id_cidadao = v_cidadao;
    END IF;
  END IF;
END//

-- 3.5 Denuncia resolvida ou rejeitada nao pode voltar a ser alterada
CREATE TRIGGER trg_denuncia_estado_final BEFORE UPDATE ON denuncia
FOR EACH ROW
BEGIN
  IF OLD.estado IN ('Resolvida','Rejeitada') AND NEW.estado <> OLD.estado THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'Denuncia encerrada: o estado nao pode ser alterado.';
  END IF;
END//

DELIMITER ;

-- ---------------------------------------------------------------------
-- 3.6 RESTRICOES CHECK ADICIONAIS (formato dos contactos)
-- ---------------------------------------------------------------------
ALTER TABLE utilizador ADD CONSTRAINT ck_util_contacto
  CHECK (contacto REGEXP '^[+]?[0-9]{9,13}$');
ALTER TABLE esquadra ADD CONSTRAINT ck_esq_contacto
  CHECK (contacto REGEXP '^[+]?[0-9]{9,13}$');

-- ---------------------------------------------------------------------
-- 3.7 VISTA: painel dos agentes (emergencias em primeiro lugar)
--     A localizacao do denunciante so e mostrada em denuncias nao anonimas
-- ---------------------------------------------------------------------
CREATE VIEW v_painel_agente AS
SELECT d.id_denuncia, d.id_esquadra, e.nome AS esquadra,
       c.nome_categoria, d.gravidade, d.emergencia, d.estado, d.data_hora,
       d.descricao, d.lat_ocorrencia, d.lon_ocorrencia,
       IF(d.anonima = 1, NULL, d.lat_denunciante) AS lat_denunciante,
       IF(d.anonima = 1, NULL, d.lon_denunciante) AS lon_denunciante
FROM denuncia d
JOIN esquadra e ON e.id_esquadra = d.id_esquadra
JOIN categoria_ocorrencia c ON c.id_categoria = d.id_categoria
WHERE d.estado IN ('Pendente','Em Analise');
-- uso: SELECT * FROM v_painel_agente WHERE id_esquadra = 1
--      ORDER BY emergencia DESC, gravidade DESC, data_hora;

-- ---------------------------------------------------------------------
-- 3.8 PROCEDIMENTO: esquadra mais proxima de um ponto (para o cidadao)
-- ---------------------------------------------------------------------
DELIMITER //
CREATE PROCEDURE sp_esquadra_proxima(IN p_lat DOUBLE, IN p_lon DOUBLE)
BEGIN
  SELECT nome, contacto, endereco,
         ROUND(fn_distancia_km(p_lat, p_lon, latitude, longitude), 2) AS distancia_km
  FROM esquadra
  ORDER BY distancia_km ASC
  LIMIT 1;
END//
DELIMITER ;

-- ---------------------------------------------------------------------
-- 4. DADOS DE TESTE
-- ---------------------------------------------------------------------
INSERT INTO categoria_ocorrencia (nome_categoria, nivel_gravidade) VALUES
 ('Rapto',3),('Assalto',3),('Violacao',3),('Agressao',2),('Furto',1),('Viatura suspeita',1);

INSERT INTO esquadra (nome, contacto, endereco, area_jurisdicao, latitude, longitude) VALUES
 ('1a Esquadra', '+25821300001','Av. 24 de Julho, Maputo','Baixa e Central',-25.967500,32.573000),
 ('Esquadra da Matola','+25821720002','Av. Samora Machel, Matola','Matola Sede',-25.962200,32.458900),
 ('Esquadra de KaMpfumo','+25821300003','Av. Eduardo Mondlane, Maputo','Polana e Sommerschield',-25.948000,32.590000);

-- senha_hash = SHA-256 hexadecimal (a aplicacao Java usa o mesmo algoritmo)
INSERT INTO utilizador (nome, contacto, email, senha_hash, perfil) VALUES
 ('Maria Govene','+258840000001','maria@email.mz',SHA2('maria123',256),'CIDADAO'),
 ('Jaime Cossa','+258840000002','jaime@email.mz',SHA2('jaime123',256),'CIDADAO'),
 ('Agente Bapu','+258840000003',NULL,SHA2('bapu123',256),'AGENTE'),
 ('Agente Helton','+258840000004',NULL,SHA2('helton123',256),'AGENTE'),
 ('Admin PRM','+258840000005','admin@prm.gov.mz',SHA2('admin123',256),'ADMINISTRADOR'),
 ('Agente Sitoe','+258840000006',NULL,SHA2('sitoe123',256),'AGENTE');

INSERT INTO cidadao (id_utilizador) VALUES (1),(2);
INSERT INTO agente_policial (matricula, id_utilizador, cargo, id_esquadra) VALUES
 ('PRM-0001',3,'Subinspector',1),('PRM-0002',4,'Agente',2),('PRM-0003',6,'Agente',3);
INSERT INTO administrador (id_utilizador) VALUES (5);

-- esquadra e gravidade sao preenchidas pelo trigger
INSERT INTO denuncia (id_cidadao,id_categoria,descricao,lat_ocorrencia,lon_ocorrencia,lat_denunciante,lon_denunciante,anonima) VALUES
 (1,2,'Assalto na Baixa, junto ao mercado.',-25.9690,32.5730,-25.9620,32.4600,0),
 (NULL,5,'Furto de telemovel na paragem.',-25.9630,32.4580,NULL,NULL,1),
 (2,6,'Viatura suspeita parada ha horas.',-25.9480,32.5900,-25.9480,32.5900,0),
 (2,1,'Tentativa de rapto perto da Baixa.',-25.9680,32.5735,-25.9680,32.5735,0);

INSERT INTO anexo (id_denuncia,tipo,caminho) VALUES
 (1,'FOTO','/uploads/den1_foto1.jpg'),(1,'VIDEO','/uploads/den1_video1.mp4');

INSERT INTO accao_policial (id_denuncia,matricula,descricao,novo_estado) VALUES
 (1,'PRM-0001','Patrulha enviada ao local.','Em Analise'),
 (1,'PRM-0001','Ocorrencia confirmada no local.','Validada'),
 (2,'PRM-0002','Queixa registada.','Em Analise');
