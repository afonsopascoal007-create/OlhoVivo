package olhovivo.view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import olhovivo.model.vo.Categoria;
import olhovivo.model.vo.Denuncia;

/** Janela do cidadao (ou do denunciante anonimo, quando o utilizador e null). */
public class CidadaoView extends JFrame {
    private static final String LAT = "-25.9692", LON = "32.5732";   // Baixa de Maputo
    private final java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

    public final JComboBox<Categoria> cmbCategoria = new JComboBox<>();
    public final JTextArea txtDescricao = new JTextArea(5, 30);
    public final JTextField txtLatO = new JTextField(LAT, 12), txtLonO = new JTextField(LON, 12);
    public final JCheckBox chkMesmoLocal = new JCheckBox("O meu local e o da ocorrencia", true);
    public final JTextField txtLatD = new JTextField(LAT, 12), txtLonD = new JTextField(LON, 12);
    public final JCheckBox chkAnonima = new JCheckBox("Denuncia anonima (nao fica ligada a mim)");
    public final JButton btnEnviar = new JButton("Enviar denuncia");
    public final JButton btnLimpar = new JButton("Limpar");

    public final DefaultTableModel modeloDenuncias = UI.modelo("ID", "Data/hora", "Categoria", "Esquadra", "Estado");
    public final JButton btnActualizar = new JButton("Actualizar");
    public final JLabel lblPontuacao = new JLabel(" ");

    public final JTextField txtLatP = new JTextField(LAT, 12), txtLonP = new JTextField(LON, 12);
    public final JButton btnProcurar = new JButton("Procurar esquadra mais proxima");
    public final JTextArea txtResultado = new JTextArea(6, 30);

    public final JButton btnSair = new JButton("Terminar sessao");

    public CidadaoView(String nome, boolean anonimo) {
        super("Olho Vivo - " + (anonimo ? "Denuncia anonima" : "Cidadao: " + nome));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        txtDescricao.setLineWrap(true);
        txtDescricao.setWrapStyleWord(true);
        txtResultado.setEditable(false);
        chkAnonima.setSelected(anonimo);
        chkAnonima.setEnabled(!anonimo);
        txtLatD.setEnabled(false);
        txtLonD.setEnabled(false);
        // evento: alternar entre "o meu local = ocorrencia" e coordenadas proprias
        chkMesmoLocal.addActionListener(e -> {
            boolean mesmo = chkMesmoLocal.isSelected();
            txtLatD.setEnabled(!mesmo);
            txtLonD.setEnabled(!mesmo);
        });
        chkAnonima.addActionListener(e -> chkMesmoLocal.setEnabled(!chkAnonima.isSelected()));
        if (anonimo) chkMesmoLocal.setEnabled(false);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Nova denuncia", painelNova());
        if (!anonimo) {
            abas.addTab("As minhas denuncias", painelMinhas());
            abas.addTab("Esquadra mais proxima", painelEsquadra());
        }

        JPanel raiz = new JPanel(new BorderLayout(6, 6));
        raiz.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        raiz.add(abas, BorderLayout.CENTER);
        raiz.add(UI.botoes(btnSair), BorderLayout.SOUTH);
        setContentPane(raiz);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel painelNova() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        JPanel f = UI.formulario("Categoria:", cmbCategoria,
                "Descricao:", UI.rolar(txtDescricao, 340, 100),
                "Latitude da ocorrencia:", txtLatO, "Longitude da ocorrencia:", txtLonO,
                "", chkMesmoLocal,
                "Latitude do denunciante:", txtLatD, "Longitude do denunciante:", txtLonD,
                "", chkAnonima);
        p.add(f, BorderLayout.CENTER);
        p.add(UI.botoes(btnEnviar, btnLimpar), BorderLayout.SOUTH);
        return p;
    }

    private JPanel painelMinhas() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.add(UI.rolar(UI.tabela(modeloDenuncias), 560, 260), BorderLayout.CENTER);
        JPanel s = new JPanel(new BorderLayout());
        s.add(UI.botoes(btnActualizar), BorderLayout.WEST);
        s.add(lblPontuacao, BorderLayout.EAST);
        p.add(s, BorderLayout.SOUTH);
        return p;
    }

    private JPanel painelEsquadra() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.add(UI.formulario("Latitude:", txtLatP, "Longitude:", txtLonP), BorderLayout.NORTH);
        p.add(UI.rolar(txtResultado, 400, 140), BorderLayout.CENTER);
        p.add(UI.botoes(btnProcurar), BorderLayout.SOUTH);
        return p;
    }

    public void carregarCategorias(List<Categoria> l) {
        cmbCategoria.removeAllItems();
        for (Categoria c : l) cmbCategoria.addItem(c);
    }

    public void preencherDenuncias(List<Denuncia> l) {
        modeloDenuncias.setRowCount(0);
        for (Denuncia d : l)
            modeloDenuncias.addRow(new Object[]{d.getId(), fmt.format(d.getDataHora()), d.getCategoria(), d.getEsquadra(), d.getEstado()});
    }

    public void limpar() {
        txtDescricao.setText("");
        txtLatO.setText(LAT); txtLonO.setText(LON);
        txtLatD.setText(LAT); txtLonD.setText(LON);
    }
}
