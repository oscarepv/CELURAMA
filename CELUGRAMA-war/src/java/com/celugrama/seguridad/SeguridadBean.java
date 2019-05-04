/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.seguridad;

import com.celugrama.auxiliares.AuxiliarEmpleado;
import java.io.IOException;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.icefaces.ace.model.MenuModel;

/**
 *
 * @author edison
 */
@ManagedBean(name = "seguridadsoyyo")
@SessionScoped
public class SeguridadBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private String usr;
    private String usrLoggeado;
    private AuxiliarEmpleado entidad;
    private MenuModel modelo;

    /**
     * Creates a new instance of SeguridadBean
     */
    public SeguridadBean() {
    }

    public String login() {
//        if ((getUsr() == null) || (getUsr().trim().isEmpty())) {
//           MensajesErrores.advertencia("Ingrese un usuario válido");
//            return null;
//        }
//        if ((getPwd() == null) || (getPwd().trim().isEmpty())) {
//           MensajesErrores.advertencia("Ingrese una clave válida");
//            return null;
//        }
//        setEntidad(ejbLogin.logearSistema(getUsr(), pwd));
//
//        if (entidad == null) {
//            MensajesErrores.fatal("Usuario y/o contraseña incorrecto");
//            return null;
//        }
//        if (entidad.getUserid() == null) {
//            MensajesErrores.fatal(entidad.getTextoEmail());
//            return null;
//        }
//        setUsrLoggeado(getEntidad().getApellidos() + " " + getEntidad().getNombres());
//        setModelo(getMenuPrincipal());
//        inspectorLoggeado = traerInspector(entidad.getPin());
        return "/PrincipalVista.jsf?faces-redirect=true";
    }

//    private Inspectores traerInspector(String cedula) {
//        Map parametros = new HashMap();
//        parametros.put(";where", "o.cedula=:cedula");
//        parametros.put("cedula", cedula);
//        List<Inspectores> listainspectores;
//        try {
//            listainspectores = ejbInspectores.encontarParametros(parametros);
//            for (Inspectores d : listainspectores) {
//
//                return d;
//            }
//        } catch (ConsultarException ex) {
//            Logger.getLogger(SeguridadBean.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return null;
//    }
//    public MenuModel getMenuPrincipal() {
//        if (getEntidad() == null) {
//            return null;
//        }
//        MenuModel retorno = new DefaultMenuModel();
//
//        List<Menusistema> ml;
//        try {
//            ml = ejbLogin.menusDisponibles(7);
//            for (Menusistema m : ml) {
//                Submenu nuevoSubmenu = new Submenu();
//                nuevoSubmenu.setId("sm_" + (m.getId()));
//                nuevoSubmenu.setLabel(m.getTexto());
//                // traer los perfiles
//                List<Perfil> pl = ejbLogin.perfiles(getEntidad().getTextoEmail(), m.getId());
//                //nuevoSubmenu.getChildren().clear();
//                for (Perfil p : pl) {
//                    org.icefaces.ace.component.menuitem.MenuItem nuevo = new org.icefaces.ace.component.menuitem.MenuItem();
//                    nuevo.setId(nuevoSubmenu.getId() + "_mmi_" + p.getId());
//                    nuevo.setValue(p.getMenu().getTexto());
//                    nuevo.setUrl(p.getMenu().getFormulario() + ".jsf?faces-redirect=true");
//                    nuevoSubmenu.getChildren().add(nuevo);
//                }
//                retorno.addSubmenu(nuevoSubmenu);
//            }
//        } catch (ConsultarException_Exception ex) {
//            Logger.getLogger(SeguridadBean.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return retorno;
//    }
    public String irDenuncias() {
        return "/DenunciasVista.jsf?faces-redirect=true";
    }

    public String logout() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        String ctxPath = ((ServletContext) ctx.getContext()).getContextPath();

        try {

            ctx.redirect(ctxPath + "/LoginVista.jsf");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
//    public String logout() {
//        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
//        String ctxPath = ((ServletContext) ctx.getContext()).getContextPath();
//
//        try {
//            ((HttpSession) ctx.getSession(false)).invalidate();
//            ctx.redirect(ctxPath + "/LoginVista.jsf");
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }

    /**
     * @return the usr
     */
    public String getUsr() {
        return usr;
    }

    /**
     * @param usr the usr to set
     */
    public void setUsr(String usr) {
        this.usr = usr;
    }

//    /**
//     * @return the pwd
//     */
//    public String getPwd() {
//        return pwd;
//    }
//
//    /**
//     * @param pwd the pwd to set
//     */
//    public void setPwd(String pwd) {
//        this.pwd = pwd;
//    }
    /**
     * @return the usrLoggeado
     */
    public String getUsrLoggeado() {
        return usrLoggeado;
    }

    /**
     * @param usrLoggeado the usrLoggeado to set
     */
    public void setUsrLoggeado(String usrLoggeado) {
        this.usrLoggeado = usrLoggeado;
    }

    /**
     * @return the entidad
     */
    public AuxiliarEmpleado getEntidad() {
        return entidad;
    }

    /**
     * @param entidad the entidad to set
     */
    public void setEntidad(AuxiliarEmpleado entidad) {
        this.entidad = entidad;
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

//    /**
//     * @return the inspectorLoggeado
//     */
//    public Inspectores getInspectorLoggeado() {
//        return inspectorLoggeado;
//    }
//
//    /**
//     * @param inspectorLoggeado the inspectorLoggeado to set
//     */
//    public void setInspectorLoggeado(Inspectores inspectorLoggeado) {
//        this.inspectorLoggeado = inspectorLoggeado;
//    }
}
