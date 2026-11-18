package olhovivo.model.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import olhovivo.model.vo.Utilizador;

/** DAO de utilizadores e dos seus perfis (cidadao, agente_policial, administrador). */
public class UtilizadorDAO {

    private static final String SQL_LOGIN =
        "SELECT u.id_utilizador, u.nome, u.contacto, u.email, u.perfil, u.activo, "
      + "       c.id_cidadao, c.pontuacao_confianca, a.matricula, a.cargo, a.id_esquadra "
      + "FROM utilizador u "
      + "LEFT JOIN cidadao c ON c.id_utilizador = u.id_utilizador "
      + "LEFT JOIN agente_policial a ON a.id_utilizador = u.id_utilizador "
      + "WHERE u.contacto = ? AND u.senha_hash = ? AND u.activo = 1";

    /** Devolve o utilizador ou null se as credenciais forem invalidas. */
    public Utilizador autenticar(String contacto, String hash) throws SQLException {
        try (Connection con = Conexao.obter();
             PreparedStatement ps = con.prepareStatement(SQL_LOGIN)) {
            ps.setString(1, contacto);
            ps.setString(2, hash);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Utilizador u = new Utilizador();
                u.setId(rs.getInt("id_utilizador"));
                u.setNome(rs.getString("nome"));
                u.setContacto(rs.getString("contacto"));
                u.setEmail(rs.getString("email"));
                u.setPerfil(rs.getString("perfil"));
                u.setActivo(rs.getBoolean("activo"));
                int idc = rs.getInt("id_cidadao");
                if (!rs.wasNull()) { u.setIdCidadao(idc); u.setPontuacao(rs.getInt("pontuacao_confianca")); }
                u.setMatricula(rs.getString("matricula"));
                u.setCargo(rs.getString("cargo"));
                int ide = rs.getInt("id_esquadra");
                if (!rs.wasNull()) u.setIdEsquadra(ide);
                return u;
            }
        }
    }

    public int pontuacao(int idCidadao) throws SQLException {
        try (Connection con = Conexao.obter();
             PreparedStatement ps = con.prepareStatement("SELECT pontuacao_confianca FROM cidadao WHERE id_cidadao = ?")) {
            ps.setInt(1, idCidadao);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    private int inserirUtilizador(Connection con, String nome, String contacto, String email,
                                  String hash, String perfil) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO utilizador (nome, contacto, email, senha_hash, perfil) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nome);
            ps.setString(2, contacto);
            if (email == null || email.isEmpty()) ps.setNull(3, Types.VARCHAR); else ps.setString(3, email);
            ps.setString(4, hash);
            ps.setString(5, perfil);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { k.next(); return k.getInt(1); }
        }
    }

    /** Transaccao: utilizador + cidadao (ou tudo ou nada). */
    public void registarCidadao(String nome, String contacto, String email, String hash) throws SQLException {
        try (Connection con = Conexao.obter()) {
            con.setAutoCommit(false);
            try {
                int id = inserirUtilizador(con, nome, contacto, email, hash, "CIDADAO");
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO cidadao (id_utilizador) VALUES (?)")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) { con.rollback(); throw e; }
        }
    }

    /** Transaccao: utilizador + agente_policial. */
    public void registarAgente(String nome, String contacto, String email, String hash,
                               String matricula, String cargo, int idEsquadra) throws SQLException {
        try (Connection con = Conexao.obter()) {
            con.setAutoCommit(false);
            try {
                int id = inserirUtilizador(con, nome, contacto, email, hash, "AGENTE");
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO agente_policial (matricula, id_utilizador, cargo, id_esquadra) VALUES (?,?,?,?)")) {
                    ps.setString(1, matricula);
                    ps.setInt(2, id);
                    ps.setString(3, cargo);
                    ps.setInt(4, idEsquadra);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) { con.rollback(); throw e; }
        }
    }

    /** Lista para o administrador: linhas {id, nome, contacto, perfil, activo, matricula, esquadra}. */
    public List<Object[]> listar() throws SQLException {
        String sql = "SELECT u.id_utilizador, u.nome, u.contacto, u.perfil, u.activo, a.matricula, e.nome AS esquadra "
                   + "FROM utilizador u LEFT JOIN agente_policial a ON a.id_utilizador = u.id_utilizador "
                   + "LEFT JOIN esquadra e ON e.id_esquadra = a.id_esquadra ORDER BY u.id_utilizador";
        List<Object[]> r = new ArrayList<>();
        try (Connection con = Conexao.obter(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                r.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                                   rs.getBoolean(5) ? "Sim" : "Nao", rs.getString(6), rs.getString(7)});
        }
        return r;
    }

    public void definirActivo(int idUtilizador, boolean activo) throws SQLException {
        try (Connection con = Conexao.obter();
             PreparedStatement ps = con.prepareStatement("UPDATE utilizador SET activo = ? WHERE id_utilizador = ?")) {
            ps.setBoolean(1, activo);
            ps.setInt(2, idUtilizador);
            ps.executeUpdate();
        }
    }
}
