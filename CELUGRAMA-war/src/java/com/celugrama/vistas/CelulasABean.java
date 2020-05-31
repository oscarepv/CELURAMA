/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.vistas;

import com.celugrama.comun.ParametrosCelugramaBean;
import com.celugrama.entidades.Asistenciacelulas;
import com.celugrama.entidades.Asistencias;
import com.celugrama.entidades.Celulas;
import com.celugrama.entidades.Codigos;
import com.celugrama.entidades.Crecimientos;
import com.celugrama.entidades.Entidades;
import com.celugrama.entidades.Perfiles;
import com.celugrama.entidades.Temas;
import com.celugrama.excepciones.BorrarException;
import com.celugrama.excepciones.ConsultarException;
import com.celugrama.excepciones.GrabarException;
import com.celugrama.excepciones.InsertarException;
import com.celugrama.seguridad.EntidadesBean;
import com.celugrama.servicios.AsistenciacelulasFacade;
import com.celugrama.servicios.AsistenciasFacade;
import com.celugrama.servicios.CelulasFacade;
import com.celugrama.servicios.CodigosFacade;
import com.celugrama.servicios.CorreoRespuestaFacade;
import com.celugrama.servicios.CrecimientosFacade;
import com.celugrama.servicios.EntidadesFacade;
import com.celugrama.servicios.TemasFacade;
import com.celugrama.utilitarios.Formulario;
import com.celugrama.utilitarios.MensajesErrores;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import javax.faces.context.FacesContext;
import javax.mail.MessagingException;
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
@ManagedBean(name = "celulasACelugrama")
@ViewScoped
public class CelulasABean {

    @ManagedProperty(value = "#{parametrosCelugrama}")
    private ParametrosCelugramaBean seguridadbean;
    @ManagedProperty(value = "#{entidadesCelugrama}")
    private EntidadesBean entidadesBean;

    private LazyDataModel<Celulas> celulas;
    private Celulas celula;
    private Formulario formulario = new Formulario();
    private Formulario formularioTemas = new Formulario();
    private Formulario formularioTema = new Formulario();
    private Formulario formularioAsistentes = new Formulario();
    private Formulario formularioSubida = new Formulario();
    private Perfiles perfil;
    private List<Temas> listaTemas;
    private Temas tema;
    private List<Entidades> asistentesTotales;
    private List<Asistenciacelulas> asitencias;
    private List<Asistenciacelulas> asitenciasaux;
    private List<Asistenciacelulas> asitenciasAuxGrabar;
    private List<Crecimientos> listaCrecimientos;
    private Codigos red;
    private String delimitador = ",";
    private int identificador = 0;
    private Boolean buscarCodigo = false;
    private String pbuscar;
    private String[] strMonths = new String[]{
        "Enero",
        "Febebro",
        "Marzo",
        "Abril",
        "Mayo",
        "Junio",
        "Julio",
        "Agosto",
        "Septiembre",
        "Octubre",
        "Noviembre",
        "Diciembre"};

    @EJB
    private CelulasFacade ejbCelulas;
    @EJB
    private TemasFacade ejbTemas;
    @EJB
    private EntidadesFacade ejbEntidades;
    @EJB
    private AsistenciacelulasFacade ejbAsistenciacelulas;
    @EJB
    private CodigosFacade ejbCodigos;
    @EJB
    private CrecimientosFacade ejbCrecimientos;
    @EJB
    private CorreoRespuestaFacade ejbCorreo;
    @EJB
    private AsistenciasFacade ejbAsistencias;

    public CelulasABean() {
        celulas = new LazyDataModel<Celulas>() {
            @Override
            public List<Celulas> load(int i, int i1, SortCriteria[] scs, Map<String, String> map) {
                //
                return null;
            }
        };

    }

    @PostConstruct
    private void activar() {

        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String perfil = (String) params.get("p");
        String redirect = (String) params.get("faces-redirect");
        if (redirect == null) {
            return;
        }
        String nombreForma = "CelulasAVista";
        if (perfil == null) {
            MensajesErrores.fatal("Sin perfil válido");
            getSeguridadbean().cerraSession();
        }
        this.setPerfil(getSeguridadbean().traerPerfil(perfil));

        if (this.getPerfil() == null) {
            return;
//            MensajesErrores.fatal("Sin perfil válido");
//            seguridadbean.cerraSession();
        }
        if (this.getPerfil().getMenu().equals("PER")) {
            return;
        }
//        if (nombreForma==null){
//            nombreForma="";
//        }
        if (nombreForma.contains(this.getPerfil().getMenu().getFormulario())) {
            if (!this.perfil.getGrupo().equals(seguridadbean.getGrpUsuario().getGrupo())) {
                MensajesErrores.fatal("Sin perfil válido, grupo invalido");
                getSeguridadbean().cerraSession();
            }
        }
    }

    public Codigos traer(Integer id) throws ConsultarException {
        return ejbCodigos.find(id);
    }

    public String buscar() {
        if (!perfil.getConsulta()) {
            MensajesErrores.advertencia("No tiene autorización para consultar");
            return null;
        }
        celulas = new LazyDataModel<Celulas>() {
            @Override
            public List<Celulas> load(int i, int pageSize, SortCriteria[] scs, Map<String, String> map) {
                Map parametros = new HashMap();
                if (scs.length == 0) {
                    parametros.put(";orden", "o.id desc");
                } else {
                    parametros.put(";orden", "o." + scs[0].getPropertyName()
                            + (scs[0].isAscending() ? " ASC" : " DESC"));
                }
                String where = "  o.activo=true ";
                for (Map.Entry e : map.entrySet()) {
                    String clave = (String) e.getKey();
                    String valor = (String) e.getValue();
                    if (clave.contains(".id")) {
                        Integer id = Integer.parseInt(valor);
                        if (id != 0) {
                            where += " and o." + clave + "=:" + clave.replaceAll("\\.", "");
                            parametros.put(clave.replaceAll("\\.", ""), id);
                        }
                    } else {
                        where += " and upper(o." + clave + ") like :" + clave.replaceAll("\\.", "");
                        parametros.put(clave.replaceAll("\\.", ""), valor.toUpperCase() + "%");
                    }
                }

                int total = 0;

                try {
//                    where += " and o.lider=:lider";
//                    parametros.put("lider", seguridadbean.getLogueado());
                    parametros.put(";where", where);
                    total = ejbCelulas.contar(parametros);
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
                celulas.setRowCount(total);
                try {
                    return ejbCelulas.encontarParametros(parametros);
                } catch (ConsultarException ex) {
                    MensajesErrores.fatal(ex.getMessage() + " : " + ex.getCause());
                    Logger.getLogger("").log(Level.SEVERE, null, ex);
                }
                return null;
            }
        };

        return null;

    }

    public String crear() {
        if (!perfil.getNuevo()) {
            MensajesErrores.advertencia("No tiene autorización para crear un registro");
            return null;
        }
        try {
            celula = new Celulas();
            asitenciasAuxGrabar = new LinkedList<>();
            listaCrecimientos = new LinkedList<>();
            int valor = 0;
            getEntidadesBean().setEntidad(null);
            getEntidadesBean().setNombresBuscar(null);
            valor = traerUltimoId();
            String codigo = generarCodigo(valor);
            Boolean parar = false;
            while (!parar) {
                Map parametros = new HashMap();
                parametros.put(";where", "o.codigo=:codigo");
                parametros.put("codigo", codigo);
                List<Celulas> listadoCelulas;

                listadoCelulas = ejbCelulas.encontarParametros(parametros);
                if (listadoCelulas.isEmpty()) {
                    parar = true;
                    break;
                }
                valor++;
                codigo = generarCodigo(valor);

            }
            celula.setCodigo(codigo);
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        asitenciasAuxGrabar = new LinkedList<>();
        listaCrecimientos = new LinkedList<>();

        formulario.insertar();
        return null;
    }

    private String generarCodigo(int valor) {
        return "CODC-" + valor;
    }

    private Integer traerUltimoId() {
        Integer valor = 0;
        try {

            Map parameter = new HashMap();
            parameter.put(";orden", "o.id desc");
            List<Celulas> listaCelula = ejbCelulas.encontarParametros(parameter);
            if (!listaCelula.isEmpty()) {
                valor = ejbCelulas.contar(parameter);
                return valor;
            }

        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return valor;
    }

    public String buscarCelula() {
        Map parametros = new HashMap();
        parametros.put(";where", "o.activo=true and o.codigo=:codigo");
        parametros.put("codigo", pbuscar);
        List<Celulas> cel;
        try {
            cel = ejbCelulas.encontarParametros(parametros);
            if (!cel.isEmpty()) {
                celula.setDireccion(cel.get(0).getDireccion());
                celula.setSector(cel.get(0).getSector());
                celula.setNombreanfitrion(cel.get(0).getNombreanfitrion());
                celula.setNombretimoteo(cel.get(0).getNombretimoteo());
                celula.setTelefono(cel.get(0).getTelefono());
                parametros = new HashMap();
                parametros.put(";where", "o.maestro.codigo='RED' and o.nombre=:red");
                parametros.put("red", cel.get(0).getRed());
                List<Codigos> c = ejbCodigos.encontarParametros(parametros);
                if (!c.isEmpty()) {
                    red = c.get(0);
                }
                parametros = new HashMap();
                parametros.put(";where", "o.celula=:celula");
                parametros.put("celula", cel.get(0));
                asitenciasAuxGrabar = ejbAsistenciacelulas.encontarParametros(parametros);
                parametros = new HashMap();
                parametros.put(";where", "o.celula=:celula");
                parametros.put("celula", cel.get(0));
                listaCrecimientos = ejbCrecimientos.encontarParametros(parametros);

            } else {
                MensajesErrores.advertencia("No se encontraron resultados");
            }
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String insertar() {
        if (!perfil.getNuevo()) {
            MensajesErrores.advertencia("No tiene autorización para crear un registro");
            return null;
        }
        if (validar()) {
            return null;
        }

        try {

            Map parametros = new HashMap();
            parametros.put(";where", "o.codigo=:codigo");
            parametros.put("codigo", celula.getCodigo());
            List<Celulas> listadoCelulas = ejbCelulas.encontarParametros(parametros);
            if (!listadoCelulas.isEmpty()) {
                Boolean parar = false;
                int valor = traerUltimoId();
                while (!parar) {
                    Map parameters = new HashMap();
                    parameters.put(";where", "o.codigo=:codigo");
                    parameters.put("codigo", celula.getCodigo());
                    List<Celulas> listadoCelulaAux = ejbCelulas.encontarParametros(parameters);

                    if (listadoCelulaAux.isEmpty()) {
                        parar = true;
                        break;
                    }
                    celula.setCodigo(generarCodigo(valor));
                    valor = valor++;

                }
//                MensajesErrores.advertencia("Codigo de celula ya existe");
//                return null;
            }
            celula.setRed(red.getParametros());
            celula.setActivo(Boolean.TRUE);
            celula.setLider(entidadesBean.getEntidad());
            ejbCelulas.create(celula, getSeguridadbean().getLogueado().getUserid());

            if (!asitenciasAuxGrabar.isEmpty()) {
                for (Asistenciacelulas a : asitenciasAuxGrabar) {
                    Asistenciacelulas asisAux = new Asistenciacelulas();
                    asisAux.setCelula(celula);
                    asisAux.setAsistente(a.getAsistente());
                    asisAux.setActivo(a.getActivo());
                    ejbAsistenciacelulas.create(asisAux, getSeguridadbean().getLogueado().getUserid());
                }
            }

            if (!listaCrecimientos.isEmpty()) {
                for (Crecimientos c : listaCrecimientos) {
                    Crecimientos cre = new Crecimientos();
                    cre.setPre(c.getPre());
                    cre.setEnc(c.getEnc());
                    cre.setPos(c.getPos());
                    cre.setNivel1(c.getNivel1());
                    cre.setNivel2(c.getNivel3());
                    cre.setNivel3(c.getNivel3());
                    cre.setCelula(celula);
                    cre.setEntidad(c.getEntidad());
                    ejbCrecimientos.create(cre, getSeguridadbean().getLogueado().getUserid());
                }
            }
            // enviarmailCreacionCelula();

        } catch (InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger("").log(Level.SEVERE, null, ex);
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String enviarmailCreacionCelula() throws MessagingException, UnsupportedEncodingException {
        //Se envia un Mail el Estado es Suspendido y previa peticion del Profesional la Rehabilitación
        String email = "oscar_e1993@hotmail.com";
        String correo = "<p><Strong>Estimado Señor/a:</Strong> " + "</p>";
        correo += "<p style= 'text-align:justify' >Reciba un cordial saludo del Cuerpo de Bomberos del DMQ y la Bienvenida al Sistema de Resistro  de Profesionales Técnicos Especializados en Actividades de Diseño, Instalación  y Mantenimiento del Sistema de Prevención y Protección Contra Incendios según  lo establece  la Ordenanza Metropolitana Nro. 470.</p>";
        correo += "<p style= 'text-align:justify' >El motivo de la presente es notificarle  que usted se encuentra en estado <strong>" + "</strong> de acuerdo a lo  establecido  en el Artículo 7.- <strong>SUSPENSIÓN DEL REGISTRO</strong> del Instructivo para el Registro de Profesionales Técnicos  Especializados  en Actividades de Diseño, Instalación  y Mantenimiento de Sistema de Prevención  y Protección  Contra Incendios publicado en el Registro Oficial  Nro. 337 del viernes 19 de septiembre de 2014.</p>";
        correo += "<p style= 'text-align:justify' >Su registro se tomará nota al margen del registro de profesionales publicado en la página web  del CB-DMQ.</p>";
        correo += "<br><br><p><Strong> Atentamente CBDMQ</Strong></p>";
        correo += "<br><br><p>Texto generado automáticamente por el Sistema de Registro de Profesionales del CBDMQ</p>";
        ejbCorreo.enviarCorreo(email, "Notificación del Registro", correo);
        buscar();
        getFormulario().cancelar();
        return null;
    }

    private boolean validar() {
        if (getEntidadesBean().getEntidad() == null) {
            MensajesErrores.advertencia("Lider es obligatorio");
            return true;
        }

        if (!getEntidadesBean().getEntidad().getTipo().equals("L")) {
            MensajesErrores.advertencia("Usuario Seleccionado no es un Lider");
            return true;
        }

        if (celula.getFecha() == null) {
            MensajesErrores.advertencia("Fecha es obligatorio");
            return true;
        }

        if (celula.getHora() == null) {
            MensajesErrores.advertencia("Hora es obligatorio");
            return true;
        }

        if ((celula.getCodigo() == null) || (celula.getCodigo().isEmpty())) {
            MensajesErrores.advertencia("Codigo es obligatorio");
            return true;
        }

        if ((celula.getDireccion() == null) || (celula.getDireccion().isEmpty())) {
            MensajesErrores.advertencia("Direccion es obligatorio");
            return true;
        }

        if ((celula.getSector() == null) || (celula.getSector().isEmpty())) {
            MensajesErrores.advertencia("Sector es obligatorio");
            return true;
        }
        if ((celula.getNombreanfitrion() == null) || (celula.getNombreanfitrion().isEmpty())) {
            MensajesErrores.advertencia("Nombre del anfitrion es obligatorio");
            return true;
        }
        if ((celula.getNombretimoteo() == null) || (celula.getNombreanfitrion().isEmpty())) {
            MensajesErrores.advertencia("Nombre del timoteo obligatorio");
            return true;
        }
        if ((celula.getTelefono() == null) || (celula.getTelefono().isEmpty())) {
            MensajesErrores.advertencia("Telefono es obligatorio");
            return true;
        }

        return false;
    }

    public String modificar() {
        if (!perfil.getModificacion()) {
            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
            return null;
        }
        celula = (Celulas) celulas.getRowData();
        entidadesBean.setEntidad(celula.getLider());
        Map parametros = new HashMap();
        parametros.put(";where", "o.maestro.codigo='RED' and o.nombre=:red");
        parametros.put("red", celula.getRed());
        List<Codigos> c;
        try {
            c = ejbCodigos.encontarParametros(parametros);
            if (!c.isEmpty()) {
                red = c.get(0);
            }
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.editar();
        return null;
    }

    public String grabar() {
        if (!perfil.getModificacion()) {
            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
            return null;
        }
        if (validar()) {
            return null;
        }
        try {
            celula.setActivo(Boolean.TRUE);
            celula.setRed(red.getParametros());
            ejbCelulas.edit(celula, getSeguridadbean().getLogueado().getUserid());

        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger("").log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String eliminar() {
        if (!perfil.getBorrado()) {
            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
            return null;
        }

        celula = (Celulas) celulas.getRowData();
        formulario.eliminar();
        return null;
    }

    public String borrar() {
        if (!perfil.getBorrado()) {
            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
            return null;
        }
        try {
            //celula.setActivo(Boolean.FALSE);
            Map parametros = new HashMap();
            parametros.put(";where", "o.celula=:celula");
            parametros.put("celula", celula);

            List<Temas> listaTemasAux = ejbTemas.encontarParametros(parametros);

            List<Crecimientos> listaCrecimientosAux = ejbCrecimientos.encontarParametros(parametros);
            for (Crecimientos cr : listaCrecimientosAux) {
                ejbCrecimientos.remove(cr, getSeguridadbean().getLogueado().getUserid());
            }

            List<Asistenciacelulas> listaAsistenciasCelulas = ejbAsistenciacelulas.encontarParametros(parametros);
            for (Asistenciacelulas ac : listaAsistenciasCelulas) {
                parametros = new HashMap();
                parametros.put(";where", "o.asistenciacelula=:asistenciacelula");
                parametros.put("asistenciacelula", ac);
                List<Asistencias> listaAsistencias = ejbAsistencias.encontarParametros(parametros);
                for (Asistencias a : listaAsistencias) {
                    ejbAsistencias.remove(a, getSeguridadbean().getLogueado().getUserid());
                }
                ejbAsistenciacelulas.remove(ac, getSeguridadbean().getLogueado().getUserid());
            }

            for (Temas t : listaTemasAux) {
                ejbTemas.remove(t, getSeguridadbean().getLogueado().getUserid());
            }
            ejbCelulas.remove(celula, getSeguridadbean().getLogueado().getUserid());

        } catch (BorrarException | ConsultarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }

        formulario.cancelar();
        buscar();
        return null;
    }

    public void buscarTemas() {

        listaTemas = new LinkedList<>();
        Map parametros = new HashMap();
        parametros.put(";where", "o.celula=:celula and o.activo=true");
        parametros.put("celula", celula);
        try {
            listaTemas = ejbTemas.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String temas() {

        celula = (Celulas) celulas.getRowData();
        tema = new Temas();
        buscarTemas();
        buscarAsistentesCelulaTotales();
        formularioTemas.insertar();
        return null;
    }

    public String nuevoTema() {

        tema = new Temas();
        int valor = traerUltimoIdTema();
        String codigo = generarCodigoT(valor);
        Boolean parar = false;
        while (!parar) {
            Map parametros = new HashMap();
            parametros.put(";where", "o.codigo=:codigo");
            parametros.put("codigo", codigo);
            List<Temas> listadoTemas;
            try {
                listadoTemas = ejbTemas.encontarParametros(parametros);
                if (listadoTemas.isEmpty()) {
                    parar = true;
                    break;
                }
            } catch (ConsultarException ex) {
                Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
            }
            valor++;
            codigo = generarCodigoT(valor);

        }
        tema.setCodigo(codigo);
        formularioTema.insertar();
        return null;
    }

    private Integer traerUltimoIdTema() {
        Integer valor = 0;
        try {

            Map parameter = new HashMap();
            parameter.put(";orden", "o.id desc");
            if (ejbCelulas.contar(parameter) > 0) {
                valor = ejbCelulas.contar(parameter);
                return valor;
            }

        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return valor;
    }

    private String generarCodigoT(int valor) {
        return "CODT-" + valor;
    }

    public String insertarTema() {
        if (!validarTema()) {
            return null;
        }
        Map parametros = new HashMap();
        parametros.put(";where", "o.codigo=:codigo");
        parametros.put("codigo", tema.getCodigo());
        try {
            List<Temas> listadoTemas = ejbTemas.encontarParametros(parametros);
            if (!listadoTemas.isEmpty()) {
                int valor = traerUltimoIdTema();
                Boolean parar = false;
                while (!parar) {
                    parametros = new HashMap();
                    parametros.put(";where", "o.codigo=:codigo");
                    parametros.put("codigo", tema.getCodigo());
                    try {
                        List<Temas> listadoTemasAux = ejbTemas.encontarParametros(parametros);
                        if (listadoTemasAux.isEmpty()) {
                            parar = true;
                            break;
                        }
                    } catch (ConsultarException ex) {
                        Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    tema.setCodigo(generarCodigoT(valor));
                    valor++;
                }
            }
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }

        tema.setCelula(celula);
        tema.setActivo(true);
        try {
            ejbTemas.create(tema, getSeguridadbean().getLogueado().getUserid());
        } catch (InsertarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        buscarTemas();
        formularioTema.cancelar();
        return null;
    }

    public boolean validarTema() {

        if (listaTemas.size() > 5) {
            MensajesErrores.advertencia("No puede agregar mas de 5 temas");
            return false;
        }
        if (tema.getCodigo() == null || tema.getCodigo().isEmpty()) {
            MensajesErrores.advertencia("Codigo es necesario");
            return false;
        }
        if (tema.getFecha() == null) {
            MensajesErrores.advertencia("Fecha es necesario");
            return false;
        }

        if (traerAnio(celula.getFecha()) != traerAnio(tema.getFecha()) || !traerMes(celula.getFecha()).equals(traerMes(tema.getFecha()))) {
            MensajesErrores.advertencia("Mes del tema no coincide con el mes de la celula");
            return false;
        }

        if (tema.getTemas() == null || tema.getTemas().isEmpty()) {
            MensajesErrores.advertencia("Tema es necesario");
            return false;
        }

        return true;
    }

    public String modificarTema() {
        tema = listaTemas.get(formularioTema.getFila().getRowIndex());
        formularioTema.editar();
        return null;
    }

    public String grabarTema() {
        if (!validarTema()) {
            return null;
        }
        try {
            ejbTemas.edit(tema, getSeguridadbean().getLogueado().getUserid());
        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(Celulas.class.getName()).log(Level.SEVERE, null, ex);
        }
        buscarTemas();
        formularioTema.cancelar();
        return null;
    }

    public String eliminarTema() {
        tema = listaTemas.get(formularioTema.getFila().getRowIndex());
        formularioTema.eliminar();
        return null;
    }

    public String borrarTema() {
        try {
            //tema.setActivo(false);
            Map parametros = new HashMap();
            parametros.put(";where", "o.tema=:tema");
            parametros.put("tema", tema);
            List<Asistencias> listaAsistencias = ejbAsistencias.encontarParametros(parametros);
            for (Asistencias a : listaAsistencias) {
                ejbAsistencias.remove(a, getSeguridadbean().getLogueado().getUserid());
            }
            ejbTemas.remove(tema, getSeguridadbean().getLogueado().getUserid());
        } catch (BorrarException | ConsultarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        buscarTemas();
        formularioTema.cancelar();

        return null;
    }

//    public String asistentesCelula() {
//        celula = (Celulas) celulas.getRowData();
//        buscarAsistentesCelulaTotales();
//        formularioTemas.insertar();
//
//        return null;
//    }
    private Asistenciacelulas buscarAsistentesCelulaTotales() {
        Map parametros = new HashMap();
        parametros.put(";where", "o.celula=:celula and o.activo=true and o.asistente.activo=true");
        parametros.put("celula", celula);
        try {
            asitencias = ejbAsistenciacelulas.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private Asistenciacelulas buscarAsistentesCelula(Entidades e) {
        Map parametros = new HashMap();
        parametros.put(";where", "o.celula=:celula and o.asistente=:asistente ");
        parametros.put("celula", celula);
        parametros.put("asistente", e);
        try {
            List<Asistenciacelulas> ac = ejbAsistenciacelulas.encontarParametros(parametros);
            if (!ac.isEmpty()) {
                return ac.get(0);
            }
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void buscarListaEntidades() {
        Map parametros = new HashMap();
        parametros.put(";where", "o.activo=true and o.tipo='A'");
        parametros.put(";orden", "o.nombres asc");
        try {
            asistentesTotales = ejbEntidades.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String nuevoAsistente() {
        buscarListaEntidades();
        asitenciasaux = new LinkedList<>();
        for (Entidades a : asistentesTotales) {
            Asistenciacelulas ac = new Asistenciacelulas();
            Asistenciacelulas aux;
            aux = buscarAsistentesCelula(a);
            if (aux != null) {
                ac = aux;
            } else {
                ac.setAsistente(a);
                ac.setCelula(celula);
            }
            asitenciasaux.add(ac);

        }
        formularioAsistentes.editar();
        return null;
    }

    public String grabarAsistentes() {
        for (Asistenciacelulas a : asitenciasaux) {
            try {
                if (a.getId() == null) {
                    ejbAsistenciacelulas.create(a, getSeguridadbean().getLogueado().getUserid());
                } else {
                    ejbAsistenciacelulas.edit(a, getSeguridadbean().getLogueado().getUserid());
                }

            } catch (InsertarException | GrabarException ex) {
                Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        buscarAsistentesCelulaTotales();
        formularioAsistentes.cancelar();
        return null;
    }

    public String cargarDatos() {
        //Celula
        identificador = 1;
        formularioSubida.insertar();
        return null;
    }

    public String cargarDatosTemas() {
        //Temas
        identificador = 2;
        formularioSubida.insertar();
        return null;
    }

    public String cargarDatosAsistentes() {
        //Asistentes
        identificador = 3;
        formularioSubida.insertar();
        return null;
    }

    public String cargarDatosFechas() {
        identificador = 4;
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
                            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        entrada = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset.equals("WINDOWS-1252") ? "ISO-8859-1" : charset));
                        String sb;
                        // linea de cabeceras
//                        sb = entrada.readLine();

                        //Borrado
                        int linea = 1;
                        while ((sb = entrada.readLine()) != null) {
                            String[] aux = sb.split(delimitador);// lee los caracteres en el arreglo
                            if (aux == null || aux.length != 11) {
                                MensajesErrores.advertencia("Revise que el archivo esté en formato CSV y tiene las columnas solicitadas. Error linea: " + linea);
                                return;
                            }
                            String codigoCelula = aux[0];
                            String codigoLider = aux[1];
                            String direccion = aux[2];
                            String telefono = aux[3];
                            String barrio = aux[4];
                            String nombreAnfitrion = aux[5];
                            String nombreTimoteo = aux[6];
                            String Red = aux[7];
                            String promedioA = aux[8];
                            String invitadosC = aux[9];
                            String celulasNR = aux[10];

                            celula = new Celulas();
                            Map parametros = new HashMap();
                            parametros.put(";where", "o.codigo=:codigo");
                            parametros.put("codigo", codigoLider);
                            List<Entidades> listadoEntidades = ejbEntidades.encontarParametros(parametros);
                            if (!listadoEntidades.isEmpty()) {
                                celula.setLider(listadoEntidades.get(0));
                                celula.setCodigo(codigoCelula);
                                celula.setDireccion(direccion);
                                celula.setTelefono(telefono);
                                celula.setSector(barrio);
                                celula.setNombreanfitrion(nombreAnfitrion);
                                celula.setNombretimoteo(nombreTimoteo);
                                celula.setRed(Red);
                                celula.setPromedioa(Integer.parseInt(promedioA));
                                celula.setInvitados(Integer.parseInt(invitadosC));
                                celula.setCelulasn(Integer.parseInt(celulasNR));
                                celula.setActivo(Boolean.TRUE);
                                ejbCelulas.create(celula, getSeguridadbean().getLogueado().getUserid());

                            } else {
                                MensajesErrores.advertencia("Lider no encontrado. Linea: " + linea);
                            }
                            linea++;
                        }

                        entrada.close();

                    }
                } else {
                    MensajesErrores.fatal("Archivo no puede ser cargado: " + i.getStatus().getFacesMessage(FacesContext.getCurrentInstance(), fe, i).getSummary());
                }
            }
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            MensajesErrores.advertencia("Revise que el archivo esté en formato CSV y tiene las columnas solicitadas");
        } catch (ConsultarException | InsertarException ex) {
            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        MensajesErrores.informacion("Archivo Subido correctamente");
        formularioSubida.cancelar();
        buscar();
    }

    public void archivoListenerTemas(FileEntryEvent e) {
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
                            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        entrada = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset.equals("WINDOWS-1252") ? "ISO-8859-1" : charset));
                        String sb;
                        // linea de cabeceras
//                        sb = entrada.readLine();

                        //Borrado
                        int linea = 1;
                        SimpleDateFormat sdfFecha = new SimpleDateFormat("MM/dd/yyyy");
                        while ((sb = entrada.readLine()) != null) {
                            String[] aux = sb.split(delimitador);// lee los caracteres en el arreglo
                            if (aux == null || aux.length != 5) {
                                MensajesErrores.advertencia("Revise que el archivo esté en formato CSV y tiene las columnas solicitadas. Error linea: " + linea);
                                return;
                            }
                            String codigoTema = aux[0];
                            String codigoCelula = aux[1];
                            String temas = aux[2];
                            String observacion = aux[3];
                            String fecha = aux[4];

                            tema = new Temas();
                            Map parametros = new HashMap();
                            parametros.put(";where", "o.codigo=:codigo");
                            parametros.put("codigo", codigoCelula);
                            List<Celulas> cel = ejbCelulas.encontarParametros(parametros);
                            if (!cel.isEmpty()) {
                                tema.setCelula(cel.get(0));
                                tema.setCodigo(codigoTema);
                                tema.setTemas(temas);
                                tema.setObservacion(observacion);
                                tema.setFecha(sdfFecha.parse(fecha));
                                tema.setActivo(Boolean.TRUE);
                                ejbTemas.create(tema, getSeguridadbean().getLogueado().getUserid());

                            } else {
                                MensajesErrores.advertencia("Celula no encontrada. Linea: " + linea);
                            }
                            linea++;
                        }

                        entrada.close();

                    }
                } else {
                    MensajesErrores.fatal("Archivo no puede ser cargado: " + i.getStatus().getFacesMessage(FacesContext.getCurrentInstance(), fe, i).getSummary());
                }
            }
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            MensajesErrores.advertencia("Revise que el archivo esté en formato CSV y tiene las columnas solicitadas");
        } catch (ConsultarException | InsertarException ex) {
            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        MensajesErrores.informacion("Archivo Subido correctamente");
        formularioSubida.cancelar();
    }

    public void archivoListenerFechas(FileEntryEvent e) {
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
                            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        entrada = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset.equals("WINDOWS-1252") ? "ISO-8859-1" : charset));
                        String sb;
                        // linea de cabeceras
//                        sb = entrada.readLine();

                        //Borrado
                        int linea = 1;
                        SimpleDateFormat sdfFecha = new SimpleDateFormat("MM/dd/yyyy");
                        SimpleDateFormat sdfhora = new SimpleDateFormat("HH:mm");
                        while ((sb = entrada.readLine()) != null) {
                            String[] aux = sb.split(delimitador);// lee los caracteres en el arreglo
                            if (aux == null || aux.length != 3) {
                                MensajesErrores.advertencia("Revise que el archivo esté en formato CSV y tiene las columnas solicitadas. Error linea: " + linea);
                                return;
                            }
                            String codigoCelula = aux[0];
                            String fecha = aux[1];
                            String hora = aux[2];
                            Map parametros = new HashMap();
                            parametros.put(";where", "o.codigo=:codigo");
                            parametros.put("codigo", codigoCelula);
                            List<Celulas> cel = ejbCelulas.encontarParametros(parametros);
                            if (!cel.isEmpty()) {

                                cel.get(0).setFecha(sdfFecha.parse(fecha));
                                cel.get(0).setHora(sdfhora.parse(hora));
                                ejbCelulas.edit(cel.get(0), getSeguridadbean().getLogueado().getUserid());

                            } else {
                                MensajesErrores.advertencia("Celula no encontrada. Linea: " + linea);
                            }
                            linea++;
                        }

                        entrada.close();

                    }
                } else {
                    MensajesErrores.fatal("Archivo no puede ser cargado: " + i.getStatus().getFacesMessage(FacesContext.getCurrentInstance(), fe, i).getSummary());
                }
            }
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            MensajesErrores.advertencia("Revise que el archivo esté en formato CSV y tiene las columnas solicitadas");
        } catch (ConsultarException ex) {
            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException | GrabarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        MensajesErrores.informacion("Archivo Subido correctamente");
        formularioSubida.cancelar();
    }

    public void archivoListenerAsistentes(FileEntryEvent e) {
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
                            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
                        }

                        entrada = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset.equals("WINDOWS-1252") ? "ISO-8859-1" : charset));
                        String sb;
                        // linea de cabeceras
//                        sb = entrada.readLine();

                        //Borrado
                        int linea = 1;
                        SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy");
                        while ((sb = entrada.readLine()) != null) {
                            String[] aux = sb.split(delimitador);// lee los caracteres en el arreglo
                            if (aux == null || aux.length != 5) {
                                MensajesErrores.advertencia("Revise que el archivo esté en formato CSV y tiene las columnas solicitadas. Error linea: " + linea);
                                return;
                            }
                            String codigoTema = aux[0];
                            String codigoCelula = aux[1];
                            String temas = aux[2];
                            String observacion = aux[3];
                            String fecha = aux[4];

                            tema = new Temas();
                            Map parametros = new HashMap();
                            parametros.put(";where", "o.codigo=:codigo");
                            parametros.put("codigo", codigoCelula);
                            List<Celulas> cel = ejbCelulas.encontarParametros(parametros);
                            if (!cel.isEmpty()) {
                                tema.setCelula(cel.get(0));
                                tema.setCodigo(codigoTema);
                                tema.setTemas(temas);
                                tema.setObservacion(observacion);
                                tema.setFecha(sdfFecha.parse(fecha));
                                tema.setActivo(Boolean.TRUE);
                                ejbTemas.create(tema, getSeguridadbean().getLogueado().getUserid());

                            } else {
                                MensajesErrores.advertencia("Celula no encontrada. Linea: " + linea);
                            }
                            linea++;
                        }

                        entrada.close();

                    }
                } else {
                    MensajesErrores.fatal("Archivo no puede ser cargado: " + i.getStatus().getFacesMessage(FacesContext.getCurrentInstance(), fe, i).getSummary());
                }
            }
        } catch (UnsupportedEncodingException | FileNotFoundException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            MensajesErrores.advertencia("Revise que el archivo esté en formato CSV y tiene las columnas solicitadas");
        } catch (ConsultarException | InsertarException ex) {
            Logger.getLogger(LideresBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        MensajesErrores.informacion("Archivo Subido correctamente");
        formularioSubida.cancelar();
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

    public String traerMes(Date fecha) {
        if (fecha != null) {
            Calendar act = Calendar.getInstance();
            act.setTime(fecha);
            return strMonths[act.get(Calendar.MONTH)];
        }

        return null;
    }

    public int traerAnio(Date fecha) {
        if (fecha != null) {
            Calendar act = Calendar.getInstance();
            act.setTime(fecha);
            return act.get(Calendar.YEAR);
        }

        return 0;
    }

    public String getTextoFecha(Date f) {
        int nrodiasemana;
        String dia;
        String mes;
        String anio;
        String nromes;
        String nrodia;
        String retorno;
        Date referencia = f;
        //trae el numero de día de la semana . (Domingo = 1, Lunes = 2)
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(referencia);
        nrodiasemana = cal.get(Calendar.DAY_OF_WEEK);

        anio = new SimpleDateFormat("yyyy").format(referencia);
        nrodia = new SimpleDateFormat("dd").format(referencia);
        nromes = new SimpleDateFormat("MM").format(referencia);
        mes = traermes(Integer.parseInt(nromes));
        dia = traerdia(nrodiasemana);
        // Ejemplo Formato de respuesta "Viernes , 08 Diciembre 2017"
        retorno = dia + " , " + nrodia + " " + mes + " " + anio;
        return retorno;
    }

    public String traermes(int mes) {
        switch (mes) {
            case 1:
                return "Enero";
            case 2:
                return "Febrero";
            case 3:
                return "Marzo";
            case 4:
                return "Abril";
            case 5:
                return "Mayo";
            case 6:
                return "Junio";
            case 7:
                return "Julio";
            case 8:
                return "Agosto";
            case 9:
                return "Septiembre";
            case 10:
                return "Octubre";
            case 11:
                return "Noviembre";
            case 12:
                return "Diciembre";
            default:
                return " ";
        }
    }

    public String traerdia(int dia) {
        switch (dia) {
            case 1:
                return "Domingo";
            case 2:
                return "Lunes";
            case 3:
                return "Martes";
            case 4:
                return "Miércoles";
            case 5:
                return "Jueves";
            case 6:
                return "Viernes";
            case 7:
                return "Sábado";
            default:
                return " ";
        }
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
     * @return the celulas
     */
    public LazyDataModel<Celulas> getCelulas() {
        return celulas;
    }

    /**
     * @param celulas the celulas to set
     */
    public void setCelulas(LazyDataModel<Celulas> celulas) {
        this.celulas = celulas;
    }

    /**
     * @return the celula
     */
    public Celulas getCelula() {
        return celula;
    }

    /**
     * @param celula the celula to set
     */
    public void setCelula(Celulas celula) {
        this.celula = celula;
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
     * @return the formularioTemas
     */
    public Formulario getFormularioTemas() {
        return formularioTemas;
    }

    /**
     * @param formularioTemas the formularioTemas to set
     */
    public void setFormularioTemas(Formulario formularioTemas) {
        this.formularioTemas = formularioTemas;
    }

    /**
     * @return the listaTemas
     */
    public List<Temas> getListaTemas() {
        return listaTemas;
    }

    /**
     * @param listaTemas the listaTemas to set
     */
    public void setListaTemas(List<Temas> listaTemas) {
        this.listaTemas = listaTemas;
    }

    /**
     * @return the formularioTema
     */
    public Formulario getFormularioTema() {
        return formularioTema;
    }

    /**
     * @param formularioTema the formularioTema to set
     */
    public void setFormularioTema(Formulario formularioTema) {
        this.formularioTema = formularioTema;
    }

    /**
     * @return the tema
     */
    public Temas getTema() {
        return tema;
    }

    /**
     * @param tema the tema to set
     */
    public void setTema(Temas tema) {
        this.tema = tema;
    }

    /**
     * @return the asistentesTotales
     */
    public List<Entidades> getAsistentesTotales() {
        return asistentesTotales;
    }

    /**
     * @param asistentesTotales the asistentesTotales to set
     */
    public void setAsistentesTotales(List<Entidades> asistentesTotales) {
        this.asistentesTotales = asistentesTotales;
    }

    /**
     * @return the asitencias
     */
    public List<Asistenciacelulas> getAsitencias() {
        return asitencias;
    }

    /**
     * @param asitencias the asitencias to set
     */
    public void setAsitencias(List<Asistenciacelulas> asitencias) {
        this.asitencias = asitencias;
    }

    /**
     * @return the asitenciasaux
     */
    public List<Asistenciacelulas> getAsitenciasaux() {
        return asitenciasaux;
    }

    /**
     * @param asitenciasaux the asitenciasaux to set
     */
    public void setAsitenciasaux(List<Asistenciacelulas> asitenciasaux) {
        this.asitenciasaux = asitenciasaux;
    }

    /**
     * @return the formularioAsistentes
     */
    public Formulario getFormularioAsistentes() {
        return formularioAsistentes;
    }

    /**
     * @param formularioAsistentes the formularioAsistentes to set
     */
    public void setFormularioAsistentes(Formulario formularioAsistentes) {
        this.formularioAsistentes = formularioAsistentes;
    }

    /**
     * @return the red
     */
    public Codigos getRed() {
        return red;
    }

    /**
     * @param red the red to set
     */
    public void setRed(Codigos red) {
        this.red = red;
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
     * @return the identificador
     */
    public int getIdentificador() {
        return identificador;
    }

    /**
     * @param identificador the identificador to set
     */
    public void setIdentificador(int identificador) {
        this.identificador = identificador;
    }

    /**
     * @return the pbuscar
     */
    public String getPbuscar() {
        return pbuscar;
    }

    /**
     * @param pbuscar the pbuscar to set
     */
    public void setPbuscar(String pbuscar) {
        this.pbuscar = pbuscar;
    }

    /**
     * @return the strMonths
     */
    public String[] getStrMonths() {
        return strMonths;
    }

    /**
     * @param strMonths the strMonths to set
     */
    public void setStrMonths(String[] strMonths) {
        this.strMonths = strMonths;
    }

    /**
     * @return the buscarCodigo
     */
    public Boolean getBuscarCodigo() {
        return buscarCodigo;
    }

    /**
     * @param buscarCodigo the buscarCodigo to set
     */
    public void setBuscarCodigo(Boolean buscarCodigo) {
        this.buscarCodigo = buscarCodigo;
    }

    /**
     * @return the asitenciasAuxGrabar
     */
    public List<Asistenciacelulas> getAsitenciasAuxGrabar() {
        return asitenciasAuxGrabar;
    }

    /**
     * @param asitenciasAuxGrabar the asitenciasAuxGrabar to set
     */
    public void setAsitenciasAuxGrabar(List<Asistenciacelulas> asitenciasAuxGrabar) {
        this.asitenciasAuxGrabar = asitenciasAuxGrabar;
    }

    /**
     * @return the listaCrecimientos
     */
    public List<Crecimientos> getListaCrecimientos() {
        return listaCrecimientos;
    }

    /**
     * @param listaCrecimientos the listaCrecimientos to set
     */
    public void setListaCrecimientos(List<Crecimientos> listaCrecimientos) {
        this.listaCrecimientos = listaCrecimientos;
    }

    /**
     * @return the entidadesBean
     */
    public EntidadesBean getEntidadesBean() {
        return entidadesBean;
    }

    /**
     * @param entidadesBean the entidadesBean to set
     */
    public void setEntidadesBean(EntidadesBean entidadesBean) {
        this.entidadesBean = entidadesBean;
    }
}
