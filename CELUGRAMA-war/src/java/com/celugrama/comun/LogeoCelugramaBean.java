/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.comun;

import com.celugrama.entidades.Entidades;
import com.celugrama.entidades.Gruposusuarios;
import com.celugrama.entidades.Menusistema;
import com.celugrama.entidades.Perfiles;
import com.celugrama.excepciones.ConsultarException;
import com.celugrama.excepciones.GrabarException;
import com.celugrama.seguridad.SeguridadBean;
import com.celugrama.servicios.EntidadesFacade;
import com.celugrama.servicios.GruposusuariosFacade;
import com.celugrama.servicios.MenusistemaFacade;
import com.celugrama.servicios.PerfilesFacade;
import com.celugrama.utilitarios.Codificador;
import com.celugrama.utilitarios.Formulario;
import com.celugrama.utilitarios.MensajesErrores;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.icefaces.ace.component.submenu.Submenu;
import org.icefaces.ace.model.DefaultMenuModel;
import org.icefaces.ace.model.MenuModel;

@ManagedBean(name = "logueoCelugrama")
@SessionScoped
public class LogeoCelugramaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Entidades usuario;
    private String userid;
    private String contrasenia;
    private Gruposusuarios grupo;
    private MenuModel modelo;
    private String formaSeleccionada;
    private Formulario formulario;
    private String claveNueva;
    private String claveRetpeada;
    private String claveAnterior;

    @EJB
    private EntidadesFacade ejbEntidades;
    @EJB
    private MenusistemaFacade ejbMenusistema;
    @EJB
    private GruposusuariosFacade ejbGruposusuarios;
    @EJB
    private PerfilesFacade ejbPerfiles;
    @ManagedProperty(value = "#{parametrosCelugrama}")
    private ParametrosCelugramaBean parametrosSeguridad;

    public LogeoCelugramaBean() {
    }

    public String login() {
        try {
            grupo = null;
            modelo = null;
            if ((getUserid() == null) && (getUserid().trim().isEmpty())) {
                MensajesErrores.advertencia("Ingrese un usuario válido");
                return null;
            }
            if ((getContrasenia() == null) && (getContrasenia().trim().isEmpty())) {
                MensajesErrores.advertencia("Ingrese una clave válida");
                return null;
            }

            Codificador c = new Codificador();
            usuario = ejbEntidades.login(getUserid(), c.getEncoded(getContrasenia().trim(), "MD5"));

            if (usuario == null) {
                MensajesErrores.advertencia("Usuario no registrado, o clave invalida");
                return null;
            }
            parametrosSeguridad.setLogueado(usuario);

            Map parametros = new HashMap();
            parametros.put(";where", " o.usuario=:usuario");
            parametros.put("usuario", usuario);
            List<Gruposusuarios> aux = ejbGruposusuarios.encontarParametros(parametros);

            for (Gruposusuarios g : aux) {
                grupo = g;
                modelo = getMenuPrincipal();

            }
            if (grupo == null) {
                MensajesErrores.advertencia("Sin perfil válido, grupo invalido");
                return null;
            }

            return grupo.getModulo().getParametros().trim() + "/PrincipalVista.jsf?faces-redirect=true";
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger(LogeoCelugramaBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public MenuModel getMenuPrincipal() {

        if (usuario == null) {
            return null;
        }
        MenuModel retorno = new DefaultMenuModel();

        List<Menusistema> ml;
        try {
            Map parametros = new HashMap();
            parametros.put(";where", " o.modulo=:modulo");
            parametros.put("modulo", grupo.getModulo());
            parametros.put(";orden", " o.texto");
            ml = ejbMenusistema.encontarParametros(parametros);

            for (Menusistema m : ml) {
                Submenu nuevoSubmenu = new Submenu();
                nuevoSubmenu.setId("sm_" + (m.getId()));
                nuevoSubmenu.setLabel(m.getTexto());
                // traer los perfiles
                parametros = new HashMap();
                parametros.put(";where", " o.menu.idpadre=:menu and o.grupo=:grupo");
                parametros.put(";orden", " o.menu.texto");
                parametros.put("menu", m);
                parametros.put("grupo", grupo.getGrupo());
                List<Perfiles> pl = ejbPerfiles.encontarParametros(parametros);
                //nuevoSubmenu.getChildren().clear();
                for (Perfiles p : pl) {
                    org.icefaces.ace.component.menuitem.MenuItem nuevo = new org.icefaces.ace.component.menuitem.MenuItem();
                    nuevo.setId(nuevoSubmenu.getId() + "_mmi_" + p.getId());
                    nuevo.setValue(p.getMenu().getTexto());
                    nuevo.setUrl("../" + grupo.getModulo().getParametros().trim() + "/" + p.getMenu().getFormulario().trim() + ".jsf?faces-redirect=true&p=" + p.getId());
                    nuevoSubmenu.getChildren().add(nuevo);
                }

                retorno.addSubmenu(nuevoSubmenu);
            }

        } catch (ConsultarException ex) {
            Logger.getLogger(LogeoCelugramaBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return retorno;
    }

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        String ctxPath = ((ServletContext) ctx.getContext()).getContextPath();
        try {
            ctx.redirect(ctxPath);
        } catch (IOException ex) {
            Logger.getLogger(SeguridadBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public String cambio() {
        // dos password nueva y una anterior
        try {
            if ((claveNueva == null) && (claveNueva.trim().isEmpty())) {
                MensajesErrores.advertencia("Ingrese una clave nueva válida");
                return null;
            }
            if ((claveRetpeada == null) && (claveRetpeada.trim().isEmpty())) {
                MensajesErrores.advertencia("Ingrese una clave repetida válida ");
                return null;
            }

            Codificador c = new Codificador();
            String cnCodificada = c.getEncoded(claveNueva, "MD5");
            String cnCodificadaR = c.getEncoded(claveRetpeada, "MD5");
            if (!cnCodificada.equals(cnCodificadaR)) {
                MensajesErrores.advertencia("Claves no válida, inténtelo de nuevo.");
                return null;
            }

            usuario.setPwd(cnCodificada);
            ejbEntidades.edit(usuario, null);
        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger("LOGIN").log(Level.SEVERE, null, ex);
        }
        MensajesErrores.informacion("Su clave ha sido cambiada con éxito!");

        return null;
    }

    public String salir() {
        return grupo.getModulo().getParametros().trim() + "/PrincipalVista.jsf?faces-redirect=true";
    }

    public String cambiar() {
        return "/CambioClaveVista.jsf?faces-redirect=true";
    }

    /**
     * @return the usuario
     */
    public Entidades getUsuario() {
        return usuario;
    }

    /**
     * @param usuario the usuario to set
     */
    public void setUsuario(Entidades usuario) {
        this.usuario = usuario;
    }

    /**
     * @return the grupo
     */
    public Gruposusuarios getGrupo() {
        return grupo;
    }

    /**
     * @param grupo the grupo to set
     */
    public void setGrupo(Gruposusuarios grupo) {
        this.grupo = grupo;
    }

    /**
     * @return the modelo
     */
    public MenuModel getModelo() {
        return modelo;
    }

    /**
     * @param modelo the modelo to set
     */
    public void setModelo(MenuModel modelo) {
        this.modelo = modelo;
    }

    /**
     * @return the parametrosSeguridad
     */
    public ParametrosCelugramaBean getParametrosSeguridad() {
        return parametrosSeguridad;
    }

    /**
     * @param parametrosSeguridad the parametrosSeguridad to set
     */
    public void setParametrosSeguridad(ParametrosCelugramaBean parametrosSeguridad) {
        this.parametrosSeguridad = parametrosSeguridad;
    }

    /**
     * @return the formaSeleccionada
     */
    public String getFormaSeleccionada() {
        return formaSeleccionada;
    }

    /**
     * @param formaSeleccionada the formaSeleccionada to set
     */
    public void setFormaSeleccionada(String formaSeleccionada) {
        this.formaSeleccionada = formaSeleccionada;
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

    /**
     * @return the claveNueva
     */
    public String getClaveNueva() {
        return claveNueva;
    }

    /**
     * @param claveNueva the claveNueva to set
     */
    public void setClaveNueva(String claveNueva) {
        this.claveNueva = claveNueva;
    }

    /**
     * @return the claveRetpeada
     */
    public String getClaveRetpeada() {
        return claveRetpeada;
    }

    /**
     * @param claveRetpeada the claveRetpeada to set
     */
    public void setClaveRetpeada(String claveRetpeada) {
        this.claveRetpeada = claveRetpeada;
    }

    /**
     * @return the claveAnterior
     */
    public String getClaveAnterior() {
        return claveAnterior;
    }

    /**
     * @param claveAnterior the claveAnterior to set
     */
    public void setClaveAnterior(String claveAnterior) {
        this.claveAnterior = claveAnterior;
    }

    /**
     * @return the userid
     */
    public String getUserid() {
        return userid;
    }

    /**
     * @param userid the userid to set
     */
    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * @return the contrasenia
     */
    public String getContrasenia() {
        return contrasenia;
    }

    /**
     * @param contrasenia the contrasenia to set
     */
    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

}
