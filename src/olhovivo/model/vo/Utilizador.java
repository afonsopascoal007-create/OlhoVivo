package olhovivo.model.vo;

/** Value Object: utilizador autenticado (com os dados do seu perfil). */
public class Utilizador {
    private int id;
    private String nome, contacto, email, perfil;
    private boolean activo;
    private Integer idCidadao;      // so para CIDADAO
    private int pontuacao;          // so para CIDADAO
    private String matricula, cargo; // so para AGENTE
    private Integer idEsquadra;     // so para AGENTE

    public Utilizador() { }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Integer getIdCidadao() { return idCidadao; }
    public void setIdCidadao(Integer idCidadao) { this.idCidadao = idCidadao; }
    public int getPontuacao() { return pontuacao; }
    public void setPontuacao(int pontuacao) { this.pontuacao = pontuacao; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public Integer getIdEsquadra() { return idEsquadra; }
    public void setIdEsquadra(Integer idEsquadra) { this.idEsquadra = idEsquadra; }
    @Override public String toString() { return nome; }
}
