package olhovivo.view;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import olhovivo.model.bo.DenunciaBO;
import olhovivo.model.vo.Denuncia;

/** Painel do agente policial: denuncias da sua esquadra, emergencias a vermelho. */
public class AgenteView extends JFrame {
    private final java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final List<Denuncia> actual = new ArrayList<>();

    public final DefaultTableModel modelo = UI.modelo("ID", "Data/hora", "Categoria", "Grav.", "Estado", "Descricao");
    public final JTable tabela = UI.tabela(modelo);
    public final JCheckBox chkAbertas = new JCheckBox("So abertas (Pendente / Em Analise)", true);
    public final JButton btnActualizar = new JButton("Actualizar");
    public final JButton btnAccao = new JButton("Registar accao");
    public final JButton btnMapa = new JButton("Ver no mapa");
    public final JButton btnRelatorio = new JButton("Relatorio PDF");
    public final JButton btnSair = new JButton("Terminar sessao");
    public final JTextArea txtDetalhe = new JTextArea(9, 40);
    public final JLabel lblEstado = new JLabel(" ");
    private final JLabel lblCabecalho = new JLabel();

    public AgenteView(String agente, String matricula, String cargo, int idEsquadra) {
        super("Olho Vivo - Painel do agente");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        lblCabecalho.setText(agente + "  |  " + cargo + "  |  " + matricula + "  |  Esquadra n." + idEsquadra);
        lblCabecalho.setFont(lblCabecalho.getFont().deriveFont(Font.BOLD, 13f));
        lblCabecalho.setForeground(UI.AZUL);

        tabela.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(45);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(85);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(380);
        // emergencias a vermelho e negrito
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                boolean emerg = r >= 0 && r < actual.size() && actual.get(r).isEmergencia();
                comp.setFont(t.getFont().deriveFont(emerg ? Font.BOLD : Font.PLAIN));
                if (!sel) comp.setForeground(emerg ? UI.VERMELHO : Color.BLACK);
                return comp;
            }
        });
        txtDetalhe.setEditable(false);
        txtDetalhe.setLineWrap(true);
        txtDetalhe.setWrapStyleWord(true);
        txtDetalhe.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        barra.add(chkAbertas); barra.add(btnActualizar); barra.add(btnAccao);
        barra.add(btnMapa); barra.add(btnRelatorio); barra.add(btnSair);

        JPanel topo = new JPanel(new BorderLayout());
        topo.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 8));
        topo.add(lblCabecalho, BorderLayout.NORTH);
        topo.add(barra, BorderLayout.CENTER);

        JSplitPane divisao = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tabela), new JScrollPane(txtDetalhe));
        divisao.setResizeWeight(0.6);

        JPanel raiz = new JPanel(new BorderLayout(4, 4));
        raiz.add(topo, BorderLayout.NORTH);
        raiz.add(divisao, BorderLayout.CENTER);
        raiz.add(lblEstado, BorderLayout.SOUTH);
        setContentPane(raiz);
        setSize(900, 620);
        setLocationRelativeTo(null);
    }

    /** Preenche a tabela mantendo a denuncia seleccionada. Chamar na thread de eventos. */
    public void preencher(List<Denuncia> lista) {
        Denuncia sel = seleccionada();
        int idSel = sel == null ? -1 : sel.getId();
        actual.clear();
        actual.addAll(lista);
        modelo.setRowCount(0);
        int novaLinha = -1, i = 0;
        for (Denuncia d : lista) {
            modelo.addRow(new Object[]{d.getId(), fmt.format(d.getDataHora()),
                    d.getCategoria() + (d.isEmergencia() ? " !" : ""), d.getGravidade(), d.getEstado(), d.getDescricao()});
            if (d.getId() == idSel) novaLinha = i;
            i++;
        }
        if (novaLinha >= 0) tabela.setRowSelectionInterval(novaLinha, novaLinha);
        lblEstado.setText("  " + lista.size() + " denuncia(s). Ultima actualizacao: "
                + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));
    }

    public Denuncia seleccionada() {
        int r = tabela.getSelectedRow();
        return (r >= 0 && r < actual.size()) ? actual.get(r) : null;
    }

    public List<Denuncia> todas() { return new ArrayList<>(actual); }

    /** Pede a descricao e o novo estado ao agente. Devolve {descricao, estado|null} ou null. */
    public String[] pedirAccao(Denuncia d) {
        JTextArea desc = new JTextArea(4, 32);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        JComboBox<String> estado = new JComboBox<>();
        estado.addItem("(manter estado)");
        for (String s : DenunciaBO.ESTADOS) estado.addItem(s);
        JPanel p = UI.formulario("Denuncia:", new JLabel("#" + d.getId() + " - " + d.getCategoria()),
                "Accao realizada:", new JScrollPane(desc), "Novo estado:", estado);
        int r = JOptionPane.showConfirmDialog(this, p, "Registar accao policial",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return null;
        return new String[]{desc.getText(), estado.getSelectedIndex() == 0 ? null : (String) estado.getSelectedItem()};
    }
}
