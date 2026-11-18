package olhovivo.controller;

import java.util.List;
import java.util.Map;
import olhovivo.model.bo.AdminBO;
import olhovivo.model.vo.Categoria;
import olhovivo.model.vo.Esquadra;
import olhovivo.model.vo.Utilizador;
import olhovivo.util.NegocioException;
import olhovivo.util.Validacao;
import olhovivo.view.AdminView;
import olhovivo.view.NovoAgenteDialog;
import olhovivo.view.UI;

/** Controlador da janela do administrador. */
public class AdminController {
    private final AdminView view;
    private final AdminBO bo = new AdminBO();
    private List<Esquadra> esquadras;
    private List<Categoria> categorias;

    public AdminController(Utilizador admin) {
        view = new AdminView(admin.getNome());
        view.eNovo.addActionListener(e -> view.limparEsquadra());
        view.eGravar.addActionListener(e -> gravarEsquadra());
        view.eApagar.addActionListener(e -> apagarEsquadra());
        view.tabEsq.getSelectionModel().addListSelectionListener(e -> {
            int r = view.tabEsq.getSelectedRow();
            if (!e.getValueIsAdjusting() && r >= 0 && r < esquadras.size()) view.mostrarEsquadra(esquadras.get(r));
        });
        view.cNovo.addActionListener(e -> view.limparCategoria());
        view.cGravar.addActionListener(e -> gravarCategoria());
        view.cApagar.addActionListener(e -> apagarCategoria());
        view.tabCat.getSelectionModel().addListSelectionListener(e -> {
            int r = view.tabCat.getSelectedRow();
            if (!e.getValueIsAdjusting() && r >= 0 && r < categorias.size()) {
                Categoria c = categorias.get(r);
                view.mostrarCategoria(c.getId(), c.getNome(), c.getNivel());
            }
        });
        view.uActivar.addActionListener(e -> definirActivo(true));
        view.uDesactivar.addActionListener(e -> definirActivo(false));
        view.uNovoAgente.addActionListener(e -> novoAgente());
        view.sActualizar.addActionListener(e -> estatisticas());
        view.btnSair.addActionListener(e -> { view.dispose(); LoginController.abrir(); });
    }

    public void abrir() {
        carregarEsquadras(); carregarCategorias(); carregarUtilizadores(); estatisticas();
        view.setVisible(true);
    }

    // ---------- esquadras ----------
    private void carregarEsquadras() {
        try {
            esquadras = bo.esquadras();
            view.modeloEsq.setRowCount(0);
            for (Esquadra e : esquadras)
                view.modeloEsq.addRow(new Object[]{e.getId(), e.getNome(), e.getContacto(), e.getEndereco(),
                        e.getArea(), e.getLatitude(), e.getLongitude()});
        } catch (NegocioException ex) { UI.erro(view, ex.getMessage()); }
    }

    private void gravarEsquadra() {
        try {
            Esquadra e = new Esquadra();
            e.setId(view.idEsquadra());
            e.setNome(view.eNome.getText().trim());
            e.setContacto(view.eContacto.getText());
            e.setEndereco(view.eEndereco.getText().trim());
            e.setArea(view.eArea.getText().trim());
            e.setLatitude(Validacao.numero(view.eLat.getText(), "Latitude"));
            e.setLongitude(Validacao.numero(view.eLon.getText(), "Longitude"));
            bo.gravar(e);
            view.limparEsquadra();
            carregarEsquadras();
        } catch (NegocioException ex) { UI.erro(view, ex.getMessage()); }
    }

    private void apagarEsquadra() {
        if (view.idEsquadra() == 0) { UI.info(view, "Seleccione uma esquadra."); return; }
        if (!UI.confirmar(view, "Apagar a esquadra seleccionada?")) return;
        try { bo.apagarEsquadra(view.idEsquadra()); view.limparEsquadra(); carregarEsquadras(); }
        catch (NegocioException ex) { UI.erro(view, ex.getMessage()); }
    }

    // ---------- categorias ----------
    private void carregarCategorias() {
        try {
            categorias = bo.categorias();
            view.modeloCat.setRowCount(0);
            for (Categoria c : categorias) view.modeloCat.addRow(new Object[]{c.getId(), c.getNome(), c.getNivel()});
        } catch (NegocioException ex) { UI.erro(view, ex.getMessage()); }
    }

    private void gravarCategoria() {
        try {
            bo.gravar(new Categoria(view.idCategoria(), view.cNome.getText().trim(), (Integer) view.cNivel.getValue()));
            view.limparCategoria();
            carregarCategorias();
        } catch (NegocioException ex) { UI.erro(view, ex.getMessage()); }
    }

    private void apagarCategoria() {
        if (view.idCategoria() == 0) { UI.info(view, "Seleccione uma categoria."); return; }
        if (!UI.confirmar(view, "Apagar a categoria seleccionada?")) return;
        try { bo.apagarCategoria(view.idCategoria()); view.limparCategoria(); carregarCategorias(); }
        catch (NegocioException ex) { UI.erro(view, ex.getMessage()); }
    }

    // ---------- utilizadores ----------
    private void carregarUtilizadores() {
        try {
            view.modeloUti.setRowCount(0);
            for (Object[] l : bo.utilizadores()) view.modeloUti.addRow(l);
        } catch (NegocioException ex) { UI.erro(view, ex.getMessage()); }
    }

    private void definirActivo(boolean activo) {
        int r = view.tabUti.getSelectedRow();
        if (r < 0) { UI.info(view, "Seleccione um utilizador."); return; }
        try { bo.definirActivo((Integer) view.modeloUti.getValueAt(r, 0), activo); carregarUtilizadores(); }
        catch (NegocioException ex) { UI.erro(view, ex.getMessage()); }
    }

    private void novoAgente() {
        if (esquadras == null || esquadras.isEmpty()) { UI.info(view, "Crie primeiro uma esquadra."); return; }
        NovoAgenteDialog d = new NovoAgenteDialog(view, esquadras);
        d.btnGuardar.addActionListener(e -> {
            try {
                bo.registarAgente(d.txtNome.getText(), d.txtContacto.getText(), d.txtEmail.getText(),
                        new String(d.txtSenha.getPassword()), d.txtMatricula.getText(), d.txtCargo.getText(),
                        (Esquadra) d.cmbEsquadra.getSelectedItem());
                d.dispose();
                carregarUtilizadores();
            } catch (NegocioException ex) { UI.erro(d, ex.getMessage()); }
        });
        d.setVisible(true);
    }

    // ---------- estatisticas ----------
    private void estatisticas() {
        try {
            StringBuilder sb = new StringBuilder("DENUNCIAS POR ESTADO\n");
            for (Map.Entry<String, Integer> e : bo.estatisticas(false).entrySet())
                sb.append(String.format("  %-14s %d%n", e.getKey(), e.getValue()));
            sb.append("\nDENUNCIAS POR CATEGORIA\n");
            for (Map.Entry<String, Integer> e : bo.estatisticas(true).entrySet())
                sb.append(String.format("  %-18s %d%n", e.getKey(), e.getValue()));
            view.txtStats.setText(sb.toString());
        } catch (NegocioException ex) { UI.erro(view, ex.getMessage()); }
    }
}
