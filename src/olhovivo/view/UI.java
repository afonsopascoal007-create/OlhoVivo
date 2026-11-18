package olhovivo.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Utilitarios de interface partilhados pelas janelas (tema, formularios, mensagens). */
public final class UI {
    public static final Color AZUL = new Color(0x1F3A5F);
    public static final Color VERMELHO = new Color(0xC62828);
    private UI() { }

    public static void tema() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception e) { /* mantem o aspecto por omissao */ }
    }

    public static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 20f));
        l.setForeground(AZUL);
        return l;
    }

    /** Formulario em grelha: pares (String rotulo, JComponent campo). */
    public static JPanel formulario(Object... pares) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.NORTHWEST;
        for (int i = 0; i < pares.length; i += 2) {
            g.gridy = i / 2;
            g.gridx = 0; g.weightx = 0; g.fill = GridBagConstraints.NONE;
            p.add(new JLabel((String) pares[i]), g);
            g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
            p.add((JComponent) pares[i + 1], g);
        }
        return p;
    }

    public static DefaultTableModel modelo(String... colunas) {
        return new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static JTable tabela(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setRowHeight(22);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    public static JScrollPane rolar(JComponent c, int largura, int altura) {
        JScrollPane s = new JScrollPane(c);
        s.setPreferredSize(new Dimension(largura, altura));
        return s;
    }

    public static JPanel botoes(JComponent... bs) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        for (JComponent b : bs) p.add(b);
        return p;
    }

    public static void erro(Component pai, String msg) {
        JOptionPane.showMessageDialog(pai, msg, "Olho Vivo", JOptionPane.ERROR_MESSAGE);
    }
    public static void info(Component pai, String msg) {
        JOptionPane.showMessageDialog(pai, msg, "Olho Vivo", JOptionPane.INFORMATION_MESSAGE);
    }
    public static boolean confirmar(Component pai, String msg) {
        return JOptionPane.showConfirmDialog(pai, msg, "Olho Vivo", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
