package olhovivo.model.vo;

/** Value Object: categoria de ocorrencia (nivel 1 Baixa, 2 Media, 3 Alta). */
public class Categoria {
    private int id, nivel;
    private String nome;

    public Categoria() { }
    public Categoria(int id, String nome, int nivel) { this.id = id; this.nome = nome; this.nivel = nivel; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }
    @Override public String toString() { return nome; }
}
