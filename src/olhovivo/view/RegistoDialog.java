package olhovivo.view;

import java.awt.*;
import javax.swing.*;

/** Dialogo de criacao de conta de cidadao. */
public class RegistoDialog extends JDialog {
    public final JTextField txtNome = new JTextField(20);
    public final JTextField txtContacto = new JTextField(20);
    public final JTextField txtEmail = new JTextField(20);
    public final JPasswordField txtSenha = new JPasswordField(20);
    public final JPasswordField txtConfirmar = new JPasswordField(20);
    public final JButton btnCriar = new JButton("Criar conta");
    public final JButton btnCancelar = new JButton("Cancelar");

    public RegistoDialog(Frame pai) {
        super(pai, "Criar conta de cidadao", true);
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        p.add(UI.formulario("Nome completo:", txtNome, "Contacto (+258...):", txtContacto,
                "E-mail (opcional):", txtEmail, "Palavra-passe (min. 6):", txtSenha,
                "Confirmar:", txtConfirmar), BorderLayout.CENTER);
        p.add(UI.botoes(btnCriar, btnCancelar), BorderLayout.SOUTH);
        btnCancelar.addActionListener(e -> dispose());
        setContentPane(p);
        pack();
        setLocationRelativeTo(pai);
    }
}
