package olhovivo.view;

import java.awt.*;
import javax.swing.*;

/** Janela de entrada: login, criacao de conta e denuncia anonima. */
public class LoginView extends JFrame {
    public final JTextField txtContacto = new JTextField(18);
    public final JPasswordField txtSenha = new JPasswordField(18);
    public final JButton btnEntrar = new JButton("Entrar");
    public final JButton btnRegistar = new JButton("Criar conta de cidadao");
    public final JButton btnAnonima = new JButton("Fazer denuncia anonima");

    public LoginView() {
        super("Olho Vivo - Entrar");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel topo = new JPanel(new GridLayout(2, 1));
        topo.add(UI.titulo("OLHO VIVO"));
        JLabel sub = new JLabel("Plataforma de denuncias comunitarias - PRM");
        sub.setForeground(Color.GRAY);
        topo.add(sub);
        raiz.add(topo, BorderLayout.NORTH);

        raiz.add(UI.formulario("Contacto:", txtContacto, "Palavra-passe:", txtSenha), BorderLayout.CENTER);

        JPanel sul = new JPanel(new GridLayout(3, 1, 0, 6));
        btnEntrar.setBackground(UI.AZUL);
        btnEntrar.setForeground(Color.WHITE);
        sul.add(btnEntrar);
        sul.add(btnRegistar);
        sul.add(btnAnonima);
        raiz.add(sul, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnEntrar);
        setContentPane(raiz);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }
}
