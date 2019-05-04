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
import com.celugrama.entidades.Crecimientos;
import com.celugrama.entidades.Entidades;
import com.celugrama.entidades.Perfiles;
import com.celugrama.entidades.Temas;
import com.celugrama.excepciones.ConsultarException;
import com.celugrama.excepciones.GrabarException;
import com.celugrama.excepciones.InsertarException;
import com.celugrama.servicios.AsistenciacelulasFacade;
import com.celugrama.servicios.AsistenciasFacade;
import com.celugrama.servicios.CelulasFacade;
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
@ManagedBean(name = "principalACelugrama")
@ViewScoped
public class PrincipalAsistenciaABean {

    @ManagedProperty(value = "#{parametrosCelugrama}")
    private ParametrosCelugramaBean seguridadbean;

    private LazyDataModel<Celulas> celulas;
    private Celulas celula;
    private Formulario formulario = new Formulario();
    private Formulario formularioAsistentes = new Formulario();
    private Formulario formularioSubida = new Formulario();
    private Perfiles perfil;
    private List<Temas> listaTemas;
    private List<Asistenciacelulas> asitencias;
    private List<Asistencias> asistenciasaux;
    private Asistenciacelulas asistencia;
    private Crecimientos crecimiento;
    private Entidades entidad;
    private String delimitador = ",";

    @EJB
    private AsistenciasFacade ejbAsistencias;
    @EJB
    private CelulasFacade ejbCelulas;
    @EJB
    private AsistenciacelulasFacade ejbAsistenciacelulas;
    @EJB
    private TemasFacade ejbTemas;
    @EJB
    private CrecimientosFacade ejbCrecimientos;
    @EJB
    private EntidadesFacade ejbEntidades;

    public PrincipalAsistenciaABean() {
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
        String nombreForma = "PrincipalVista";
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

    public String ver() {
        if (!perfil.getConsulta()) {
            MensajesErrores.advertencia("No tiene autorización para consultar");
            return null;
        }
        celula = (Celulas) celulas.getRowData();
        buscarTemas();
        buscarAsistentesCelulaTotales();
        formulario.editar();
        return null;
    }

    private void buscarTemas() {
        listaTemas = new LinkedList<>();
        Map parametros = new HashMap();
        parametros.put(";where", "o.celula=:celula");
        parametros.put("celula", celula);
        try {
            listaTemas = ejbTemas.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Asistenciacelulas buscarAsistentesCelulaTotales() {
        Map parametros = new HashMap();
        parametros.put(";where", "o.celula=:celula and o.activo=true and o.asistente.activo=true");
        parametros.put("celula", celula);
        try {
            asitencias = ejbAsistenciacelulas.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private Asistencias buscarAsistentcia(Asistenciacelulas ac, Temas t) {
        Map parametros = new HashMap();
        parametros.put(";where", "o.asistenciacelula=:ac and o.tema=:tema ");
        parametros.put("ac", ac);
        parametros.put("tema", t);
        try {
            List<Asistencias> a = ejbAsistencias.encontarParametros(parametros);
            if (!a.isEmpty()) {
                return a.get(0);
            }
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String asistencia() {
        setAsistencia(asitencias.get(formulario.getFila().getRowIndex()));
        asistenciasaux = new LinkedList<>();
        Map parametros = new HashMap();
        parametros.put(";where", "o.celula=:c and o.entidad=:e");
        parametros.put("c", getAsistencia().getCelula());
        parametros.put("e", getAsistencia().getAsistente());
        crecimiento = new Crecimientos();
        try {
            List<Crecimientos> c = ejbCrecimientos.encontarParametros(parametros);
            if (!c.isEmpty()) {
                crecimiento = c.get(0);
            }
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (Temas t : listaTemas) {
            Asistencias a = new Asistencias();
            Asistencias as = buscarAsistentcia(getAsistencia(), t);
            if (as != null) {
                a = as;
            } else {
                a.setAsistenciacelula(getAsistencia());
                a.setTema(t);
            }
            asistenciasaux.add(a);
        }
        formularioAsistentes.insertar();
        return null;
    }

    public String grabarAsistencia() {
        try {
            for (Asistencias a : asistenciasaux) {

                if (a.getId() == null) {
                    ejbAsistencias.create(a, getSeguridadbean().getLogueado().getUserid());
                } else {
                    ejbAsistencias.edit(a, getSeguridadbean().getLogueado().getUserid());
                }

            }
            if (crecimiento.getId() == null) {
                crecimiento.setEntidad(getAsistencia().getAsistente());
                crecimiento.setCelula(getAsistencia().getCelula());
                ejbCrecimientos.create(crecimiento, getSeguridadbean().getLogueado().getUserid());
            } else {
                ejbCrecimientos.edit(crecimiento, getSeguridadbean().getLogueado().getUserid());
            }
        } catch (InsertarException | GrabarException ex) {
            Logger.getLogger(PrincipalAsistenciaABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formularioAsistentes.cancelar();

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
                            if (aux == null || aux.length != 19) {
                                MensajesErrores.advertencia("Revise que el archivo esté en formato CSV y tiene las columnas solicitadas. Error linea: " + linea);
                                return;
                            }
                            String codigoAsistente = aux[0];
                            String codigoCelula = aux[1];
                            String codigoAsitentecia = aux[2];
                            String s1 = aux[3];
                            String s2 = aux[4];
                            String s3 = aux[5];
                            String s4 = aux[6];
                            String s5 = aux[7];
                            String l1 = aux[8];
                            String l2 = aux[9];
                            String l3 = aux[10];
                            String l4 = aux[11];
                            String l5 = aux[12];
                            String pre = aux[13];
                            String enc = aux[14];
                            String pos = aux[15];
                            String n1 = aux[16];
                            String n2 = aux[17];
                            String n3 = aux[18];
                            Map parametros = new HashMap();
                            parametros.put(";where", "o.codigo=:codigo");
                            parametros.put("codigo", codigoAsistente);
                            List<Entidades> listadoEntidades = ejbEntidades.encontarParametros(parametros);
                            entidad = new Entidades();
                            celula = new Celulas();
                            asistencia = new Asistenciacelulas();
                            if (!listadoEntidades.isEmpty()) {
                                entidad = listadoEntidades.get(0);
                            }
                            parametros = new HashMap();
                            parametros.put(";where", "o.codigo=:codigo");
                            parametros.put("codigo", codigoCelula);
                            List<Celulas> listadoCelula = ejbCelulas.encontarParametros(parametros);

                            if (!listadoEntidades.isEmpty()) {
                                celula = listadoCelula.get(0);
                            }

                            if (entidad == null || celula == null) {
                                MensajesErrores.advertencia("Error al crear asistencia. Linea: " + linea);
                            } else {
                                parametros = new HashMap();
                                parametros.put(";where", "o.celula=:celula and o.asistente=:a");
                                parametros.put("celula", celula);
                                parametros.put("a", entidad);
                                List<Asistenciacelulas> listadoAsistenciasC = ejbAsistenciacelulas.encontarParametros(parametros);
                                if (!listadoAsistenciasC.isEmpty()) {                                   
                                    asistencia = listadoAsistenciasC.get(0);
                                    asistencia.setActivo(Boolean.TRUE);
                                    ejbAsistenciacelulas.edit(asistencia, getSeguridadbean().getLogueado().getUserid());
                                } else {
                                    asistencia.setActivo(Boolean.TRUE);
                                    asistencia.setAsistente(entidad);
                                    asistencia.setCelula(celula);
                                    ejbAsistenciacelulas.create(asistencia, getSeguridadbean().getLogueado().getUserid());
                                }

                                parametros = new HashMap();
                                parametros.put(";where", "o.celula=:celula");
                                parametros.put("celula", celula);
                                parametros.put(";orden", "o.fecha asc");
                                List<Temas> listadoTemas = ejbTemas.encontarParametros(parametros);
                                int indice = 1;
                                for (Temas t : listadoTemas) {
                                    Asistencias a = new Asistencias();
                                    parametros = new HashMap();
                                    parametros.put(";where", "o.tema=:t and o.asistenciacelula=:asis");
                                    parametros.put("t", t);
                                    parametros.put("asis", asistencia);
                                    List<Asistencias> listadoAsistencias = ejbAsistencias.encontarParametros(parametros);
                                    if (!listadoAsistencias.isEmpty()) {
                                        a = listadoAsistencias.get(0);
                                    }
                                    switch (indice) {
                                        case 1:
                                            a.setAsistencia(traerValor(s1));
                                            a.setLectura(traerValor(l1));
                                            a.setActivo(Boolean.TRUE);
                                            a.setAsistenciacelula(asistencia);
                                            a.setTema(t);
                                            if (a.getId() == null) {
                                                ejbAsistencias.create(a, getSeguridadbean().getLogueado().getUserid());
                                            } else {
                                                ejbAsistencias.edit(a, getSeguridadbean().getLogueado().getUserid());
                                            }
                                            break;
                                        case 2:
                                            a.setAsistencia(traerValor(s2));
                                            a.setLectura(traerValor(l2));
                                            a.setActivo(Boolean.TRUE);
                                            a.setAsistenciacelula(asistencia);
                                            a.setTema(t);
                                            if (a.getId() == null) {
                                                ejbAsistencias.create(a, getSeguridadbean().getLogueado().getUserid());
                                            } else {
                                                ejbAsistencias.edit(a, getSeguridadbean().getLogueado().getUserid());
                                            }
                                            break;
                                        case 3:
                                            a.setAsistencia(traerValor(s3));
                                            a.setLectura(traerValor(l3));
                                            a.setActivo(Boolean.TRUE);
                                            a.setAsistenciacelula(asistencia);
                                            a.setTema(t);
                                            if (a.getId() == null) {
                                                ejbAsistencias.create(a, getSeguridadbean().getLogueado().getUserid());
                                            } else {
                                                ejbAsistencias.edit(a, getSeguridadbean().getLogueado().getUserid());
                                            }
                                            break;
                                        case 4:
                                            a.setAsistencia(traerValor(s4));
                                            a.setLectura(traerValor(l4));
                                            a.setActivo(Boolean.TRUE);
                                            a.setAsistenciacelula(asistencia);
                                            a.setTema(t);
                                            if (a.getId() == null) {
                                                ejbAsistencias.create(a, getSeguridadbean().getLogueado().getUserid());
                                            } else {
                                                ejbAsistencias.edit(a, getSeguridadbean().getLogueado().getUserid());
                                            }
                                            break;
                                        case 5:
                                            a.setAsistencia(traerValor(s5));
                                            a.setLectura(traerValor(l5));
                                            a.setActivo(Boolean.TRUE);
                                            a.setAsistenciacelula(asistencia);
                                            a.setTema(t);
                                            if (a.getId() == null) {
                                                ejbAsistencias.create(a, getSeguridadbean().getLogueado().getUserid());
                                            } else {
                                                ejbAsistencias.edit(a, getSeguridadbean().getLogueado().getUserid());
                                            }
                                            break;
                                    }
                                    indice++;
                                }
                                crecimiento = new Crecimientos();
                                parametros = new HashMap();
                                parametros.put(";where", "o.celula=:celula and o.entidad=:entidad");
                                parametros.put("celula", celula);
                                parametros.put("entidad", entidad);
                                List<Crecimientos> listadoCrecimientos = ejbCrecimientos.encontarParametros(parametros);
                                if (!listadoCrecimientos.isEmpty()) {
                                    crecimiento = listadoCrecimientos.get(0);
                                    crecimiento.setPos(traerValor(pos));
                                    crecimiento.setPre(traerValor(pre));
                                    crecimiento.setEnc(traerValor(enc));
                                    crecimiento.setNivel1(traerValor(n1));
                                    crecimiento.setNivel2(traerValor(n2));
                                    crecimiento.setNivel3(traerValor(n3));
                                    crecimiento.setEntidad(entidad);
                                    crecimiento.setCelula(celula);
                                    ejbCrecimientos.edit(crecimiento, getSeguridadbean().getLogueado().getUserid());
                                } else {
                                    crecimiento.setPos(traerValor(pos));
                                    crecimiento.setPre(traerValor(pre));
                                    crecimiento.setEnc(traerValor(enc));
                                    crecimiento.setNivel1(traerValor(n1));
                                    crecimiento.setNivel2(traerValor(n2));
                                    crecimiento.setNivel3(traerValor(n3));
                                    crecimiento.setEntidad(entidad);
                                    crecimiento.setCelula(celula);
                                    ejbCrecimientos.create(crecimiento, getSeguridadbean().getLogueado().getUserid());
                                }

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
        } catch (ConsultarException | InsertarException | GrabarException ex) {
            Logger.getLogger(PrincipalAsistenciaABean.class.getName()).log(Level.SEVERE, null, ex);
        }
        MensajesErrores.informacion("Archivo Subido correctamente");
        formularioSubida.cancelar();
        buscar();
    }

    private Boolean traerValor(String valor) {
        if (valor.equals("SI")) {
            return true;
        }
        return false;
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
     * @return the asistenciasaux
     */
    public List<Asistencias> getAsistenciasaux() {
        return asistenciasaux;
    }

    /**
     * @param asistenciasaux the asistenciasaux to set
     */
    public void setAsistenciasaux(List<Asistencias> asistenciasaux) {
        this.asistenciasaux = asistenciasaux;
    }

    /**
     * @return the asistencia
     */
    public Asistenciacelulas getAsistencia() {
        return asistencia;
    }

    /**
     * @param asistencia the asistencia to set
     */
    public void setAsistencia(Asistenciacelulas asistencia) {
        this.asistencia = asistencia;
    }

    /**
     * @return the crecimiento
     */
    public Crecimientos getCrecimiento() {
        return crecimiento;
    }

    /**
     * @param crecimiento the crecimiento to set
     */
    public void setCrecimiento(Crecimientos crecimiento) {
        this.crecimiento = crecimiento;
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

}
