-- =====================================================================
-- OLHO VIVO - Scripts complementares (executar DEPOIS de olho_vivo.sql)
-- Materia de Base de Dados I: subconsultas, divisao, conjuntos, indices,
-- transaccoes e controlo de acessos (DCL).
-- Executar: mysql -u root < olho_vivo_extras.sql   (ou phpMyAdmin > Importar)
-- =====================================================================
USE olho_vivo;

-- ---------------------------------------------------------------------
-- 1. SUBCONSULTAS
-- ---------------------------------------------------------------------
-- 1.1 (IN, subconsultas aninhadas) Cidadaos que denunciaram ocorrencias graves
SELECT u.nome
FROM utilizador u
WHERE u.id_utilizador IN (
        SELECT c.id_utilizador FROM cidadao c
        WHERE c.id_cidadao IN (
              SELECT d.id_cidadao FROM denuncia d
              JOIN categoria_ocorrencia k ON k.id_categoria = d.id_categoria
              WHERE k.nivel_gravidade = 3));

-- 1.2 (subconsulta escalar) Esquadras com mais denuncias do que a media
SELECT e.nome, COUNT(*) AS total
FROM denuncia d JOIN esquadra e ON e.id_esquadra = d.id_esquadra
GROUP BY e.id_esquadra, e.nome
HAVING COUNT(*) > (SELECT AVG(t.n) FROM
                    (SELECT COUNT(*) AS n FROM denuncia GROUP BY id_esquadra) t);

-- 1.3 (NOT EXISTS) Denuncias sem qualquer accao policial
SELECT d.id_denuncia, d.descricao
FROM denuncia d
WHERE NOT EXISTS (SELECT 1 FROM accao_policial a WHERE a.id_denuncia = d.id_denuncia);

-- 1.4 (ALL) Categorias cuja gravidade e superior a de todas as categorias de nivel 1
SELECT nome_categoria FROM categoria_ocorrencia
WHERE nivel_gravidade > ALL (SELECT nivel_gravidade FROM categoria_ocorrencia
                             WHERE nivel_gravidade = 1);

-- ---------------------------------------------------------------------
-- 2. DIVISAO RELACIONAL (duplo NOT EXISTS)
-- ---------------------------------------------------------------------
-- 2.1 Divisao: cidadaos que denunciaram TODAS as categorias de gravidade 3
--     R = pi id_cidadao,id_categoria (Denuncia)   S = pi id_categoria (sigma nivel=3 (Categoria))   R / S
SELECT c.id_cidadao
FROM cidadao c
WHERE NOT EXISTS (
        SELECT 1 FROM categoria_ocorrencia k
        WHERE k.nivel_gravidade = 3
          AND NOT EXISTS (SELECT 1 FROM denuncia d
                          WHERE d.id_cidadao = c.id_cidadao
                            AND d.id_categoria = k.id_categoria));

-- 2.2 Esquadras em que TODAS as denuncias ja tem pelo menos uma accao
--     (esquadras sem denuncias tambem aparecem: condicao vacuamente verdadeira)
SELECT e.nome
FROM esquadra e
WHERE NOT EXISTS (
        SELECT 1 FROM denuncia d
        WHERE d.id_esquadra = e.id_esquadra
          AND NOT EXISTS (SELECT 1 FROM accao_policial a
                          WHERE a.id_denuncia = d.id_denuncia));

-- ---------------------------------------------------------------------
-- 3. OPERACOES SOBRE CONJUNTOS
-- ---------------------------------------------------------------------
-- 3.1 Uniao: todos os contactos telefonicos registados no sistema
SELECT nome, contacto, 'Utilizador' AS origem FROM utilizador
UNION
SELECT nome, contacto, 'Esquadra' FROM esquadra;

-- 3.2 Diferenca (emulada com NOT IN): cidadaos que nunca denunciaram
SELECT u.nome FROM cidadao c JOIN utilizador u ON u.id_utilizador = c.id_utilizador
WHERE c.id_cidadao NOT IN (SELECT id_cidadao FROM denuncia WHERE id_cidadao IS NOT NULL);

-- ---------------------------------------------------------------------
-- 4. INDICES
--    O painel dos agentes filtra por esquadra + estado e ordena por emergencia/data.
-- ---------------------------------------------------------------------
CREATE INDEX idx_den_painel ON denuncia (id_esquadra, estado, emergencia, data_hora);
EXPLAIN SELECT * FROM denuncia
WHERE id_esquadra = 1 AND estado IN ('Pendente','Em Analise')
ORDER BY emergencia DESC, data_hora;

-- ---------------------------------------------------------------------
-- 5. TRANSACCOES (ACID): registar um agente = utilizador + agente_policial
--    ou tudo ou nada.
-- ---------------------------------------------------------------------
START TRANSACTION;
INSERT INTO utilizador (nome, contacto, senha_hash, perfil)
VALUES ('Agente Teste', '+258840000099', SHA2('teste123',256), 'AGENTE');
SAVEPOINT depois_utilizador;
INSERT INTO agente_policial (matricula, id_utilizador, cargo, id_esquadra)
VALUES ('PRM-0099', LAST_INSERT_ID(), 'Agente', 1);
ROLLBACK;            -- desfaz tudo (trocar por COMMIT para gravar)
SELECT COUNT(*) AS ficou_gravado FROM utilizador WHERE contacto = '+258840000099';  -- 0

-- ---------------------------------------------------------------------
-- 6. CONTROLO DE ACESSOS (DCL)
-- ---------------------------------------------------------------------
CREATE USER IF NOT EXISTS 'ov_app'@'localhost' IDENTIFIED BY 'ov_app123';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON olho_vivo.* TO 'ov_app'@'localhost';

CREATE USER IF NOT EXISTS 'ov_leitor'@'localhost' IDENTIFIED BY 'ov_leitor123';
GRANT SELECT ON olho_vivo.v_painel_agente TO 'ov_leitor'@'localhost';

REVOKE DELETE ON olho_vivo.* FROM 'ov_app'@'localhost';
FLUSH PRIVILEGES;
-- Para a aplicacao usar este utilizador: db.properties -> user=ov_app / password=ov_app123
-- (o DELETE e necessario ao administrador para apagar esquadras/categorias:
--  se usar ov_app, volte a conceder: GRANT DELETE ON olho_vivo.* TO 'ov_app'@'localhost';)
