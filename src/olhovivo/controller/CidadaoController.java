package olhovivo.controller;

import java.util.List;
import olhovivo.model.bo.AutenticacaoBO;
import olhovivo.model.bo.DenunciaBO;
import olhovivo.model.vo.Categoria;
import olhovivo.model.vo.Denuncia;
import olhovivo.model.vo.Esquadra;
import olhovivo.model.vo.Utilizador;
import olhovivo.util.NegocioException;
import olhovivo.util.Validacao;
import olhovivo.view.CidadaoView;
import olhovivo.view.UI;

/** Controlador da janela do cidadao. utilizador == null => denunciante anonimo. */
public class CidadaoController {
    private final Utilizador u;
    private final CidadaoView view;
    private final DenunciaBO bo = new DenunciaBO();
    private final AutenticacaoBO auth = new AutenticacaoBO();

    public CidadaoController(Utilizador u) {
        this.u = u;
        this.view = new CidadaoView(u == null ? "" : u.getNome(), u == null);
        view.btnEnviar.addActionListener(e -> enviar());
        view.btnLimpar.addActionListener(e -> view.limpar());
        view.btnActualizar.addActionListener(e -> actualizar());
        view.btnProcurar.addActionListener(e -> procurar());
        view.btnSair.addActionListener(e -> { view.dispose(); LoginController.abrir(); });
    }

    public void abrir() {
        try { view.carregarCategorias(bo.categorias()); }
        catch (NegocioException e) { UI.erro(view, e.getMessage()); }
        if (u != null) actualizar();
        view.setVisible(true);
    }

    private void enviar() {
        try {
            boolean anonima = u == null || view.chkAnonima.isSelected();
            double latO = Validacao.numero(view.txtLatO.getText(), "Latitude da ocorrencia");
            double lonO = Validacao.numero(view.txtLonO.getText(), "Longitude da ocorrencia");
            Double latD = null, lonD = null;
            if (!anonima) {
                boolean mesmo = view.chkMesmoLocal.isSelected();
                latD = mesmo ? latO : Validacao.numero(view.txtLatD.getText(), "Latitude do denunciante");
                lonD = mesmo ? lonO : Validacao.numero(view.txtLonD.getText(), "Longitude do denunciante");
            }
            Denuncia d = bo.registar(u, (Categoria) view.cmbCategoria.getSelectedItem(), view.txtDescricao.getText(),
                    latO, lonO, latD, lonD, anonima);
            UI.info(view, "Denuncia #" + d.getId() + " registada.\nEsquadra responsavel: " + d.getEsquadra()
                    + "\nGravidade: " + d.getGravidade() + (d.isEmergencia() ? "  (EMERGENCIA)" : "")
                    + "\nEstado: " + d.getEstado());
            view.limpar();
            if (u != null) actualizar();
        } catch (NegocioException e) { UI.erro(view, e.getMessage()); }
    }

    private void actualizar() {
        try {
            List<Denuncia> l = bo.doCidadao(u);
            view.preencherDenuncias(l);
            view.lblPontuacao.setText("Pontuacao de confianca: " + auth.pontuacao(u) + "   ");
        } catch (NegocioException e) { UI.erro(view, e.getMessage()); }
    }

    private void procurar() {
        try {
            double lat = Validacao.numero(view.txtLatP.getText(), "Latitude");
            double lon = Validacao.numero(view.txtLonP.getText(), "Longitude");
            Esquadra e = bo.esquadraMaisProxima(lat, lon);
            view.txtResultado.setText(e.getNome() + "\nContacto: " + e.getContacto() + "\nEndereco: " + e.getEndereco()
                    + String.format("%nDistancia: %.2f km", e.getDistanciaKm()));
        } catch (NegocioException e) { UI.erro(view, e.getMessage()); }
    }
}
