/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.comun;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import com.celugrama.entidades.Entidades;
import com.celugrama.entidades.Gruposusuarios;
import com.celugrama.entidades.Perfiles;
import com.celugrama.excepciones.ConsultarException;
import com.celugrama.excepciones.GrabarException;
import com.celugrama.servicios.EntidadesFacade;
import com.celugrama.servicios.GruposusuariosFacade;
import com.celugrama.servicios.PerfilesFacade;
import com.celugrama.utilitarios.Codificador;
import com.celugrama.utilitarios.Formulario;
import com.celugrama.utilitarios.MensajesErrores;

/**
 *
 * @author sigi-iepi
 *
 *
 * Bean para guardar la informacion sucursales usurario logeado y punto de venta
 * hasta el momento
 */
@ManagedBean(name = "parametrosCelugrama")
@SessionScoped
public class ParametrosCelugramaBean implements Serializable {

    /**
     * Creates a new instance of ParametrosSeguridadBean
     */
    public ParametrosCelugramaBean() {
    }
//    private Usuarioempresa usrEmpresa;
    private Entidades logueado;
    private Gruposusuarios grpUsuario;
    private String clave;
    private String estacion;
    private String claveNueva;
    private String claveNuevaRetipeada;
    private String claveAnterior;
    private Formulario formulario = new Formulario();
    @EJB
    protected GruposusuariosFacade ejbGrpUsr;
    @EJB
    protected EntidadesFacade ejbEntidad;
    @EJB
    protected PerfilesFacade ejbPerfiles;

//    @EJB
//    protected UsuariosFacade ejbUsuarios;
    /**
     * @return the logueado
     */
    public Entidades getLogueado() {
        return logueado;
    }

    public Date getFechaActual() {
        return new Date();
    }

    /**
     * @param logueado the logueado to set
     */
    public void setLogueado(Entidades logueado) {
        // traer grupo usuario
        grpUsuario = null;
        if (logueado != null) {
            try {

                Map parametros = new HashMap();
                parametros.put(";where", "o.usuario=:usuario");
                parametros.put("usuario", logueado);
                List<Gruposusuarios> lgu = ejbGrpUsr.encontarParametros(parametros);
                if (!((lgu == null) || (lgu.isEmpty()))) {
                    grpUsuario = lgu.get(0);
                }
            } catch (ConsultarException ex) {
                MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
                Logger.getLogger(ParametrosCelugramaBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        this.logueado = logueado;
    }

    public String cerraSession() {
        logout();
        return null;
    }

    public void logout() {
        ExternalContext ctx
                = FacesContext.getCurrentInstance().getExternalContext();
        String ctxPath
                = ((ServletContext) ctx.getContext()).getContextPath();

        try {
            // Usar el contexto de JSF para invalidar la sesión,
            // NO EL DE SERVLETS (nada de HttpServletRequest)

            // Redirección de nuevo con el contexto de JSF,
            // si se usa una HttpServletResponse fallará.
            // Sin embargo, como ya está fuera del ciclo de vida 
            // de JSF se debe usar la ruta completa -_-U
            ctx.redirect(ctxPath + "");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
//        ((HttpSession) ctx.getSession(false)).invalidate();
    }



    public String cambiarClaveNueva() {
        setClave(null);
        setClaveNueva(null);
        setClaveNuevaRetipeada(null);
        formulario.insertar();
        return null;
    }

    public String cambiarClave() {
        if ((getClave() == null) || (getClave().trim().isEmpty())) {
            MensajesErrores.advertencia("Ingrese la clave actual");
            return null;
        }
        if ((getClaveNueva() == null) || (getClaveNueva().trim().isEmpty())) {
            MensajesErrores.advertencia("Ingrese la clave nueva");
            return null;
        }
        if ((getClaveNuevaRetipeada() == null) || (getClaveNuevaRetipeada().trim().isEmpty())) {
            MensajesErrores.advertencia("retipee la clave nueva");
            return null;
        }
        if (!(claveNueva.equals(claveNuevaRetipeada))) {
            MensajesErrores.advertencia("Clave nueva debe ser igual a la clave retipeada");
            return null;
        }
        Codificador c = new Codificador();
        String claveCodificada = c.getEncoded(getClave(), "MD5");
        if (!(logueado.getPwd().equals(claveCodificada))) {
            MensajesErrores.advertencia("Clave anterior no es la correcta");
            return null;
        }
        claveCodificada = c.getEncoded(getClaveNueva(), "MD5");
        logueado.setPwd(claveCodificada);
        try {
            ejbEntidad.edit(logueado, logueado.getUserid());
        } catch (GrabarException ex) {
            Logger.getLogger("").log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        MensajesErrores.advertencia("Clave se cambio correctamente");
        return null;
    }


    public String cambiar() {
        return "CambioClaveVista.jsf?faces-redirect=true";
    }



    public Perfiles traerPerfil(String id) {
        Integer c = Integer.valueOf(id);
        return ejbPerfiles.find(c);
    }
    public Entidades traerCedula(String cedula)
            throws ConsultarException {
        Map parametros = new HashMap();
        parametros.put(";where", "o.pin=:pin and o.activo=true");
        parametros.put("pin", cedula);
        List<Entidades> lista = ejbEntidad.encontarParametros(parametros);
        for (Entidades e : lista) {

            return e;
        }

        return null;
    }


    /**
     * @return the grpUsuario
     */
    public Gruposusuarios getGrpUsuario() {
        return grpUsuario;
    }

    /**
     * @param grpUsuario the grpUsuario to set
     */
    public void setGrpUsuario(Gruposusuarios grpUsuario) {
        this.grpUsuario = grpUsuario;
    }

    /**
     * @return the clave
     */
    public String getClave() {
        return clave;
    }

    /**
     * @param clave the clave to set
     */
    public void setClave(String clave) {
        this.clave = clave;
    }

    /**
     * @return the estacion
     */
    public String getEstacion() {
        return estacion;
    }

    /**
     * @param estacion the estacion to set
     */
    public void setEstacion(String estacion) {
        this.estacion = estacion;
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
     * @return the claveNuevaRetipeada
     */
    public String getClaveNuevaRetipeada() {
        return claveNuevaRetipeada;
    }

    /**
     * @param claveNuevaRetipeada the claveNuevaRetipeada to set
     */
    public void setClaveNuevaRetipeada(String claveNuevaRetipeada) {
        this.claveNuevaRetipeada = claveNuevaRetipeada;
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
}
