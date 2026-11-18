package olhovivo.controller;

import javax.swing.*;
import olhovivo.model.bo.AutenticacaoBO;
import olhovivo.model.vo.Utilizador;
import olhovivo.util.NegocioException;
import olhovivo.view.*;

/** Liga a LoginView ao modelo e encaminha cada perfil para a sua janela. */
public class LoginController {
    private final LoginView view;
    private final AutenticacaoBO bo = new AutenticacaoBO();

    public LoginController(LoginView view) {
        this.view = view;
        view.btnEntrar.addActionListener(e -> entrar());
        view.btnRegistar.addActionListener(e -> registar());
        view.btnAnonima.addActionListener(e -> anonima());
    }

    public static void abrir() {
        LoginView v = new LoginView();
        new LoginController(v);
        v.setVisible(true);
    }

    private void entrar() {
        view.btnEntrar.setEnabled(false);
        // trabalho de base de dados fora da thread de eventos (SwingWorker)
        new SwingWorker<Utilizador, Void>() {
            @Override protected Utilizador doInBackground() throws Exception {
                return bo.entrar(view.txtContacto.getText(), new String(view.txtSenha.getPassword()));
            }
            @Override protected void done() {
                view.btnEntrar.setEnabled(true);
                try {
                    Utilizador u = get();
                    view.dispose();
                    switch (u.getPerfil()) {
                        case "CIDADAO": new CidadaoController(u).abrir(); break;
                        case "AGENTE": new AgenteController(u).abrir(); break;
                        default: new AdminController(u).abrir();
                    }
                } catch (Exception ex) {
                    Throwable c = ex.getCause() != null ? ex.getCause() : ex;
                    UI.erro(view, c.getMessage());
                    view.txtSenha.setText("");
                }
            }
        }.execute();
    }

    private void registar() {
        RegistoDialog d = new RegistoDialog(view);
        d.btnCriar.addActionListener(e -> {
            try {
                bo.registarCidadao(d.txtNome.getText(), d.txtContacto.getText(), d.txtEmail.getText(),
                        new String(d.txtSenha.getPassword()), new String(d.txtConfirmar.getPassword()));
                UI.info(d, "Conta criada. Ja pode entrar com o seu contacto.");
                view.txtContacto.setText(olhovivo.util.Validacao.normalizarContacto(d.txtContacto.getText()));
                d.dispose();
            } catch (NegocioException ex) { UI.erro(d, ex.getMessage()); }
        });
        d.setVisible(true);
    }

    private void anonima() {
        view.dispose();
        new CidadaoController(null).abrir();
    }
}
