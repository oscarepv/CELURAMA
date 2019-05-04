/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.seguridad;

import com.celugrama.comun.ParametrosCelugramaBean;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import com.celugrama.entidades.Codigos;
import com.celugrama.entidades.Menusistema;
import com.celugrama.entidades.Perfiles;
import com.celugrama.excepciones.BorrarException;
import com.celugrama.excepciones.ConsultarException;
import com.celugrama.excepciones.GrabarException;
import com.celugrama.excepciones.InsertarException;
import com.celugrama.servicios.MenusistemaFacade;
import com.celugrama.servicios.PerfilesFacade;
import com.celugrama.utilitarios.Combos;
import com.celugrama.utilitarios.Formulario;
import com.celugrama.utilitarios.MensajesErrores;
import javax.faces.context.FacesContext;

/**
 *
 * @author sigi-iepi
 */
@ManagedBean(name = "perfilesCelugrama")
@ViewScoped
public class PerfilesBean implements Serializable {

    /**
     * Creates a new instance of MenusBean
     */
    public PerfilesBean() {
    }
    @ManagedProperty(value = "#{parametrosCelugrama}")
    private ParametrosCelugramaBean parametrosSeguiridad;

    private Formulario formulario = new Formulario();
    private List<Perfiles> perfiles;
    private Perfiles perfil;
    private Menusistema menuSeleccionado;
//    private Menusistema submenuSeleccionado;
    @EJB
    private PerfilesFacade ejbPerfiles;
    @EJB
    private MenusistemaFacade ejbMenus;
    private Codigos grupo;
    private Perfiles perfilSeleccionado;
  

    /**
     * @return the perfil
     */
    public Perfiles getPerfilSeleccionado() {
        return perfilSeleccionado;
    }

    /**
     * @param perfil the perfil to set
     */
    public void setPerfilSeleccionado(Perfiles perfil) {
        this.perfilSeleccionado = perfil;
    }

    @PostConstruct
   private void activar() {
        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String redirect = (String) params.get("faces-redirect");
        if (redirect == null) {
            return;
        }

        String p = (String) params.get("p");
        String nombreForma = "PerfilesVista";

        if (p == null) {
            MensajesErrores.fatal("Sin perfil válido");
            getParametrosSeguiridad().cerraSession();
        }
        perfil = getParametrosSeguiridad().traerPerfil((String) params.get("p"));
        if (this.perfil == null) {
            MensajesErrores.fatal("Sin perfil válido");
            getParametrosSeguiridad().cerraSession();
        }
        if (nombreForma.contains(perfil.getMenu().getFormulario())) {
            if (!this.perfil.getGrupo().equals(parametrosSeguiridad.getGrpUsuario().getGrupo())) {
                MensajesErrores.fatal("Sin perfil válido, grupo invalido");
                getParametrosSeguiridad().cerraSession();
            }
        }
    }

    /**
     * @return the formulario
     */
    public Formulario getFormulario() {
        return formulario;
    }

    /**
     * @param formulario the formulario to set
     */
    public void setFormulario(Formulario formulario) {
        this.formulario = formulario;
    }

    // colocamos los metodos en verbo
    public String crear() {
        // se deberia chequear perfil?

        if ((grupo == null)) {
            MensajesErrores.advertencia("Por favor seleccione un grupo");
            return null;
        }
        if (menuSeleccionado == null) {
            MensajesErrores.advertencia("Seleccione un menú primero");
            return null;
        }
        perfil = new Perfiles();
        perfil.setGrupo(grupo);
        formulario.insertar();
        return null;
    }

    public String modificar() {

        perfil = perfiles.get(formulario.getFila().getRowIndex());
        formulario.editar();
        return null;
    }

    public String eliminar() {

        perfil = perfiles.get(formulario.getFila().getRowIndex());
        formulario.eliminar();
        return null;
    }

    public String cancelar() {
        formulario.cancelar();
        buscar();
        return null;
    }
    // buscar

    public String buscar() {

        if ((grupo == null)) {
            MensajesErrores.advertencia("Por favor seleccione un grupo");
            return null;
        }
        if (menuSeleccionado == null) {
            MensajesErrores.advertencia("Seleccione un menú primero");
            return null;
        }
        try {
            Map parametros = new HashMap();
            parametros.put(";where", "o.grupo=:grupo and o.menu.idpadre=:menu");
            parametros.put(";orden", "o.menu.texto");
            parametros.put("menu", menuSeleccionado);
            parametros.put("grupo", grupo);
            perfiles = ejbPerfiles.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger(PerfilesBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
    // acciones de base de datos

    private boolean validar() {
        if ((perfil.getGrupo() == null)) {
            MensajesErrores.advertencia("Es necesario grupo");
            return true;
        }

        if ((perfil.getMenu() == null)) {
            MensajesErrores.advertencia("Es necesario seleccionar un  Menú");
            return true;
        }
        return false;
    }

    public String insertar() {

        if (validar()) {
            return null;
        }
        try {
            ejbPerfiles.create(perfil, getParametrosSeguiridad().getLogueado().getUserid());
        } catch (InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(PerfilesBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String grabar() {

        if (validar()) {
            return null;
        }
        try {
            ejbPerfiles.edit(perfil, getParametrosSeguiridad().getLogueado().getUserid());
        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(PerfilesBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String borrar() {
//        if (!menusBean.getPerfil().getBorrado()) {
//            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
//            return null;
//        }
        try {
            ejbPerfiles.remove(perfil, getParametrosSeguiridad().getLogueado().getUserid());
        } catch (BorrarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(PerfilesBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        formulario.cancelar();
        buscar();
        return null;
    }
    // falta el combo

    /**
     * @return the grupo
     */
    public Codigos getGrupo() {
        return grupo;
    }

    /**
     * @param grupo the grupo to set
     */
    public void setGrupo(Codigos grupo) {
        this.grupo = grupo;
    }

    /**
     * @return the perfiles
     */
    public List<Perfiles> getPerfiles() {
        return perfiles;
    }

    /**
     * @param perfiles the perfiles to set
     */
    public void setPerfiles(List<Perfiles> perfiles) {
        this.perfiles = perfiles;
    }

    /**
     * @return the perfil
     */
    public Perfiles getPerfil() {
        return perfil;
    }

    /**
     * @param perfil the perfil to set
     */
    public void setPerfil(Perfiles perfil) {
        this.perfil = perfil;
    }

    /**
     * @return the menuSeleccionado
     */
    public Menusistema getMenuSeleccionado() {
        return menuSeleccionado;
    }

    /**
     * @param menuSeleccionado the menuSeleccionado to set
     */
    public void setMenuSeleccionado(Menusistema menuSeleccionado) {
        this.menuSeleccionado = menuSeleccionado;
    }

    public SelectItem[] getComboDisponibles() {
        Map parametros = new HashMap();
        parametros.put(";where", "o.idpadre=:padre");
        parametros.put(";orden", "o.texto");
        parametros.put("padre", menuSeleccionado);
        try {
            List<Menusistema> menus = ejbMenus.encontarParametros(parametros);
            List<Menusistema> disponibles = new LinkedList<>();
            for (Menusistema m : menus) {
                parametros = new HashMap();
                parametros.put(";where", "o.grupo=:grupo and o.menu=:menu");
//                parametros.put(";orden", "o.menu.texto");
                parametros.put("menu", m);
                parametros.put("grupo", grupo);
                List<Perfiles> lp = ejbPerfiles.encontarParametros(parametros);
                if ((lp == null) || (lp.isEmpty())) {
                    disponibles.add(m);
                }
            }
            return Combos.getSelectItems(disponibles, true);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(PerfilesBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * @return the parametrosSeguiridad
     */
    public ParametrosCelugramaBean getParametrosSeguiridad() {
        return parametrosSeguiridad;
    }

    /**
     * @param parametrosSeguiridad the parametrosSeguiridad to set
     */
    public void setParametrosSeguiridad(ParametrosCelugramaBean parametrosSeguiridad) {
        this.parametrosSeguiridad = parametrosSeguiridad;
    }



}
