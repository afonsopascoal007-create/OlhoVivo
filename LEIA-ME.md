# Olho Vivo — como colocar a funcionar

Aplicação desktop em Java (Swing + JDBC, arquitectura MVC em camadas) sobre a base de dados MariaDB/MySQL `olho_vivo`.

## 1. O que precisa
| Item | Versão | Nota |
|---|---|---|
| JDK (com `javac`) | 11 ou superior | `java -version` e `javac -version` têm de responder. Se `javac` não existir, instale o JDK (ex.: Temurin 17/21). |
| XAMPP | com MySQL/MariaDB | abrir o **XAMPP Control Panel** e carregar **Start** em *MySQL* |
| Driver JDBC | mariadb-java-client **ou** mysql-connector-j | ficheiro `.jar` para a pasta `lib/` |
| iText 5 | itextpdf-5.5.13.x | ficheiro `.jar` para a pasta `lib/` (só para o relatório PDF) |

Os links dos jars estão em `lib/COLOQUE_AQUI_OS_JARS.txt`.

## 2. Criar a base de dados (uma vez)
Escolha **uma** forma:
- **XAMPP Shell** (botão *Shell* no painel): `mysql -u root < C:\caminho\OlhoVivo\sql\olho_vivo.sql`
- **phpMyAdmin** (`http://localhost/phpmyadmin`) → separador **Importar** → escolher `sql/olho_vivo.sql` → **Executar**.

Não cole o script linha a linha: os blocos `DELIMITER //` têm de correr inteiros.
Para confirmar: `USE olho_vivo; SHOW TABLES;` deve listar 9 tabelas e a vista `v_painel_agente`.
(Opcional) `sql/olho_vivo_extras.sql` — subconsultas, índice, transacção e utilizadores (DCL) da matéria de BD.

## 3. Configurar a ligação
Abra `db.properties`. Em XAMPP o normal é já estar certo (`user=root`, `password=` vazio).
Se definiu palavra-passe no root, escreva-a em `password=`.

## 4. Compilar e executar
**Windows:** duplo clique em `compilar.bat` e depois em `executar.bat`.
**Linux/macOS:** `./compilar.sh` e `./executar.sh`.

### Alternativa: NetBeans
1. *File → New Project → Java with Ant → Java Application* (sem classe principal), nome `OlhoVivo`.
2. Copie a pasta `src/olhovivo` para a pasta `src` do projecto e `db.properties` para a raiz do projecto.
3. *Libraries → Add JAR/Folder* → os 2 jars.
4. *Run → Set Project Configuration → Customize → Main Class* = `olhovivo.Main` → **F6**.

## 5. Contas de teste
| Perfil | Contacto | Palavra-passe | Notas |
|---|---|---|---|
| Cidadão | +258840000001 | maria123 | Maria Govene |
| Cidadão | +258840000002 | jaime123 | Jaime Cossa |
| Agente | +258840000003 | bapu123 | Esquadra 1 (PRM-0001) |
| Agente | +258840000004 | helton123 | Esquadra da Matola (PRM-0002) |
| Agente | +258840000006 | sitoe123 | Esquadra de KaMpfumo (PRM-0003) |
| Administrador | +258840000005 | admin123 | |

Pode escrever só `840000001` — a aplicação acrescenta `+258`.

## 6. Roteiro de teste (2 minutos)
1. Entrar como **Maria** → *Nova denúncia* → categoria *Rapto*, descrição com 10+ letras, coordenadas por omissão (Baixa) → **Enviar**. A mensagem indica a esquadra atribuída (1.ª Esquadra) e *EMERGÊNCIA*.
2. **Terminar sessão** → entrar como **Bapu** (agente). A denúncia surge a vermelho no topo. Seleccione-a → **Registar acção** → novo estado *Validada*.
3. Entrar como **Helton** (outra esquadra) e verificar que **não** vê essa denúncia (privacidade por jurisdição).
4. Entrar de novo como Maria → *As minhas denúncias*: estado actualizado e pontuação de confiança aumentada.
5. Entrar como **admin** → criar esquadra/categoria/agente; ver *Estatísticas*.
6. No painel do agente: **Relatório PDF** gera o ficheiro com todas as denúncias da esquadra.

## 7. Problemas frequentes
| Mensagem / sintoma | Causa e solução |
|---|---|
| `Sem ligação à base de dados` | MySQL do XAMPP não está iniciado, ou porta ≠ 3306 (ajuste `port`). |
| `A base de dados olho_vivo não existe` (erro 1049) | Falta executar `sql/olho_vivo.sql` (passo 2). |
| Erro 1045 `Access denied` | `user`/`password` de `db.properties` errados. |
| `No suitable driver` / aviso “nenhum driver” | Falta o jar do driver na pasta `lib/`. |
| `package com.itextpdf.text does not exist` ao compilar | Falta o jar do iText em `lib/`. |
| `javac` não é reconhecido | Instalou só o JRE; instale o JDK e reabra o terminal. |
| Script SQL falha com erro de sintaxe perto de `DELIMITER` | Foi executado numa ferramenta que não suporta `DELIMITER`. Use Shell, phpMyAdmin (Importar) ou Workbench. |
| `CHECK constraint ... violated` | Dado inválido (contacto fora do formato `+258…`, gravidade fora de 1–3…). |
| `Limite atingido: já tem 5 denúncias pendentes` | Regra anti-spam (asserção) do trigger; um agente tem de tratar as pendentes. |

## 8. Estrutura
```
OlhoVivo/
 db.properties  compilar.bat/.sh  executar.bat/.sh
 sql/   olho_vivo.sql  olho_vivo_extras.sql
 lib/   (jars)
 src/olhovivo/
   Main.java
   model/vo/   Utilizador, Esquadra, Categoria, Denuncia, Accao      (Value Objects)
   model/dao/  Conexao, UtilizadorDAO, EsquadraDAO, CategoriaDAO,
               DenunciaDAO, AccaoDAO                                  (acesso a dados, JDBC)
   model/bo/   AutenticacaoBO, DenunciaBO, AdminBO                    (regras de negócio)
   view/       LoginView, RegistoDialog, CidadaoView, AgenteView,
               AdminView, NovoAgenteDialog, UI                        (Swing)
   controller/ Login-, Cidadao-, Agente-, AdminController             (eventos)
   util/       Hash, Validacao, NegocioException, PainelRefresher
               (Thread), RelatorioPDF (iText)
```
