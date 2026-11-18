package olhovivo.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import olhovivo.model.vo.Esquadra;

/** Janela do administrador: esquadras, categorias, utilizadores e estatisticas. */
public class AdminView extends JFrame {
    // esquadras
    public final DefaultTableModel modeloEsq = UI.modelo("ID", "Nome", "Contacto", "Endereco", "Area", "Lat", "Lon");
    public final JTable tabEsq = UI.tabela(modeloEsq);
    public final JTextField eNome = new JTextField(18), eContacto = new JTextField(18), eEndereco = new JTextField(18),
            eArea = new JTextField(18), eLat = new JTextField(10), eLon = new JTextField(10);
    public final JButton eNovo = new JButton("Novo"), eGravar = new JButton("Gravar"), eApagar = new JButton("Apagar");
    // categorias
    public final DefaultTableModel modeloCat = UI.modelo("ID", "Categoria", "Gravidade");
    public final JTable tabCat = UI.tabela(modeloCat);
    public final JTextField cNome = new JTextField(18);
    public final JSpinner cNivel = new JSpinner(new SpinnerNumberModel(1, 1, 3, 1));
    public final JButton cNovo = new JButton("Novo"), cGravar = new JButton("Gravar"), cApagar = new JButton("Apagar");
    // utilizadores
    public final DefaultTableModel modeloUti = UI.modelo("ID", "Nome", "Contacto", "Perfil", "Activo", "Matricula", "Esquadra");
    public final JTable tabUti = UI.tabela(modeloUti);
    public final JButton uActivar = new JButton("Activar"), uDesactivar = new JButton("Desactivar"),
            uNovoAgente = new JButton("Novo agente...");
    // estatisticas
    public final JTextArea txtStats = new JTextArea(12, 40);
    public final JButton sActualizar = new JButton("Actualizar");

    public final JButton btnSair = new JButton("Terminar sessao");
    private int idEsquadra, idCategoria;

    public AdminView(String nome) {
        super("Olho Vivo - Administrador: " + nome);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        txtStats.setEditable(false);
        txtStats.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JTabbedPane abas = new JTabbedPane();
        JPanel pe = new JPanel(new BorderLayout(6, 6));
        pe.add(UI.rolar(tabEsq, 760, 200), BorderLayout.CENTER);
        JPanel fe = new JPanel(new BorderLayout());
        fe.add(UI.formulario("Nome:", eNome, "Contacto:", eContacto, "Endereco:", eEndereco, "Area de jurisdicao:", eArea,
                "Latitude:", eLat, "Longitude:", eLon), BorderLayout.CENTER);
        fe.add(UI.botoes(eNovo, eGravar, eApagar), BorderLayout.SOUTH);
        pe.add(fe, BorderLayout.SOUTH);
        abas.addTab("Esquadras", pe);

        JPanel pc = new JPanel(new BorderLayout(6, 6));
        pc.add(UI.rolar(tabCat, 760, 200), BorderLayout.CENTER);
        JPanel fc = new JPanel(new BorderLayout());
        fc.add(UI.formulario("Nome:", cNome, "Gravidade (1-3):", cNivel), BorderLayout.CENTER);
        fc.add(UI.botoes(cNovo, cGravar, cApagar), BorderLayout.SOUTH);
        pc.add(fc, BorderLayout.SOUTH);
        abas.addTab("Categorias", pc);

        JPanel pu = new JPanel(new BorderLayout(6, 6));
        pu.add(UI.rolar(tabUti, 760, 300), BorderLayout.CENTER);
        pu.add(UI.botoes(uActivar, uDesactivar, uNovoAgente), BorderLayout.SOUTH);
        abas.addTab("Utilizadores", pu);

        JPanel ps = new JPanel(new BorderLayout(6, 6));
        ps.add(new JScrollPane(txtStats), BorderLayout.CENTER);
        ps.add(UI.botoes(sActualizar), BorderLayout.SOUTH);
        abas.addTab("Estatisticas", ps);

        JPanel raiz = new JPanel(new BorderLayout(6, 6));
        raiz.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        raiz.add(abas, BorderLayout.CENTER);
        raiz.add(UI.botoes(btnSair), BorderLayout.SOUTH);
        setContentPane(raiz);
        pack();
        setLocationRelativeTo(null);
    }

    public void limparEsquadra() {
        idEsquadra = 0;
        eNome.setText(""); eContacto.setText(""); eEndereco.setText(""); eArea.setText(""); eLat.setText(""); eLon.setText("");
        tabEsq.clearSelection();
    }
    public void mostrarEsquadra(Esquadra e) {
        idEsquadra = e.getId();
        eNome.setText(e.getNome()); eContacto.setText(e.getContacto()); eEndereco.setText(e.getEndereco());
        eArea.setText(e.getArea()); eLat.setText(String.valueOf(e.getLatitude())); eLon.setText(String.valueOf(e.getLongitude()));
    }
    public int idEsquadra() { return idEsquadra; }

    public void limparCategoria() { idCategoria = 0; cNome.setText(""); cNivel.setValue(1); tabCat.clearSelection(); }
    public void mostrarCategoria(int id, String nome, int nivel) { idCategoria = id; cNome.setText(nome); cNivel.setValue(nivel); }
    public int idCategoria() { return idCategoria; }
}
