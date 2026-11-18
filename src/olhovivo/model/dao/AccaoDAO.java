package olhovivo.model.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import olhovivo.model.vo.Accao;

/** DAO das accoes policiais. Os triggers validam a jurisdicao e actualizam o estado da denuncia. */
public class AccaoDAO {

    public void inserir(Accao a) throws SQLException {
        String sql = "INSERT INTO accao_policial (id_denuncia, matricula, descricao, novo_estado) VALUES (?,?,?,?)";
        try (Connection con = Conexao.obter(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, a.getIdDenuncia());
            ps.setString(2, a.getMatricula());
            ps.setString(3, a.getDescricao());
            if (a.getNovoEstado() == null) ps.setNull(4, Types.VARCHAR); else ps.setString(4, a.getNovoEstado());
            ps.executeUpdate();
        }
    }

    public List<Accao> listarPorDenuncia(int idDenuncia) throws SQLException {
        String sql = "SELECT a.id_accao, a.matricula, u.nome, a.descricao, a.novo_estado, a.data_hora "
                   + "FROM accao_policial a JOIN agente_policial ag ON ag.matricula = a.matricula "
                   + "JOIN utilizador u ON u.id_utilizador = ag.id_utilizador "
                   + "WHERE a.id_denuncia = ? ORDER BY a.data_hora, a.id_accao";
        List<Accao> r = new ArrayList<>();
        try (Connection con = Conexao.obter(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idDenuncia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Accao a = new Accao();
                    a.setId(rs.getInt(1)); a.setIdDenuncia(idDenuncia); a.setMatricula(rs.getString(2));
                    a.setAgente(rs.getString(3)); a.setDescricao(rs.getString(4));
                    a.setNovoEstado(rs.getString(5)); a.setDataHora(rs.getTimestamp(6));
                    r.add(a);
                }
            }
        }
        return r;
    }
}
