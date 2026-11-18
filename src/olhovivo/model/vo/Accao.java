package olhovivo.model.vo;

import java.sql.Timestamp;

/** Value Object: accao policial registada sobre uma denuncia. */
public class Accao {
    private int id, idDenuncia;
    private String matricula, agente, descricao, novoEstado;
    private Timestamp dataHora;

    public Accao() { }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdDenuncia() { return idDenuncia; }
    public void setIdDenuncia(int idDenuncia) { this.idDenuncia = idDenuncia; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getAgente() { return agente; }
    public void setAgente(String agente) { this.agente = agente; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getNovoEstado() { return novoEstado; }
    public void setNovoEstado(String novoEstado) { this.novoEstado = novoEstado; }
    public Timestamp getDataHora() { return dataHora; }
    public void setDataHora(Timestamp dataHora) { this.dataHora = dataHora; }
}
