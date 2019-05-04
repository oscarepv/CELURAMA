/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.seguridad;

import com.celugrama.comun.ParametrosCelugramaBean;
import com.celugrama.entidades.Codigos;
import com.celugrama.entidades.Entidades;
import com.celugrama.entidades.Gruposusuarios;
import com.celugrama.entidades.Perfiles;
import com.celugrama.excepciones.ConsultarException;
import com.celugrama.excepciones.GrabarException;
import com.celugrama.excepciones.InsertarException;
import com.celugrama.servicios.GruposusuariosFacade;
import com.celugrama.utilitarios.Formulario;
import com.celugrama.utilitarios.MensajesErrores;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

/**
 *
 * @author sigi-iepi
 */
@ManagedBean(name = "usuariosGrpCelugrama")
@ViewScoped
public class UsuariosGrpBean implements Serializable {

    /**
     * Creates a new instance of UsuariosGruposBean
     */
    public UsuariosGrpBean() {
    }
    
    @ManagedProperty(value = "#{parametrosCelugrama}")
    private ParametrosCelugramaBean seguridadbean;
    @ManagedProperty(value = "#{entidadesCelugrama}")
    private EntidadesBean usariosBean;
    
    private Gruposusuarios grupoUsuario;
    private Codigos modulo;
    private Formulario formulario = new Formulario();
    private Perfiles perfil;
    
    @EJB
    private GruposusuariosFacade ejbGrpUsr;

    @PostConstruct
    private void activar() {

        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String perfil = (String) params.get("p");
        String redirect = (String) params.get("faces-redirect");
        if (redirect == null) {
            return;
        }
        String nombreForma = "UsrGrpVista";
        if (perfil == null) {
            MensajesErrores.fatal("Sin perfil válido");
            getSeguridadbean().cerraSession();
        }
        this.setPerfil(getSeguridadbean().traerPerfil(perfil));
        if (this.getPerfil() == null) {
            MensajesErrores.fatal("Sin perfil válido");
            getSeguridadbean().cerraSession();
        }
        if (nombreForma.contains(this.getPerfil().getMenu().getFormulario())) {
            if (!this.perfil.getGrupo().equals(seguridadbean.getGrpUsuario().getGrupo())) {
                MensajesErrores.fatal("Sin perfil válido, grupo invalido");
                getSeguridadbean().cerraSession();
            }
        }
    }



    public String grabar() {
        try {
            if (grupoUsuario.getId() == null) {
                grupoUsuario.setModulo(modulo);
                ejbGrpUsr.create(getGrupoUsuario(), getSeguridadbean().getLogueado().getUserid());
            } else {
                grupoUsuario.setModulo(modulo);
                ejbGrpUsr.edit(getGrupoUsuario(), getSeguridadbean().getLogueado().getUserid());
            }
        } catch (GrabarException | InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger(UsuariosGrpBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        usariosBean.setEntidad(null);
        usariosBean.setNombresBuscar(null);
        return null;
    }

    public String buscar() {
        formulario.cancelar();
        Entidades e = usariosBean.getEntidad();
        if (e == null) {
            MensajesErrores.informacion("Es necesario un usuario válido");
            return null;
        }

        grupoUsuario = new Gruposusuarios();
        grupoUsuario.setUsuario(e);
        grupoUsuario.setModulo(modulo);
        try {
            Map parametros = new HashMap();
//            parametros.put(";where", "o.usuario=:usuario and o.modulo=:modulo");
            parametros.put(";where", "o.usuario=:usuario");
            parametros.put("usuario", e);
//            parametros.put("modulo", modulo);

            List<Gruposusuarios> lgu = ejbGrpUsr.encontarParametros(parametros);
            if (!((lgu == null) || (lgu.isEmpty()))) {
                setGrupoUsuario(lgu.get(0));
//                formulario.editar();
            }

        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
            Logger.getLogger(UsuariosGrpBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        formulario.editar();
        return null;
    }


    /**
     * @return the usariosBean
     */
    public EntidadesBean getUsariosBean() {
        return usariosBean;
    }

    /**
     * @param usariosBean the usariosBean to set
     */
    public void setUsariosBean(EntidadesBean usariosBean) {
        this.usariosBean = usariosBean;
    }

    /**
     * @return the grupoUsuario
     */
    public Gruposusuarios getGrupoUsuario() {
        return grupoUsuario;
    }

    /**
     * @param grupoUsuario the grupoUsuario to set
     */
    public void setGrupoUsuario(Gruposusuarios grupoUsuario) {
        this.grupoUsuario = grupoUsuario;
    }

    /**
     * @return the modulo
     */
    public Codigos getModulo() {
        return modulo;
    }

    /**
     * @param modulo the modulo to set
     */
    public void setModulo(Codigos modulo) {
        this.modulo = modulo;
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
     * @return the seguridadbean
     */
    public ParametrosCelugramaBean getSeguridadbean() {
        return seguridadbean;
    }

    /**
     * @param seguridadbean the seguridadbean to set
     */
    public void setSeguridadbean(ParametrosCelugramaBean seguridadbean) {
        this.seguridadbean = seguridadbean;
    }

   
}
