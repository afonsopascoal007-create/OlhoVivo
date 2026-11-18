package olhovivo.model.bo;

import java.sql.SQLException;
import olhovivo.model.dao.UtilizadorDAO;
import olhovivo.model.vo.Utilizador;
import olhovivo.util.Hash;
import olhovivo.util.NegocioException;
import olhovivo.util.Validacao;

/** Regras de negocio de autenticacao e registo de cidadaos. */
public class AutenticacaoBO {
    private final UtilizadorDAO dao = new UtilizadorDAO();

    public Utilizador entrar(String contacto, String senha) throws NegocioException {
        if (Validacao.vazio(contacto) || Validacao.vazio(senha))
            throw new NegocioException("Indique o contacto e a palavra-passe.");
        try {
            Utilizador u = dao.autenticar(Validacao.normalizarContacto(contacto), Hash.sha256(senha));
            if (u == null) throw new NegocioException("Contacto ou palavra-passe incorrectos (ou conta inactiva).");
            return u;
        } catch (SQLException e) { throw NegocioException.de(e); }
    }

    public void registarCidadao(String nome, String contacto, String email, String senha, String confirmar)
            throws NegocioException {
        String c = Validacao.normalizarContacto(contacto);
        if (Validacao.vazio(nome) || nome.trim().length() > 60) throw new NegocioException("Nome obrigatorio (maximo 60 caracteres).");
        if (!Validacao.contactoValido(c)) throw new NegocioException("Contacto invalido. Exemplo: +258840000001.");
        if (senha == null || senha.length() < 6) throw new NegocioException("A palavra-passe deve ter pelo menos 6 caracteres.");
        if (!senha.equals(confirmar)) throw new NegocioException("As palavras-passe nao coincidem.");
        try { dao.registarCidadao(nome.trim(), c, email == null ? null : email.trim(), Hash.sha256(senha)); }
        catch (SQLException e) { throw NegocioException.de(e); }
    }

    public int pontuacao(Utilizador u) throws NegocioException {
        try { return u.getIdCidadao() == null ? 0 : dao.pontuacao(u.getIdCidadao()); }
        catch (SQLException e) { throw NegocioException.de(e); }
    }
}
