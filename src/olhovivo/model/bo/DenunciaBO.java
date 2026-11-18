package olhovivo.model.bo;

import java.sql.SQLException;
import java.util.List;
import olhovivo.model.dao.AccaoDAO;
import olhovivo.model.dao.CategoriaDAO;
import olhovivo.model.dao.DenunciaDAO;
import olhovivo.model.dao.EsquadraDAO;
import olhovivo.model.vo.Accao;
import olhovivo.model.vo.Categoria;
import olhovivo.model.vo.Denuncia;
import olhovivo.model.vo.Esquadra;
import olhovivo.model.vo.Utilizador;
import olhovivo.util.NegocioException;
import olhovivo.util.Validacao;

/** Regras de negocio das denuncias e das accoes policiais. */
public class DenunciaBO {
    public static final String[] ESTADOS = {"Pendente", "Em Analise", "Validada", "Rejeitada", "Resolvida"};
    private final DenunciaDAO denDao = new DenunciaDAO();
    private final AccaoDAO accDao = new AccaoDAO();
    private final CategoriaDAO catDao = new CategoriaDAO();
    private final EsquadraDAO esqDao = new EsquadraDAO();

    public List<Categoria> categorias() throws NegocioException {
        try { return catDao.listar(); } catch (SQLException e) { throw NegocioException.de(e); }
    }

    /**
     * Regista uma denuncia. cidadao == null => anonima. A esquadra mais proxima, a gravidade e a
     * emergencia sao atribuidas pelo trigger trg_denuncia_encaminhar.
     */
    public Denuncia registar(Utilizador cidadao, Categoria cat, String descricao, double latO, double lonO,
                             Double latD, Double lonD, boolean anonima) throws NegocioException {
        if (cat == null) throw new NegocioException("Escolha a categoria da ocorrencia.");
        if (Validacao.vazio(descricao) || descricao.trim().length() < 10)
            throw new NegocioException("A descricao deve ter pelo menos 10 caracteres.");
        if (descricao.trim().length() > 500) throw new NegocioException("A descricao nao pode exceder 500 caracteres.");
        if (!Validacao.latitudeValida(latO) || !Validacao.longitudeValida(lonO))
            throw new NegocioException("Coordenadas da ocorrencia fora do intervalo valido.");
        if (!anonima && (cidadao == null || cidadao.getIdCidadao() == null))
            throw new NegocioException("Apenas cidadaos autenticados podem fazer denuncias identificadas.");
        Denuncia d = new Denuncia();
        d.setIdCategoria(cat.getId());
        d.setDescricao(descricao.trim());
        d.setLatOcorrencia(latO);
        d.setLonOcorrencia(lonO);
        d.setAnonima(anonima);
        if (anonima) {
            d.setIdCidadao(null);           // privacidade: nem a localizacao do denunciante e guardada
        } else {
            d.setIdCidadao(cidadao.getIdCidadao());
            if (latD != null && lonD != null) {
                if (!Validacao.latitudeValida(latD) || !Validacao.longitudeValida(lonD))
                    throw new NegocioException("Coordenadas do denunciante fora do intervalo valido.");
                d.setLatDenunciante(latD);
                d.setLonDenunciante(lonD);
            }
        }
        try { return denDao.inserir(d); } catch (SQLException e) { throw NegocioException.de(e); }
    }

    public List<Denuncia> doCidadao(Utilizador u) throws NegocioException {
        try { return denDao.listarPorCidadao(u.getIdCidadao()); } catch (SQLException e) { throw NegocioException.de(e); }
    }

    public List<Denuncia> doAgente(Utilizador agente, boolean apenasAbertas) throws NegocioException {
        try { return denDao.listarPorEsquadra(agente.getIdEsquadra(), apenasAbertas); }
        catch (SQLException e) { throw NegocioException.de(e); }
    }

    public List<String[]> anexos(int idDenuncia) throws NegocioException {
        try { return denDao.anexos(idDenuncia); } catch (SQLException e) { throw NegocioException.de(e); }
    }

    public List<Accao> accoes(int idDenuncia) throws NegocioException {
        try { return accDao.listarPorDenuncia(idDenuncia); } catch (SQLException e) { throw NegocioException.de(e); }
    }

    /** Regista accao; novoEstado == null mantem o estado. O trigger valida a jurisdicao do agente. */
    public void registarAccao(Utilizador agente, Denuncia d, String descricao, String novoEstado) throws NegocioException {
        if (agente.getMatricula() == null) throw new NegocioException("Apenas agentes podem registar accoes.");
        if (d == null) throw new NegocioException("Seleccione uma denuncia.");
        if (d.isEncerrada()) throw new NegocioException("A denuncia esta encerrada (" + d.getEstado() + ").");
        if (Validacao.vazio(descricao) || descricao.trim().length() < 5)
            throw new NegocioException("Descreva a accao (minimo 5 caracteres).");
        if (descricao.trim().length() > 300) throw new NegocioException("A descricao da accao nao pode exceder 300 caracteres.");
        Accao a = new Accao();
        a.setIdDenuncia(d.getId());
        a.setMatricula(agente.getMatricula());
        a.setDescricao(descricao.trim());
        a.setNovoEstado(novoEstado);
        try { accDao.inserir(a); } catch (SQLException e) { throw NegocioException.de(e); }
    }

    public Esquadra esquadraMaisProxima(double lat, double lon) throws NegocioException {
        if (!Validacao.latitudeValida(lat) || !Validacao.longitudeValida(lon))
            throw new NegocioException("Coordenadas invalidas.");
        try {
            Esquadra e = esqDao.maisProxima(lat, lon);
            if (e == null) throw new NegocioException("Nao existem esquadras registadas.");
            return e;
        } catch (SQLException ex) { throw NegocioException.de(ex); }
    }
}
