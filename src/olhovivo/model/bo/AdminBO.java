package olhovivo.model.bo;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import olhovivo.model.dao.CategoriaDAO;
import olhovivo.model.dao.DenunciaDAO;
import olhovivo.model.dao.EsquadraDAO;
import olhovivo.model.dao.UtilizadorDAO;
import olhovivo.model.vo.Categoria;
import olhovivo.model.vo.Esquadra;
import olhovivo.util.Hash;
import olhovivo.util.NegocioException;
import olhovivo.util.Validacao;

/** Regras de negocio da administracao (esquadras, categorias, agentes, estatisticas). */
public class AdminBO {
    private final EsquadraDAO esqDao = new EsquadraDAO();
    private final CategoriaDAO catDao = new CategoriaDAO();
    private final UtilizadorDAO utDao = new UtilizadorDAO();
    private final DenunciaDAO denDao = new DenunciaDAO();

    // ----- esquadras -----
    public List<Esquadra> esquadras() throws NegocioException {
        try { return esqDao.listar(); } catch (SQLException e) { throw NegocioException.de(e); }
    }

    private void validar(Esquadra e) throws NegocioException {
        if (Validacao.vazio(e.getNome()) || Validacao.vazio(e.getEndereco()) || Validacao.vazio(e.getArea()))
            throw new NegocioException("Nome, endereco e area de jurisdicao sao obrigatorios.");
        e.setContacto(Validacao.normalizarContacto(e.getContacto()));
        if (!Validacao.contactoValido(e.getContacto())) throw new NegocioException("Contacto da esquadra invalido.");
        if (!Validacao.latitudeValida(e.getLatitude()) || !Validacao.longitudeValida(e.getLongitude()))
            throw new NegocioException("Latitude (-90 a 90) ou longitude (-180 a 180) invalida.");
    }

    public void gravar(Esquadra e) throws NegocioException {
        validar(e);
        try { if (e.getId() == 0) esqDao.inserir(e); else esqDao.actualizar(e); }
        catch (SQLException ex) { throw NegocioException.de(ex); }
    }

    public void apagarEsquadra(int id) throws NegocioException {
        try { esqDao.apagar(id); } catch (SQLException e) { throw NegocioException.de(e); }
    }

    // ----- categorias -----
    public List<Categoria> categorias() throws NegocioException {
        try { return catDao.listar(); } catch (SQLException e) { throw NegocioException.de(e); }
    }

    public void gravar(Categoria c) throws NegocioException {
        if (Validacao.vazio(c.getNome())) throw new NegocioException("O nome da categoria e obrigatorio.");
        if (c.getNivel() < 1 || c.getNivel() > 3) throw new NegocioException("O nivel de gravidade deve ser 1, 2 ou 3.");
        try { if (c.getId() == 0) catDao.inserir(c); else catDao.actualizar(c); }
        catch (SQLException e) { throw NegocioException.de(e); }
    }

    public void apagarCategoria(int id) throws NegocioException {
        try { catDao.apagar(id); } catch (SQLException e) { throw NegocioException.de(e); }
    }

    // ----- utilizadores -----
    public List<Object[]> utilizadores() throws NegocioException {
        try { return utDao.listar(); } catch (SQLException e) { throw NegocioException.de(e); }
    }

    public void definirActivo(int idUtilizador, boolean activo) throws NegocioException {
        try { utDao.definirActivo(idUtilizador, activo); } catch (SQLException e) { throw NegocioException.de(e); }
    }

    public void registarAgente(String nome, String contacto, String email, String senha, String matricula,
                               String cargo, Esquadra esq) throws NegocioException {
        String c = Validacao.normalizarContacto(contacto);
        if (Validacao.vazio(nome) || Validacao.vazio(matricula) || Validacao.vazio(cargo))
            throw new NegocioException("Nome, matricula e cargo sao obrigatorios.");
        if (matricula.trim().length() > 12) throw new NegocioException("A matricula nao pode exceder 12 caracteres.");
        if (!Validacao.contactoValido(c)) throw new NegocioException("Contacto invalido.");
        if (senha == null || senha.length() < 6) throw new NegocioException("A palavra-passe deve ter pelo menos 6 caracteres.");
        if (esq == null) throw new NegocioException("Escolha a esquadra do agente.");
        try { utDao.registarAgente(nome.trim(), c, email == null ? null : email.trim(), Hash.sha256(senha),
                                   matricula.trim(), cargo.trim(), esq.getId()); }
        catch (SQLException e) { throw NegocioException.de(e); }
    }

    // ----- estatisticas -----
    public Map<String, Integer> estatisticas(boolean porCategoria) throws NegocioException {
        try { return denDao.contarPor(porCategoria); } catch (SQLException e) { throw NegocioException.de(e); }
    }
}
