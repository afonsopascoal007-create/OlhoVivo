package olhovivo.view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import olhovivo.model.vo.Esquadra;

/** Dialogo do administrador para registar um agente policial. */
public class NovoAgenteDialog extends JDialog {
    public final JTextField txtNome = new JTextField(20), txtContacto = new JTextField(20), txtEmail = new JTextField(20),
            txtMatricula = new JTextField(12), txtCargo = new JTextField(20);
    public final JPasswordField txtSenha = new JPasswordField(20);
    public final JComboBox<Esquadra> cmbEsquadra = new JComboBox<>();
    public final JButton btnGuardar = new JButton("Guardar"), btnCancelar = new JButton("Cancelar");

    public NovoAgenteDialog(Frame pai, List<Esquadra> esquadras) {
        super(pai, "Novo agente policial", true);
        for (Esquadra e : esquadras) cmbEsquadra.addItem(e);
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        p.add(UI.formulario("Nome:", txtNome, "Contacto:", txtContacto, "E-mail (opcional):", txtEmail,
                "Matricula:", txtMatricula, "Cargo:", txtCargo, "Esquadra:", cmbEsquadra,
                "Palavra-passe:", txtSenha), BorderLayout.CENTER);
        p.add(UI.botoes(btnGuardar, btnCancelar), BorderLayout.SOUTH);
        btnCancelar.addActionListener(e -> dispose());
        setContentPane(p);
        pack();
        setLocationRelativeTo(pai);
    }
}
