package olhovivo.model.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import olhovivo.model.vo.Denuncia;

/** DAO de denuncias e anexos. A esquadra, a gravidade e a emergencia sao preenchidas pelo trigger. */
public class DenunciaDAO {

    private static final String BASE =
        "SELECT d.id_denuncia, d.id_cidadao, d.id_categoria, d.id_esquadra, c.nome_categoria, e.nome AS esquadra, "
      + "       d.data_hora, d.descricao, d.lat_ocorrencia, d.lon_ocorrencia, "
      + "       IF(d.anonima = 1, NULL, d.lat_denunciante) AS lat_den, "
      + "       IF(d.anonima = 1, NULL, d.lon_denunciante) AS lon_den, "
      + "       d.gravidade, d.emergencia, d.anonima, d.estado "
      + "FROM denuncia d "
      + "JOIN categoria_ocorrencia c ON c.id_categoria = d.id_categoria "
      + "LEFT JOIN esquadra e ON e.id_esquadra = d.id_esquadra ";

    private static Double dbl(ResultSet rs, String col) throws SQLException {
        double v = rs.getDouble(col);
        return rs.wasNull() ? null : v;
    }

    private Denuncia ler(ResultSet rs) throws SQLException {
        Denuncia d = new Denuncia();
        d.setId(rs.getInt("id_denuncia"));
        int idc = rs.getInt("id_cidadao");
        d.setIdCidadao(rs.wasNull() ? null : idc);
        d.setIdCategoria(rs.getInt("id_categoria"));
        int ide = rs.getInt("id_esquadra");
        d.setIdEsquadra(rs.wasNull() ? null : ide);
        d.setCategoria(rs.getString("nome_categoria"));
        d.setEsquadra(rs.getString("esquadra"));
        d.setDataHora(rs.getTimestamp("data_hora"));
        d.setDescricao(rs.getString("descricao"));
        d.setLatOcorrencia(rs.getDouble("lat_ocorrencia"));
        d.setLonOcorrencia(rs.getDouble("lon_ocorrencia"));
        d.setLatDenunciante(dbl(rs, "lat_den"));
        d.setLonDenunciante(dbl(rs, "lon_den"));
        d.setGravidade(rs.getInt("gravidade"));
        d.setEmergencia(rs.getBoolean("emergencia"));
        d.setAnonima(rs.getBoolean("anonima"));
        d.setEstado(rs.getString("estado"));
        return d;
    }

    /** Insere e devolve a denuncia ja completada pelos triggers. */
    public Denuncia inserir(Denuncia d) throws SQLException {
        String sql = "INSERT INTO denuncia (id_cidadao, id_categoria, descricao, lat_ocorrencia, lon_ocorrencia, "
                   + "lat_denunciante, lon_denunciante, anonima) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = Conexao.obter();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (d.getIdCidadao() == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, d.getIdCidadao());
            ps.setInt(2, d.getIdCategoria());
            ps.setString(3, d.getDescricao());
            ps.setDouble(4, d.getLatOcorrencia());
            ps.setDouble(5, d.getLonOcorrencia());
            if (d.getLatDenunciante() == null) { ps.setNull(6, Types.DECIMAL); ps.setNull(7, Types.DECIMAL); }
            else { ps.setDouble(6, d.getLatDenunciante()); ps.setDouble(7, d.getLonDenunciante()); }
            ps.setBoolean(8, d.isAnonima());
            ps.executeUpdate();
            int id;
            try (ResultSet k = ps.getGeneratedKeys()) { k.next(); id = k.getInt(1); }
            return buscar(con, id);
        }
    }

    private Denuncia buscar(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(BASE + "WHERE d.id_denuncia = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? ler(rs) : null; }
        }
    }

    public List<Denuncia> listarPorCidadao(int idCidadao) throws SQLException {
        List<Denuncia> r = new ArrayList<>();
        try (Connection con = Conexao.obter();
             PreparedStatement ps = con.prepareStatement(BASE + "WHERE d.id_cidadao = ? ORDER BY d.data_hora DESC")) {
            ps.setInt(1, idCidadao);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) r.add(ler(rs)); }
        }
        return r;
    }

    /**
     * Denuncias de uma esquadra. Se apenasAbertas, usa a VISTA v_painel_agente
     * (Pendente / Em Analise, emergencias primeiro); caso contrario, o historico completo.
     */
    public List<Denuncia> listarPorEsquadra(int idEsquadra, boolean apenasAbertas) throws SQLException {
        List<Denuncia> r = new ArrayList<>();
        if (apenasAbertas) {
            String sql = "SELECT v.id_denuncia, v.id_esquadra, v.esquadra, v.nome_categoria, v.gravidade, v.emergencia, "
                       + "v.estado, v.data_hora, v.descricao, v.lat_ocorrencia, v.lon_ocorrencia, "
                       + "v.lat_denunciante, v.lon_denunciante, d.id_cidadao, d.id_categoria, d.anonima "
                       + "FROM v_painel_agente v JOIN denuncia d ON d.id_denuncia = v.id_denuncia "
                       + "WHERE v.id_esquadra = ? ORDER BY v.emergencia DESC, v.gravidade DESC, v.data_hora";
            try (Connection con = Conexao.obter(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idEsquadra);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Denuncia d = new Denuncia();
                        d.setId(rs.getInt("id_denuncia"));
                        d.setIdEsquadra(rs.getInt("id_esquadra"));
                        d.setEsquadra(rs.getString("esquadra"));
                        d.setCategoria(rs.getString("nome_categoria"));
                        d.setGravidade(rs.getInt("gravidade"));
                        d.setEmergencia(rs.getBoolean("emergencia"));
                        d.setEstado(rs.getString("estado"));
                        d.setDataHora(rs.getTimestamp("data_hora"));
                        d.setDescricao(rs.getString("descricao"));
                        d.setLatOcorrencia(rs.getDouble("lat_ocorrencia"));
                        d.setLonOcorrencia(rs.getDouble("lon_ocorrencia"));
                        d.setLatDenunciante(dbl(rs, "lat_denunciante"));
                        d.setLonDenunciante(dbl(rs, "lon_denunciante"));
                        d.setAnonima(rs.getBoolean("anonima"));
                        d.setIdCategoria(rs.getInt("id_categoria"));
                        r.add(d);
                    }
                }
            }
        } else {
            try (Connection con = Conexao.obter();
                 PreparedStatement ps = con.prepareStatement(BASE + "WHERE d.id_esquadra = ? "
                         + "ORDER BY d.emergencia DESC, d.gravidade DESC, d.data_hora")) {
                ps.setInt(1, idEsquadra);
                try (ResultSet rs = ps.executeQuery()) { while (rs.next()) r.add(ler(rs)); }
            }
        }
        return r;
    }

    /** Anexos de uma denuncia: linhas {tipo, caminho}. */
    public List<String[]> anexos(int idDenuncia) throws SQLException {
        List<String[]> r = new ArrayList<>();
        try (Connection con = Conexao.obter();
             PreparedStatement ps = con.prepareStatement("SELECT tipo, caminho FROM anexo WHERE id_denuncia = ?")) {
            ps.setInt(1, idDenuncia);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) r.add(new String[]{rs.getString(1), rs.getString(2)}); }
        }
        return r;
    }

    public void inserirAnexo(int idDenuncia, String tipo, String caminho) throws SQLException {
        try (Connection con = Conexao.obter();
             PreparedStatement ps = con.prepareStatement("INSERT INTO anexo (id_denuncia, tipo, caminho) VALUES (?,?,?)")) {
            ps.setInt(1, idDenuncia); ps.setString(2, tipo); ps.setString(3, caminho); ps.executeUpdate();
        }
    }

    /** Estatisticas para o administrador (consultas 4 e 5 do relatorio). */
    public Map<String, Integer> contarPor(boolean porCategoria) throws SQLException {
        String sql = porCategoria
            ? "SELECT c.nome_categoria, COUNT(d.id_denuncia) FROM categoria_ocorrencia c "
              + "LEFT JOIN denuncia d ON d.id_categoria = c.id_categoria GROUP BY c.nome_categoria"
            : "SELECT estado, COUNT(*) FROM denuncia GROUP BY estado";
        Map<String, Integer> m = new LinkedHashMap<>();
        try (Connection con = Conexao.obter(); Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) m.put(rs.getString(1), rs.getInt(2));
        }
        return m;
    }
}
