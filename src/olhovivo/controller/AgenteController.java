package olhovivo.controller;

import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import olhovivo.model.bo.DenunciaBO;
import olhovivo.model.vo.Accao;
import olhovivo.model.vo.Denuncia;
import olhovivo.model.vo.Utilizador;
import olhovivo.util.NegocioException;
import olhovivo.util.PainelRefresher;
import olhovivo.util.RelatorioPDF;
import olhovivo.view.AgenteView;
import olhovivo.view.UI;

/** Controlador do painel do agente: eventos de botoes, rato, janela e refresco automatico (thread). */
public class AgenteController {
    private static final int SEGUNDOS_REFRESCO = 10;
    private final Utilizador agente;
    private final AgenteView view;
    private final DenunciaBO bo = new DenunciaBO();
    private final PainelRefresher refresher;

    public AgenteController(Utilizador agente) {
        this.agente = agente;
        this.view = new AgenteView(agente.getNome(), agente.getMatricula(), agente.getCargo(), agente.getIdEsquadra());
        // refresco em segundo plano: consulta na thread auxiliar, desenho na thread de eventos
        this.refresher = new PainelRefresher(() -> {
            try {
                final List<Denuncia> l = bo.doAgente(agente, view.chkAbertas.isSelected());
                SwingUtilities.invokeLater(() -> view.preencher(l));
            } catch (NegocioException e) {
                SwingUtilities.invokeLater(() -> view.lblEstado.setText("  Erro ao actualizar: " + e.getMessage()));
            }
        }, SEGUNDOS_REFRESCO);

        view.btnActualizar.addActionListener(e -> carregar());
        view.chkAbertas.addActionListener(e -> carregar());
        view.btnAccao.addActionListener(e -> registarAccao());
        view.btnMapa.addActionListener(e -> mapa());
        view.btnRelatorio.addActionListener(e -> relatorio());
        view.btnSair.addActionListener(e -> sair(true));
        // evento de selecao na tabela => mostra detalhe
        view.tabela.getSelectionModel().addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) detalhe(); });
        // evento de rato: duplo clique abre o registo de accao
        view.tabela.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) registarAccao(); }
        });
        // evento de janela: parar a thread antes de fechar
        view.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (UI.confirmar(view, "Sair do Olho Vivo?")) { refresher.parar(); System.exit(0); }
            }
        });
    }

    public void abrir() {
        carregar();
        view.setVisible(true);
        refresher.iniciar();
    }

    private void sair(boolean voltarAoLogin) {
        refresher.parar();
        view.dispose();
        if (voltarAoLogin) LoginController.abrir();
    }

    private void carregar() {
        try { view.preencher(bo.doAgente(agente, view.chkAbertas.isSelected())); }
        catch (NegocioException e) { UI.erro(view, e.getMessage()); }
    }

    private void detalhe() {
        Denuncia d = view.seleccionada();
        if (d == null) { view.txtDetalhe.setText(""); return; }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "Denuncia #%d  |  %s  |  Gravidade %d%s  |  Estado: %s%n",
                d.getId(), d.getCategoria(), d.getGravidade(), d.isEmergencia() ? " (EMERGENCIA)" : "", d.getEstado()));
        sb.append("Descricao: ").append(d.getDescricao()).append('\n');
        sb.append(String.format(Locale.ROOT, "Local da ocorrencia: %.6f, %.6f%n", d.getLatOcorrencia(), d.getLonOcorrencia()));
        if (d.isAnonima()) sb.append("Denunciante: ANONIMO (localizacao protegida)\n");
        else if (d.getLatDenunciante() != null)
            sb.append(String.format(Locale.ROOT, "Local do denunciante: %.6f, %.6f%n", d.getLatDenunciante(), d.getLonDenunciante()));
        try {
            for (String[] a : bo.anexos(d.getId())) sb.append("Anexo [").append(a[0]).append("]: ").append(a[1]).append('\n');
            sb.append("--- Accoes policiais ---\n");
            List<Accao> acs = bo.accoes(d.getId());
            if (acs.isEmpty()) sb.append("(nenhuma)\n");
            for (Accao a : acs)
                sb.append(a.getDataHora()).append(" | ").append(a.getAgente()).append(": ").append(a.getDescricao())
                  .append(a.getNovoEstado() != null ? "  -> " + a.getNovoEstado() : "").append('\n');
        } catch (NegocioException e) { sb.append("Erro: ").append(e.getMessage()); }
        view.txtDetalhe.setText(sb.toString());
        view.txtDetalhe.setCaretPosition(0);
    }

    private void registarAccao() {
        Denuncia d = view.seleccionada();
        if (d == null) { UI.info(view, "Seleccione uma denuncia na tabela."); return; }
        String[] r = view.pedirAccao(d);
        if (r == null) return;
        try {
            bo.registarAccao(agente, d, r[0], r[1]);
            carregar();
            detalhe();
        } catch (NegocioException e) { UI.erro(view, e.getMessage()); }
    }

    private void mapa() {
        Denuncia d = view.seleccionada();
        if (d == null) { UI.info(view, "Seleccione uma denuncia na tabela."); return; }
        String url = String.format(Locale.ROOT, "https://www.google.com/maps?q=%.6f,%.6f", d.getLatOcorrencia(), d.getLonOcorrencia());
        try { Desktop.getDesktop().browse(new URI(url)); }
        catch (Exception e) { UI.info(view, "Abra no navegador:\n" + url); }
    }

    private void relatorio() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("relatorio_denuncias.pdf"));
        if (fc.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) return;
        try {
            List<Denuncia> todas = bo.doAgente(agente, false);   // historico completo da esquadra
            String esq = todas.isEmpty() ? "n." + agente.getIdEsquadra() : todas.get(0).getEsquadra();
            RelatorioPDF.gerar(fc.getSelectedFile(), esq, agente.getNome(), todas);
            UI.info(view, "Relatorio criado:\n" + fc.getSelectedFile().getAbsolutePath());
        } catch (NoClassDefFoundError e) {
            UI.erro(view, "Falta o jar do iText na pasta lib/ (ver LEIA-ME).");
        } catch (Exception e) { UI.erro(view, "Nao foi possivel criar o relatorio: " + e.getMessage()); }
    }
}
