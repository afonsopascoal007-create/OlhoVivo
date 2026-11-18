package olhovivo;

import javax.swing.SwingUtilities;
import olhovivo.controller.LoginController;
import olhovivo.view.UI;

/** Ponto de entrada do Olho Vivo. */
public class Main {
    public static void main(String[] args) {
        UI.tema();
        // toda a interface Swing e criada na thread de eventos
        SwingUtilities.invokeLater(LoginController::abrir);
    }
}
