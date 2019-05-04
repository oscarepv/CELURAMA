/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.vistas;

import com.celugrama.comun.ParametrosCelugramaBean;
import com.celugrama.entidades.Entidades;
import com.celugrama.entidades.Menusistema;
import com.celugrama.entidades.Perfiles;
import com.celugrama.entidades.Roles;
import com.celugrama.excepciones.ConsultarException;
import com.celugrama.excepciones.GrabarException;
import com.celugrama.excepciones.InsertarException;
import com.celugrama.servicios.EntidadesFacade;
import com.celugrama.utilitarios.Formulario;
import com.celugrama.utilitarios.MensajesErrores;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
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
import org.icefaces.ace.component.fileentry.FileEntry;
import org.icefaces.ace.component.fileentry.FileEntryEvent;
import org.icefaces.ace.component.fileentry.FileEntryResults;
import org.icefaces.ace.model.table.LazyDataModel;
import org.icefaces.ace.model.table.SortCriteria;
import org.mozilla.universalchardet.UniversalDetector;

/**
 *
 * @author oscar
 */
@ManagedBean(name = "asistentesCelugrama")
@ViewScoped
public class AsistentesBean {

    public AsistentesBean() {
        entidades = new LazyDataModel<Entidades>() {
            @Override
            public List<Entidades> load(int i, int i1, SortCriteria[] scs, Map<String, String> map) {
                //
                return null;
            }
        };
    }

    @ManagedProperty(value = "#{parametrosCelugrama}")
    private ParametrosCelugramaBean seguridadbean;

    private LazyDataModel<Entidades> entidades;
    private List<Entidades> listaEntidades;
    private Entidades entidad;
    private Roles roles;
    private Entidades existente;
    private Formulario formulario = new Formulario();
    private Formulario formularioExiste = new Formulario();
    private Formulario formularioSubida = new Formulario();
    private List listaUsuarios;
    private Perfiles perfil;
    private String nombresBuscar;
    private String delimitador = ",";
    private Menusistema menu;

    @EJB
    private EntidadesFacade ejbEntidades;

    @PostConstruct
    private void activar() {
        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String perfil = (String) params.get("p");
        String redirect = (String) params.get("faces-redirect");
        if (redirect == null) {
            return;
        }
        String nombreForma = "AsistentesVista";
        if (perfil == null) {
            MensajesErrores.fatal("Sin perfil válido");
            getSeguridadbean().cerraSession();
        }
        this.perfil = getSeguridadbean().traerPerfil(perfil);

        if (this.getPerfil() == null) {
            MensajesErrores.fatal("Sin perfil válido");
            seguridadbean.logout();
            return;

        }
        if (this.perfil.getMenu().equals("PER")) {
            return;
        }
//        if (nombreForma==null){
//            nombreForma="";
//        }
        if (nombreForma.contains(this.getPerfil().getMenu().getFormulario())) {
            if (!this.perfil.getGrupo().equals(seguridadbean.getGrpUsuario().getGrupo())) {
                MensajesErrores.fatal("Sin perfil válido, grupo invalido");
                seguridadbean.logout();
            }
        }
    }

    public String crear() {
        if (!perfil.getNuevo()) {
            MensajesErrores.advertencia("No tiene autorización para crear un registro");
            return null;
        }
        entidad = new Entidades();
        int valor = 0;
        String codigo = generarCodigo(valor);
        Boolean parar = false;
        while (!parar) {
            Map parametros = new HashMap();
            parametros.put(";where", "o.codigo=:codigo");
            parametros.put("codigo", codigo);
            List<Entidades> listadoEntidades;
            try {
                listadoEntidades = ejbEntidades.encontarParametros(parametros);
                if (listadoEntidades.isEmpty()) {
                    parar = true;
                    break;
                }
            } catch (ConsultarException ex) {
                Logger.getLogger(AsistentesBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            valor++;
            codigo = generarCodigo(valor);

        }
        entidad.setCodigo(codigo);
        formulario.insertar();
        return null;
    }

    private String generarCodigo(int valor) {
        try {
            if (valor != 0) {
                Map parametros = new HashMap();
                parametros.put(";where", "o.tipo='A'");
                return "CODASIS-" + (ejbEntidades.contar(parametros) + valor);
            } else {
                Map parametros = new HashMap();
                parametros.put(";where", "o.tipo='A'");
                return "CODASIS-" + ejbEntidades.contar(parametros);
            }
        } catch (ConsultarException ex) {
            Logger.getLogger(AsistentesBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public String modificar() {
        if (!perfil.getModificacion()) {
            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
            return null;
        }
        entidad = (Entidades) entidades.getRowData();
        formulario.editar();
        return null;
    }

    public String eliminar() {
        if (!perfil.getBorrado()) {
            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
            return null;
        }

        entidad = (Entidades) entidades.getRowData();
        formulario.eliminar();
        return null;
    }

    public String buscar() {
        if (!perfil.getConsulta()) {
            MensajesErrores.advertencia("No tiene autorización para consultar");
            return null;
        }
        entidades = new LazyDataModel<Entidades>() {
            @Override
            public List<Entidades> load(int i, int pageSize, SortCriteria[] scs, Map<String, String> map) {
                Map parametros = new HashMap();
                if (scs.length == 0) {
                    parametros.put(";orden", "o.id desc");
                } else {
                    parametros.put(";orden", "o." + scs[0].getPropertyName()
                            + (scs[0].isAscending() ? " ASC" : " DESC"));
                }
                String where = "  o.activo=true and o.tipo = 'A'";
                for (Map.Entry e : map.entrySet()) {
                    String clave = (String) e.getKey();
                    String valor = (String) e.getValue();

                    where += " and upper(o." + clave + ") like :" + clave;
                    parametros.put(clave, valor.toUpperCase() + "%");
                }

                int total = 0;

                try {
                    parametros.put(";where", where);
                    total = ejbEntidades.contar(parametros);
                } catch (ConsultarException ex) {
                    MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
                    Logger.getLogger("").log(Level.SEVERE, null, ex);
                }
                int endIndex = i + pageSize;
                if (endIndex > total) {
                    endIndex = total;
                }

                parametros.put(";inicial", i);
                parametros.put(";final", endIndex);
                getEntidades().setRowCount(total);
                try {
                    return ejbEntidades.encontarParametros(parametros);
                } catch (ConsultarException ex) {
                    MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
                    Logger.getLogger("").log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };

        return null;

    }

    private boolean validar() {

        if ((entidad.getCodigo() == null) || (entidad.getCodigo().isEmpty())) {
            MensajesErrores.advertencia("Codigo es obligatorio");
            return true;
        }

        String codigo[] = entidad.getCodigo().split("-");

        if (!codigo[0].equals("CODASIS")) {
            MensajesErrores.advertencia("El codigo debe seguir el siguiente formato CODASIS-1");
            return true;
        }
//        if ((entidad.getIdentificacion() == null) || (entidad.getIdentificacion().isEmpty())) {
//            MensajesErrores.advertencia("CI o RUC es obligatorio");
//            return true;
//        }

        if ((entidad.getNombres() == null) || (entidad.getNombres().isEmpty())) {
            MensajesErrores.advertencia("Nombres es obligatorio");
            return true;
        }

//        if ((entidad.getEmail() == null) || (entidad.getEmail().isEmpty())) {
//            MensajesErrores.advertencia("Email es obligatorio");
//            return true;
//        }
        return false;
    }

    public String insertar() {
        if (!perfil.getNuevo()) {
            MensajesErrores.advertencia("No tiene autorización para consultar");
            return null;
        }
        if (validar()) {
            return null;
        }

        try {
            Map parametros = new HashMap();
            parametros.put(";where", "o.codigo=:codigo");
            parametros.put("codigo", entidad.getCodigo());
            List<Entidades> listadoEntidades = ejbEntidades.encontarParametros(parametros);
            if (!listadoEntidades.isEmpty()) {
                MensajesErrores.advertencia("Codigo de asistente ya existe");
                return null;
            }
            entidad.setActivo(Boolean.TRUE);
            entidad.setTipo("A");
//            Codificador cod = new Codificador();
//            entidad.setPwd(cod.getEncoded(entidad.getCodigo(), "MD5"));
            ejbEntidades.create(entidad, getSeguridadbean().getLogueado().getUserid());

        } catch (InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger("").log(Level.SEVERE, null, ex);
        } catch (ConsultarException ex) {
            Logger.getLogger(AsistentesBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String grabar() {
        if (!perfil.getModificacion()) {
            MensajesErrores.advertencia("No tiene autorización para consultar");
            return null;
        }

        if (validar()) {
            return null;
        }
        try {

            entidad.setActivo(Boolean.TRUE);
            ejbEntidades.edit(entidad, getSeguridadbean().getLogueado().getUserid());

        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger("").log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String borrar() {
        if (!perfil.getBorrado()) {
            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
            return null;
        }
        try {
            entidad.setActivo(Boolean.FALSE);
            ejbEntidades.edit(entidad, getSeguridadbean().getLogueado().getUserid());
        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger("").log(Level.SEVERE, null, ex);
        }

        formulario.cancelar();
        buscar();
        return null;
    }

    public String cargarDatos() {
        formularioSubida.insertar();
        return null;
    }

    public void archivoListenerSimple(FileEntryEvent e) {
        if (delimitador == null || delimitador.isEmpty()) {
            MensajesErrores.advertencia("Ingrese delimitador");
            return;
        }
        try {
            FileEntry fe = (FileEntry) e.getComponent();
            FileEntryResults results = fe.getResults();

            for (FileEntryResults.FileInfo i : results.getFiles()) {

                if (i.isSaved()) {

                    File file = i.getFile();
                    if (file != null) {
                        BufferedReader entrada = null;

                        FileInputStream fis = new FileInputStream(file);
                        String charset = "";
                        try {
                            charset = detectarCodificacion(fis);
                            if (charset == null) {
                                charset = "UTF-8";
                            }
                        } catch (Throwable ex) {
                            Logger.getLogger(AsistentesBean.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        entrada = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset.equals("WINDOWS-1252") ? "ISO-8859-1" : charset));
                        String sb;
                        // linea de cabeceras
//                        sb = entrada.readLine();

                        //Borrado
                        while ((sb = entrada.readLine()) != null) {
                            String[] aux = sb.split(delimitador);// lee los caracteres en el arreglo
                            if (aux == null || aux.length != 8) {
                                MensajesErrores.advertencia("Revise que el archivo esté en formato CSV y tiene las columnas solicitadas");
                                return;
                            }
                            String codigoAsistente = aux[0];
                            String nombres = aux[1];
                            String direccion = aux[2];
                            String edad = aux[3];
                            // medico
                            String sexo = aux[4];
                            String telefono = aux[5];
                            String ministerial = aux[6];
                            String Comites = aux[7];

                            Map parametros = new HashMap();
                            parametros.put(";where", "o.codigo=:codigo");
                            parametros.put("codigo", codigoAsistente);
                            List<Entidades> listadoEntidades = ejbEntidades.encontarParametros(parametros);
                            entidad = new Entidades();
                            if (!listadoEntidades.isEmpty()) {
                                entidad = listadoEntidades.get(0);

                            } else {
                                entidad.setCodigo(codigoAsistente);
                            }

                            entidad.setDireccion(direccion);
                            entidad.setNombres(nombres);
                            entidad.setEdad(Integer.parseInt(edad));
                            entidad.setSexo(sexo);
                            entidad.setTelefono(telefono);
                            entidad.setComite(Comites);
                            entidad.setServicio(ministerial);
                            entidad.setTipo("A");
                            entidad.setFecha(new Date());
                            entidad.setActivo(Boolean.TRUE);

                            if (entidad.getId() == null) {

                                ejbEntidades.create(entidad, getSeguridadbean().getLogueado().getUserid());
                            } else {
                                ejbEntidades.edit(entidad, getSeguridadbean().getLogueado().getUserid());
                            }

                        }

                        entrada.close();

                    }
                } else {
                    MensajesErrores.fatal("Archivo no puede ser cargado: " + i.getStatus().getFacesMessage(FacesContext.getCurrentInstance(), fe, i).getSummary());
                }
            }
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(AsistentesBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(AsistentesBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            MensajesErrores.advertencia("Revise que el archivo esté en formato CSV y tiene las columnas solicitadas");
        } catch (ConsultarException ex) {
            Logger.getLogger(AsistentesBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InsertarException ex) {
            Logger.getLogger(AsistentesBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (GrabarException ex) {
            Logger.getLogger(AsistentesBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        MensajesErrores.informacion("Archivo Subido correctamente");
        formularioSubida.cancelar();
        buscar();
    }

    private Boolean buscarCodigoAsistente(String codigo) {
        Map parametros = new HashMap();
        parametros.put(";where", "o.codigo=:codigo");
        parametros.put("codigo", codigo);
        try {
            List<Entidades> listado = ejbEntidades.encontarParametros(parametros);
            if (!listado.isEmpty()) {
                return false;
            }
        } catch (ConsultarException ex) {
            Logger.getLogger(AsistentesBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    public String detectarCodificacion(FileInputStream fis) throws Throwable {
        byte[] buf;
        UniversalDetector detector;
        int nread;
        String encoding;
        try {
            buf = new byte[4096];
            detector = new UniversalDetector(null);
            while ((nread = fis.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }
            detector.dataEnd();
            encoding = detector.getDetectedCharset();
            detector.reset();
            return encoding;
        } catch (IOException e) {
            System.out.println("Error en - Clase(" + new Exception().getStackTrace()[0].getClassName() + ") "
                    + "- Método(" + new Exception().getStackTrace()[0].getMethodName() + ") \n" + e);
        }
        return "UFT-8";
    }

    /**
     * @return the entidades
     */
    public LazyDataModel<Entidades> getEntidades() {
        return entidades;
    }

    /**
     * @param entidades the entidades to set
     */
    public void setEntidades(LazyDataModel<Entidades> entidades) {
        this.entidades = entidades;
    }

    /**
     * @return the listaEntidades
     */
    public List<Entidades> getListaEntidades() {
        return listaEntidades;
    }

    /**
     * @param listaEntidades the listaEntidades to set
     */
    public void setListaEntidades(List<Entidades> listaEntidades) {
        this.listaEntidades = listaEntidades;
    }

    /**
     * @return the entidad
     */
    public Entidades getEntidad() {
        return entidad;
    }

    /**
     * @param entidad the entidad to set
     */
    public void setEntidad(Entidades entidad) {
        this.entidad = entidad;
    }

    /**
     * @return the roles
     */
    public Roles getRoles() {
        return roles;
    }

    /**
     * @param roles the roles to set
     */
    public void setRoles(Roles roles) {
        this.roles = roles;
    }

    /**
     * @return the existente
     */
    public Entidades getExistente() {
        return existente;
    }

    /**
     * @param existente the existente to set
     */
    public void setExistente(Entidades existente) {
        this.existente = existente;
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
     * @return the listaUsuarios
     */
    public List getListaUsuarios() {
        return listaUsuarios;
    }

    /**
     * @param listaUsuarios the listaUsuarios to set
     */
    public void setListaUsuarios(List listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
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
     * @return the formularioExiste
     */
    public Formulario getFormularioExiste() {
        return formularioExiste;
    }

    /**
     * @param formularioExiste the formularioExiste to set
     */
    public void setFormularioExiste(Formulario formularioExiste) {
        this.formularioExiste = formularioExiste;
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

    /**
     * @return the nombresBuscar
     */
    public String getNombresBuscar() {
        return nombresBuscar;
    }

    /**
     * @param nombresBuscar the nombresBuscar to set
     */
    public void setNombresBuscar(String nombresBuscar) {
        this.nombresBuscar = nombresBuscar;
    }

    /**
     * @return the formularioSubida
     */
    public Formulario getFormularioSubida() {
        return formularioSubida;
    }

    /**
     * @param formularioSubida the formularioSubida to set
     */
    public void setFormularioSubida(Formulario formularioSubida) {
        this.formularioSubida = formularioSubida;
    }

    /**
     * @return the delimitador
     */
    public String getDelimitador() {
        return delimitador;
    }

    /**
     * @param delimitador the delimitador to set
     */
    public void setDelimitador(String delimitador) {
        this.delimitador = delimitador;
    }

    /**
     * @return the menu
     */
    public Menusistema getMenu() {
        return menu;
    }

    /**
     * @param menu the menu to set
     */
    public void setMenu(Menusistema menu) {
        this.menu = menu;
    }
}
