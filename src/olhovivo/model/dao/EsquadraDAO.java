package olhovivo.model.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import olhovivo.model.vo.Esquadra;

public class EsquadraDAO {

    private Esquadra ler(ResultSet rs) throws SQLException {
        return new Esquadra(rs.getInt("id_esquadra"), rs.getString("nome"), rs.getString("contacto"),
                rs.getString("endereco"), rs.getString("area_jurisdicao"),
                rs.getDouble("latitude"), rs.getDouble("longitude"));
    }

    public List<Esquadra> listar() throws SQLException {
        List<Esquadra> r = new ArrayList<>();
        try (Connection con = Conexao.obter(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM esquadra ORDER BY id_esquadra")) {
            while (rs.next()) r.add(ler(rs));
        }
        return r;
    }

    public void inserir(Esquadra e) throws SQLException {
        String sql = "INSERT INTO esquadra (nome, contacto, endereco, area_jurisdicao, latitude, longitude) VALUES (?,?,?,?,?,?)";
        try (Connection con = Conexao.obter(); PreparedStatement ps = con.prepareStatement(sql)) {
            preencher(ps, e); ps.executeUpdate();
        }
    }

    public void actualizar(Esquadra e) throws SQLException {
        String sql = "UPDATE esquadra SET nome=?, contacto=?, endereco=?, area_jurisdicao=?, latitude=?, longitude=? WHERE id_esquadra=?";
        try (Connection con = Conexao.obter(); PreparedStatement ps = con.prepareStatement(sql)) {
            preencher(ps, e); ps.setInt(7, e.getId()); ps.executeUpdate();
        }
    }

    private void preencher(PreparedStatement ps, Esquadra e) throws SQLException {
        ps.setString(1, e.getNome()); ps.setString(2, e.getContacto()); ps.setString(3, e.getEndereco());
        ps.setString(4, e.getArea()); ps.setDouble(5, e.getLatitude()); ps.setDouble(6, e.getLongitude());
    }

    public void apagar(int id) throws SQLException {
        try (Connection con = Conexao.obter();
             PreparedStatement ps = con.prepareStatement("DELETE FROM esquadra WHERE id_esquadra = ?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }

    /** Usa o procedimento armazenado sp_esquadra_proxima (CallableStatement). */
    public Esquadra maisProxima(double lat, double lon) throws SQLException {
        try (Connection con = Conexao.obter(); CallableStatement cs = con.prepareCall("{call sp_esquadra_proxima(?,?)}")) {
            cs.setDouble(1, lat); cs.setDouble(2, lon);
            try (ResultSet rs = cs.executeQuery()) {
                if (!rs.next()) return null;
                Esquadra e = new Esquadra();
                e.setNome(rs.getString("nome")); e.setContacto(rs.getString("contacto"));
                e.setEndereco(rs.getString("endereco")); e.setDistanciaKm(rs.getDouble("distancia_km"));
                return e;
            }
        }
    }
}
