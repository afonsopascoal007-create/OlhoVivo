package olhovivo.model.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import olhovivo.model.vo.Categoria;

public class CategoriaDAO {
    public List<Categoria> listar() throws SQLException {
        List<Categoria> r = new ArrayList<>();
        try (Connection con = Conexao.obter(); Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_categoria, nome_categoria, nivel_gravidade FROM categoria_ocorrencia ORDER BY id_categoria")) {
            while (rs.next()) r.add(new Categoria(rs.getInt(1), rs.getString(2), rs.getInt(3)));
        }
        return r;
    }

    public void inserir(Categoria c) throws SQLException {
        try (Connection con = Conexao.obter();
             PreparedStatement ps = con.prepareStatement("INSERT INTO categoria_ocorrencia (nome_categoria, nivel_gravidade) VALUES (?,?)")) {
            ps.setString(1, c.getNome()); ps.setInt(2, c.getNivel()); ps.executeUpdate();
        }
    }

    public void actualizar(Categoria c) throws SQLException {
        try (Connection con = Conexao.obter();
             PreparedStatement ps = con.prepareStatement("UPDATE categoria_ocorrencia SET nome_categoria=?, nivel_gravidade=? WHERE id_categoria=?")) {
            ps.setString(1, c.getNome()); ps.setInt(2, c.getNivel()); ps.setInt(3, c.getId()); ps.executeUpdate();
        }
    }

    public void apagar(int id) throws SQLException {
        try (Connection con = Conexao.obter();
             PreparedStatement ps = con.prepareStatement("DELETE FROM categoria_ocorrencia WHERE id_categoria = ?")) {
            ps.setInt(1, id); ps.executeUpdate();
        }
    }
}
