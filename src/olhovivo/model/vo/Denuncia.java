package olhovivo.model.vo;

import java.sql.Timestamp;

/** Value Object: denuncia (com os nomes de categoria e esquadra ja resolvidos). */
public class Denuncia {
    private int id, idCategoria, gravidade;
    private Integer idCidadao, idEsquadra;
    private String categoria, esquadra, descricao, estado;
    private Timestamp dataHora;
    private double latOcorrencia, lonOcorrencia;
    private Double latDenunciante, lonDenunciante;   // null se anonima ou nao partilhada
    private boolean emergencia, anonima;

    public Denuncia() { }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }
    public int getGravidade() { return gravidade; }
    public void setGravidade(int gravidade) { this.gravidade = gravidade; }
    public Integer getIdCidadao() { return idCidadao; }
    public void setIdCidadao(Integer idCidadao) { this.idCidadao = idCidadao; }
    public Integer getIdEsquadra() { return idEsquadra; }
    public void setIdEsquadra(Integer idEsquadra) { this.idEsquadra = idEsquadra; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getEsquadra() { return esquadra; }
    public void setEsquadra(String esquadra) { this.esquadra = esquadra; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Timestamp getDataHora() { return dataHora; }
    public void setDataHora(Timestamp dataHora) { this.dataHora = dataHora; }
    public double getLatOcorrencia() { return latOcorrencia; }
    public void setLatOcorrencia(double v) { this.latOcorrencia = v; }
    public double getLonOcorrencia() { return lonOcorrencia; }
    public void setLonOcorrencia(double v) { this.lonOcorrencia = v; }
    public Double getLatDenunciante() { return latDenunciante; }
    public void setLatDenunciante(Double v) { this.latDenunciante = v; }
    public Double getLonDenunciante() { return lonDenunciante; }
    public void setLonDenunciante(Double v) { this.lonDenunciante = v; }
    public boolean isEmergencia() { return emergencia; }
    public void setEmergencia(boolean emergencia) { this.emergencia = emergencia; }
    public boolean isAnonima() { return anonima; }
    public void setAnonima(boolean anonima) { this.anonima = anonima; }
    public boolean isEncerrada() { return "Resolvida".equals(estado) || "Rejeitada".equals(estado); }
}
