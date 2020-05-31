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
import com.celugrama.servicios.AsistenciacelulasFacade;
import com.celugrama.servicios.AsistenciasFacade;
import com.celugrama.servicios.CelulasFacade;
import com.celugrama.servicios.CrecimientosFacade;
import com.celugrama.servicios.TemasFacade;
import com.celugrama.utilitarios.AuxiliarReporte;
import com.celugrama.utilitarios.DocumentoPDF;
import com.celugrama.utilitarios.Formulario;
import com.celugrama.utilitarios.MensajesErrores;
import com.lowagie.text.DocumentException;
import java.io.IOException;
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
import javax.faces.application.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.icefaces.ace.model.table.LazyDataModel;
import org.icefaces.ace.model.table.SortCriteria;

/**
 *
 * @author oscar
 */
@ManagedBean(name = "reporteACelugrama")
@ViewScoped
public class ReporteCelulaABean {

    @ManagedProperty(value = "#{parametrosCelugrama}")
    private ParametrosCelugramaBean seguridadbean;

    private LazyDataModel<Celulas> celulas;
    private Celulas celula;
    private Formulario formulario = new Formulario();
    private Formulario formularioAsistentes = new Formulario();
    private Perfiles perfil;
    private List<Temas> listaTemas;
    private List<Asistenciacelulas> asitencias;
    private List<Asistencias> asistenciasaux;
    private Asistenciacelulas asistencia;
    private Crecimientos crecimiento;
    private Resource archivoPlanilla;
    private Resource archivoPlanillaBlanco;
    private int t1, t2, t3, t4, t5;

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

    public ReporteCelulaABean() {
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
        String nombreForma = "ReporteCelulaAVista";
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
        if (!perfil.getModificacion()) {
            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
            return null;
        }
        t1 = 0;
        t2 = 0;
        t3 = 0;
        t4 = 0;
        t5 = 0;
        celula = (Celulas) celulas.getRowData();
        archivoPlanilla = null;
        archivoPlanillaBlanco = null;
        buscarTemas();
        buscarAsistentesCelulaTotales();
        formulario.editar();
        return null;
    }

    private void buscarTemas() {
        listaTemas = new LinkedList<>();
        Map parametros = new HashMap();
        parametros.put(";where", "o.celula=:celula and o.activo=true");
        parametros.put("celula", celula);
        try {
            List<Temas> aux = ejbTemas.encontarParametros(parametros);
            int contador = 1;
            for (Temas t : aux) {
                t.setContador(contador);
                contador++;
                listaTemas.add(t);
            }
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Asistenciacelulas buscarAsistentesCelulaTotales() {
        Map parametros = new HashMap();
        parametros.put(";where", "o.celula=:celula and o.activo=true");
        parametros.put("celula", celula);
        try {
            asitencias = ejbAsistenciacelulas.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public List<Temas> traerUnTemas() {
        List<Temas> lista = new LinkedList<>();
        Temas t = new Temas();
        lista.add(t);
        return lista;

    }

    public String traerCheck(Temas t, Asistenciacelulas ac) {
        Map parametros = new HashMap();
        parametros.put(";where", "o.asistenciacelula=:ac and o.tema=:t");
        parametros.put("ac", ac);
        parametros.put("t", t);
        try {
            List<Asistencias> asis = ejbAsistencias.encontarParametros(parametros);
            if (!asis.isEmpty()) {
                if (asis.get(0).getAsistencia()) {
                    if (t.getContador() == 1) {
                        t1++;
                    }
                    if (t.getContador() == 2) {
                        t2++;
                    }
                    if (t.getContador() == 3) {
                        t3++;
                    }
                    if (t.getContador() == 4) {
                        t4++;
                    }
                    if (t.getContador() == 5) {
                        t5++;
                    }

                    return "/";
                }
                if (!asis.get(0).getAsistencia()) {
                    return "X";
                }
            }
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return " ";
    }

    public String traerCheckL(Temas t, Asistenciacelulas ac) {
        Map parametros = new HashMap();
        parametros.put(";where", "o.asistenciacelula=:ac and o.tema=:t");
        parametros.put("ac", ac);
        parametros.put("t", t);
        try {
            List<Asistencias> asis = ejbAsistencias.encontarParametros(parametros);
            if (!asis.isEmpty()) {
                if (asis.get(0).getLectura()) {
                    return "/";
                }
                if (!asis.get(0).getLectura()) {
                    return "X";
                }
            }
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return " ";
    }

    public Boolean traerCheckC(Celulas c, Entidades e, String caso) {
        Map parametros = new HashMap();
        parametros.put(";where", "o.celula=:c and o.entidad=:e");
        parametros.put("c", c);
        parametros.put("e", e);
        try {
            List<Crecimientos> cre = ejbCrecimientos.encontarParametros(parametros);
            if (!cre.isEmpty()) {
                if (caso.equals("pre")) {
                    return cre.get(0).getPre();
                }
                if (caso.equals("enc")) {
                    return cre.get(0).getEnc();
                }
                if (caso.equals("pos")) {
                    return cre.get(0).getPos();
                }
                if (caso.equals("n1")) {
                    return cre.get(0).getNivel1();
                }
                if (caso.equals("n2")) {
                    return cre.get(0).getNivel2();
                }
                if (caso.equals("n3")) {
                    return cre.get(0).getNivel3();
                }

            }
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public String generarInformePdf() {

        if (asitencias.isEmpty() || listaTemas.isEmpty()) {
            MensajesErrores.advertencia("Debe Ingresar los Temas y los Asistentes para generar el informe");
            return null;
        }

        try {
            t1 = 0;
            t2 = 0;
            t3 = 0;
            t4 = 0;
            t5 = 0;

            DocumentoPDF pdf = new DocumentoPDF("CELULAS", null, null, null, true);
            List<AuxiliarReporte> titulos;
            List<AuxiliarReporte> columnasInternas;
            List<AuxiliarReporte> columnasExternas;
            columnasExternas = new LinkedList<>();
            columnasInternas = new LinkedList<>();
            pdf.agregaTitulo("CONTROL MENSUAL DE CRECIMIENTO DE CELULAS");
            pdf.agregaParrafo(" ");
            pdf.agregaParrafo(" ");
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "CODIGO DE CELULA:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getCodigo() == null ? "S/R" : celula.getCodigo(), 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "RED:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getRed() == null ? "S/R" : celula.getRed(), 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "NOMBRE DEL LIDER:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getLider().getNombres() == null ? "S/R" : celula.getLider().getNombres(), 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "CODIGO DEL LIDER:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getLider().getCodigo() == null ? "S/R" : celula.getLider().getCodigo(), 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "NOMBRE DE TIMOTEO:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getNombretimoteo() == null ? "S/R" : celula.getNombretimoteo(), 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "FECHA:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getFecha() == null ? "S/R" : getTextoFecha(celula.getFecha()), 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "NOMBRE DEL ANFITRION:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getNombreanfitrion() == null ? "S/R" : celula.getNombreanfitrion(), 3, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "DIRECCION:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getDireccion() == null ? "S/R" : celula.getDireccion(), 3, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "TELEFONO:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getTelefono() == null ? "S/R" : celula.getTelefono(), 3, 0, false));
            pdf.agregarTabla(null, columnasExternas, 4, 100, null, 0);
            pdf.agregaParrafo(" ");
            pdf.agregaParrafo(" ");

            columnasExternas = new LinkedList<>();
            List<AuxiliarReporte> titulosA = new LinkedList<>();
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "Nro.", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "NOMBRE", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "TELEFONO", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "COMITE", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "SERVICIO", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "ASISTENCIA", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "LECTURA BIBLICA", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "U. DE LA VIDA", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "CAP. DESTINO", 1, 0, true));
            int numero = 1;
            if (!asitencias.isEmpty()) {
                for (Asistenciacelulas a : asitencias) {
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, a.getAsistente().getNombres() == null ? "S/R" : String.valueOf(numero), 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 6, false, a.getAsistente().getNombres() == null ? "S/R" : a.getAsistente().getNombres().toUpperCase(), 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, a.getAsistente().getTelefono() == null ? "S/R" : a.getAsistente().getTelefono(), 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, a.getAsistente().getComite() == null ? "S/R" : a.getAsistente().getComite(), 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, a.getAsistente().getServicio() == null ? "S/R" : a.getAsistente().getServicio(), 1, 0, false));
                    titulos = new LinkedList<>();
                    if (!listaTemas.isEmpty()) {
                        int indice = 1;
                        columnasInternas = new LinkedList<>();

                        if (numero == 1) {
                            for (Temas t : listaTemas) {
                                titulos.add(new AuxiliarReporte("String", listaTemas.size(), AuxiliarReporte.ALIGN_CENTER, 6, false, String.valueOf(indice), 1, 0, true));
                                indice++;
                            }
                        }

                        for (Temas t : listaTemas) {
                            columnasInternas.add(new AuxiliarReporte("String", listaTemas.size(), AuxiliarReporte.ALIGN_CENTER, 8, false, traerCheck(t, a), 1, 0, false));
                        }
                    }
                    columnasExternas.add(new AuxiliarReporte("Table", 1, AuxiliarReporte.ALIGN_LEFT, 8, false, pdf.retornarTabla(titulos, columnasInternas, listaTemas.size(), 100, null), 0, 0, false));
                    if (!listaTemas.isEmpty()) {
                        int indice = 1;
                        columnasInternas = new LinkedList<>();
                        titulos = new LinkedList<>();
                        if (numero == 1) {
                            for (Temas t : listaTemas) {
                                titulos.add(new AuxiliarReporte("String", listaTemas.size(), AuxiliarReporte.ALIGN_CENTER, 6, false, String.valueOf(indice), 1, 0, true));
                                indice++;
                            }
                        }

                        for (Temas t : listaTemas) {
                            columnasInternas.add(new AuxiliarReporte("String", listaTemas.size(), AuxiliarReporte.ALIGN_CENTER, 8, false, traerCheckL(t, a), 1, 0, false));
                        }
                    }
                    columnasExternas.add(new AuxiliarReporte("Table", 1, AuxiliarReporte.ALIGN_LEFT, 8, false, pdf.retornarTabla(titulos, columnasInternas, listaTemas.size(), 100, null), 0, 0, false));

                    columnasInternas = new LinkedList<>();
                    titulos = new LinkedList<>();
                    if (numero == 1) {
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 6, false, "PRE", 1, 0, true));
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 6, false, "ENC", 1, 0, true));
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 6, false, "POS", 1, 0, true));
                    }

                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, traerCheckC(a.getCelula(), a.getAsistente(), "pre") == false ? "X" : "/", 1, 0, false));
                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, traerCheckC(a.getCelula(), a.getAsistente(), "enc") == false ? "X" : "/", 1, 0, false));
                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, traerCheckC(a.getCelula(), a.getAsistente(), "pos") == false ? "X" : "/", 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("Table", 1, AuxiliarReporte.ALIGN_CENTER, 8, false, pdf.retornarTabla(titulos, columnasInternas, 3, 100, null), 0, 0, false));

                    columnasInternas = new LinkedList<>();
                    titulos = new LinkedList<>();
                    if (numero == 1) {
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 6, false, "N1", 1, 0, true));
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 6, false, "N2", 1, 0, true));
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 6, false, "N3", 1, 0, true));
                    }

                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, traerCheckC(a.getCelula(), a.getAsistente(), "n1") == false ? "X" : "/", 1, 0, false));
                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, traerCheckC(a.getCelula(), a.getAsistente(), "n2") == false ? "X" : "/", 1, 0, false));
                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, traerCheckC(a.getCelula(), a.getAsistente(), "n3") == false ? "X" : "/", 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("Table", 1, AuxiliarReporte.ALIGN_LEFT, 8, false, pdf.retornarTabla(titulos, columnasInternas, 3, 100, null), 0, 0, false));
                    numero++;
                }
            }

            columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_RIGHT, 8, false, "TOTAL", 5, 0, false));
            if (!listaTemas.isEmpty()) {
                columnasInternas = new LinkedList<>();

                for (Temas t : listaTemas) {
                    if (t.getContador() == 1) {
                        columnasInternas.add(new AuxiliarReporte("String", listaTemas.size(), AuxiliarReporte.ALIGN_CENTER, 6, false, String.valueOf(t1), 1, 0, false));

                    }
                    if (t.getContador() == 2) {
                        columnasInternas.add(new AuxiliarReporte("String", listaTemas.size(), AuxiliarReporte.ALIGN_CENTER, 6, false, String.valueOf(t2), 1, 0, false));

                    }
                    if (t.getContador() == 3) {
                        columnasInternas.add(new AuxiliarReporte("String", listaTemas.size(), AuxiliarReporte.ALIGN_CENTER, 6, false, String.valueOf(t3), 1, 0, false));

                    }
                    if (t.getContador() == 4) {
                        columnasInternas.add(new AuxiliarReporte("String", listaTemas.size(), AuxiliarReporte.ALIGN_CENTER, 6, false, String.valueOf(t4), 1, 0, false));

                    }
                    if (t.getContador() == 5) {
                        columnasInternas.add(new AuxiliarReporte("String", listaTemas.size(), AuxiliarReporte.ALIGN_CENTER, 6, false, String.valueOf(t5), 1, 0, false));

                    }
                }
            }
            columnasExternas.add(new AuxiliarReporte("Table", 1, AuxiliarReporte.ALIGN_LEFT, 8, false, pdf.retornarTabla(null, columnasInternas, listaTemas.size(), 100, null), 0, 0, false));

            columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 3, 0, false));

            pdf.agregarTabla(titulosA, columnasExternas, 9, 100, null, 0);
            pdf.agregaParrafo(" ");

            columnasExternas = new LinkedList<>();
            titulos = new LinkedList<>();
            int Semana = 1;
            if (!listaTemas.isEmpty()) {

                titulos.add(new AuxiliarReporte("String", 8, AuxiliarReporte.ALIGN_CENTER, 8, false, "SEMANA", 1, 0, true));
                titulos.add(new AuxiliarReporte("String", 8, AuxiliarReporte.ALIGN_CENTER, 8, false, "TEMA", 4, 0, true));
                titulos.add(new AuxiliarReporte("String", 8, AuxiliarReporte.ALIGN_CENTER, 8, false, "OBSERVACION", 2, 0, true));
                titulos.add(new AuxiliarReporte("String", 8, AuxiliarReporte.ALIGN_CENTER, 8, false, "FECHA", 1, 0, true));
                for (Temas t : listaTemas) {
                    columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, String.valueOf(Semana), 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, t.getTemas() == null ? "S/R" : t.getTemas(), 4, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, t.getObservacion() == null ? "S/R" : t.getObservacion(), 2, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, t.getFecha() == null ? "S/R" : getTextoFecha(t.getFecha()), 1, 0, false));
                    Semana++;
                }
            }
            pdf.agregarTabla(titulos, columnasExternas, 8, 100, null, 0);
            pdf.agregaParrafo(" ");
            pdf.agregaParrafo(" ");

            columnasExternas = new LinkedList<>();
            titulos = new LinkedList<>();

            titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, "PROMEDIO DE ASISTENTES", 1, 0, true));
            titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, "INVITADOS", 1, 0, true));
            titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, "CELULAS NO REALIZADAS", 1, 0, true));
            columnasExternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_LEFT, 8, false, String.valueOf((t1 + t2 + t3 + t4 + t5) / 5), 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getInvitados() == null ? " " : String.valueOf(celula.getInvitados()), 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getCelulasn() == null ? " " : String.valueOf(celula.getCelulasn()), 1, 0, false));

            pdf.agregarTabla(titulos, columnasExternas, 3, 100, null, 0);

            setArchivoPlanilla(pdf.traerRecurso());
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(ReporteCelulaABean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public String generarInformePdfBlanco() {

        if (asitencias.isEmpty()) {
            MensajesErrores.advertencia("Debe Ingresar los Asistentes para generar el informe");
            return null;
        }

        try {

            DocumentoPDF pdf = new DocumentoPDF("CELULAS", null, null, null, true);
            List<AuxiliarReporte> titulos;
            List<AuxiliarReporte> columnasInternas;
            List<AuxiliarReporte> columnasExternas;
            columnasExternas = new LinkedList<>();
            columnasInternas = new LinkedList<>();
            pdf.agregaTitulo("CONTROL MENSUAL DE CRECIMIENTO DE CELULAS");
            pdf.agregaParrafo(" ");
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "CODIGO DE CELULA:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getCodigo() == null ? "S/R" : celula.getCodigo(), 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "RED:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getRed() == null ? "S/R" : celula.getRed(), 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "NOMBRE DEL LIDER:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getLider().getNombres() == null ? "S/R" : celula.getLider().getNombres(), 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "CODIGO DEL LIDER:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getLider().getCodigo() == null ? "S/R" : celula.getLider().getCodigo(), 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "FECHA:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "NOMBRE DE TIMOTEO:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getNombretimoteo() == null ? "S/R" : celula.getNombretimoteo(), 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "HORA:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "NOMBRE DEL ANFITRION:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getNombreanfitrion() == null ? "S/R" : celula.getNombreanfitrion(), 3, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "DIRECCION:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getDireccion() == null ? "S/R" : celula.getDireccion(), 3, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, true, "TELEFONO:", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 4, AuxiliarReporte.ALIGN_LEFT, 8, false, celula.getTelefono() == null ? "S/R" : celula.getTelefono(), 3, 0, false));
            pdf.agregarTabla(null, columnasExternas, 4, 100, null, 0);
            pdf.agregaParrafo(" ");

            columnasExternas = new LinkedList<>();
            List<AuxiliarReporte> titulosA = new LinkedList<>();
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "Nro.", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "NOMBRE", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "TELEFONO", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "COMITE", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "SERVICIO", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "ASISTENCIA", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "LECTURA BIBLICA", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "U. DE LA VIDA", 1, 0, true));
            titulosA.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_CENTER, 8, false, "CAP. DESTINO", 1, 0, true));
            int numero = 1;
            if (!asitencias.isEmpty()) {
                for (Asistenciacelulas a : asitencias) {
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, a.getAsistente().getNombres() == null ? "S/R" : String.valueOf(numero), 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 6, false, a.getAsistente().getNombres() == null ? "S/R" : a.getAsistente().getNombres().toUpperCase(), 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, a.getAsistente().getTelefono() == null ? "S/R" : a.getAsistente().getTelefono(), 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, a.getAsistente().getComite() == null ? "S/R" : a.getAsistente().getComite(), 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, a.getAsistente().getServicio() == null ? "S/R" : a.getAsistente().getServicio(), 1, 0, false));
                    titulos = new LinkedList<>();

                    int indice = 1;
                    columnasInternas = new LinkedList<>();

                    if (numero == 1) {
                        for (int i = 0; i < 5; i++) {
                            titulos.add(new AuxiliarReporte("String", 5, AuxiliarReporte.ALIGN_CENTER, 6, false, String.valueOf(indice), 1, 0, true));
                            indice++;
                        }
                    }

                    for (int i = 0; i < 5; i++) {
                        columnasInternas.add(new AuxiliarReporte("String", 5, AuxiliarReporte.ALIGN_LEFT, 8, false," ", 1, 0, false));
                    }

                    columnasExternas.add(new AuxiliarReporte("Table", 1, AuxiliarReporte.ALIGN_LEFT, 8, false, pdf.retornarTabla(titulos, columnasInternas, 5, 100, null), 0, 0, false));

                    indice = 1;
                    columnasInternas = new LinkedList<>();
                    titulos = new LinkedList<>();
                    if (numero == 1) {
                        for (int i = 0; i < 5; i++) {
                            titulos.add(new AuxiliarReporte("String", 5, AuxiliarReporte.ALIGN_CENTER, 6, false, String.valueOf(indice), 1, 0, true));
                            indice++;
                        }
                    }

                    for (int i = 0; i < 5; i++) {
                        columnasInternas.add(new AuxiliarReporte("String", 5, AuxiliarReporte.ALIGN_LEFT, 8, false," ", 1, 0, false));
                    }

                    columnasExternas.add(new AuxiliarReporte("Table", 1, AuxiliarReporte.ALIGN_LEFT, 8, false, pdf.retornarTabla(titulos, columnasInternas, 5, 100, null), 0, 0, false));

                    columnasInternas = new LinkedList<>();
                    titulos = new LinkedList<>();
                    if (numero == 1) {
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 6, false, "PRE", 1, 0, true));
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 6, false, "ENC", 1, 0, true));
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 6, false, "POS", 1, 0, true));
                    }

                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, traerCheckC(a.getCelula(), a.getAsistente(), "pre") == false ? " " : "/", 1, 0, false));
                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, traerCheckC(a.getCelula(), a.getAsistente(), "enc") == false ? " " : "/", 1, 0, false));
                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, traerCheckC(a.getCelula(), a.getAsistente(), "pos") == false ? " " : "/", 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("Table", 1, AuxiliarReporte.ALIGN_LEFT, 8, false, pdf.retornarTabla(titulos, columnasInternas, 3, 100, null), 0, 0, false));

                    columnasInternas = new LinkedList<>();
                    titulos = new LinkedList<>();
                    if (numero == 1) {
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 6, false, "N1", 1, 0, true));
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 6, false, "N2", 1, 0, true));
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 6, false, "N3", 1, 0, true));
                    }

                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, traerCheckC(a.getCelula(), a.getAsistente(), "n1") == false ? " " : "/", 1, 0, false));
                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, traerCheckC(a.getCelula(), a.getAsistente(), "n2") == false ? " " : "/", 1, 0, false));
                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, traerCheckC(a.getCelula(), a.getAsistente(), "n3") == false ? " " : "/", 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("Table", 1, AuxiliarReporte.ALIGN_LEFT, 8, false, pdf.retornarTabla(titulos, columnasInternas, 3, 100, null), 0, 0, false));
                    numero++;
                }
                //completar los 30
                for (int i = asitencias.size(); i < 30; i++) {
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, String.valueOf(numero), 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 1, 0, false));
                    titulos = new LinkedList<>();

                    int indice = 1;
                    columnasInternas = new LinkedList<>();

                    if (numero == 1) {
                        for (int j = 0; j < 5; j++) {
                            titulos.add(new AuxiliarReporte("String", 5, AuxiliarReporte.ALIGN_CENTER, 8, false, String.valueOf(indice), 1, 0, true));
                            indice++;
                        }
                    }

                    for (int j = 0; j < 5; j++) {
                        columnasInternas.add(new AuxiliarReporte("String", 5, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 1, 0, false));
                    }

                    columnasExternas.add(new AuxiliarReporte("Table", 1, AuxiliarReporte.ALIGN_LEFT, 8, false, pdf.retornarTabla(titulos, columnasInternas, 5, 100, null), 0, 0, false));

                    indice = 1;
                    columnasInternas = new LinkedList<>();
                    titulos = new LinkedList<>();
                    if (numero == 1) {
                        for (int j = 0; j < 5; j++) {
                            titulos.add(new AuxiliarReporte("String", 5, AuxiliarReporte.ALIGN_CENTER, 8, false, String.valueOf(indice), 1, 0, true));
                            indice++;
                        }
                    }

                    for (int j = 0; j < 5; j++) {
                        columnasInternas.add(new AuxiliarReporte("String", 5, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 1, 0, false));
                    }

                    columnasExternas.add(new AuxiliarReporte("Table", 1, AuxiliarReporte.ALIGN_LEFT, 8, false, pdf.retornarTabla(titulos, columnasInternas, 5, 100, null), 0, 0, false));

                    columnasInternas = new LinkedList<>();
                    titulos = new LinkedList<>();
                    if (numero == 1) {
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, "PRE", 1, 0, true));
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, "ENC", 1, 0, true));
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, "POS", 1, 0, true));
                    }

                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, " ", 1, 0, false));
                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, " ", 1, 0, false));
                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, " ", 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("Table", 1, AuxiliarReporte.ALIGN_LEFT, 8, false, pdf.retornarTabla(titulos, columnasInternas, 3, 100, null), 0, 0, false));

                    columnasInternas = new LinkedList<>();
                    titulos = new LinkedList<>();
                    if (numero == 1) {
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, "N1", 1, 0, true));
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, "N2", 1, 0, true));
                        titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, "N3", 1, 0, true));
                    }

                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, " ", 1, 0, false));
                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, " ", 1, 0, false));
                    columnasInternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, " ", 1, 0, false));
                    columnasExternas.add(new AuxiliarReporte("Table", 1, AuxiliarReporte.ALIGN_LEFT, 8, false, pdf.retornarTabla(titulos, columnasInternas, 3, 100, null), 0, 0, false));
                    numero++;
                }
            }
            columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_RIGHT, 8, false, "TOTAL", 5, 0, false));
            if (!listaTemas.isEmpty()) {
                columnasInternas = new LinkedList<>();

                for (int j = 0; j < 5; j++) {
                    columnasInternas.add(new AuxiliarReporte("String", 5, AuxiliarReporte.ALIGN_CENTER, 6, false, " ", 1, 0, false));
                }
            }
            columnasExternas.add(new AuxiliarReporte("Table", 1, AuxiliarReporte.ALIGN_LEFT, 8, false, pdf.retornarTabla(null, columnasInternas, 5, 100, null), 0, 0, false));

            columnasExternas.add(new AuxiliarReporte("String", 9, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 3, 0, false));

            pdf.agregarTabla(titulosA, columnasExternas, 9, 100, null, 0);
            pdf.agregaParrafo(" ");

            columnasExternas = new LinkedList<>();
            titulos = new LinkedList<>();

            titulos.add(new AuxiliarReporte("String", 8, AuxiliarReporte.ALIGN_CENTER, 8, false, "SEMANA", 1, 0, true));
            titulos.add(new AuxiliarReporte("String", 8, AuxiliarReporte.ALIGN_CENTER, 8, false, "TEMA", 4, 0, true));
            titulos.add(new AuxiliarReporte("String", 8, AuxiliarReporte.ALIGN_CENTER, 8, false, "OBSERVACION", 2, 0, true));
            titulos.add(new AuxiliarReporte("String", 8, AuxiliarReporte.ALIGN_CENTER, 8, false, "FECHA", 1, 0, true));
            for (int i = 0; i < 5; i++) {
                columnasExternas.add(new AuxiliarReporte("String", 8, AuxiliarReporte.ALIGN_LEFT, 8, false, String.valueOf((i + 1)), 1, 0, false));
                columnasExternas.add(new AuxiliarReporte("String", 8, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 4, 0, false));
                columnasExternas.add(new AuxiliarReporte("String", 8, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 2, 0, false));
                columnasExternas.add(new AuxiliarReporte("String", 8, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 1, 0, false));

            }

            pdf.agregarTabla(titulos, columnasExternas, 8, 100, null, 0);
            pdf.agregaParrafo(" ");

            columnasExternas = new LinkedList<>();
            titulos = new LinkedList<>();

            titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, "PROMEDIO DE ASISTENTES", 1, 0, true));
            titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, "INVITADOS", 1, 0, true));
            titulos.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_CENTER, 8, false, "CELULAS NO REALIZADAS", 1, 0, true));
            columnasExternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 1, 0, false));
            columnasExternas.add(new AuxiliarReporte("String", 3, AuxiliarReporte.ALIGN_LEFT, 8, false, " ", 1, 0, false));

            pdf.agregarTabla(titulos, columnasExternas, 3, 100, null, 0);

            setArchivoPlanillaBlanco(pdf.traerRecurso());
        } catch (IOException | DocumentException ex) {
            Logger.getLogger(ReporteCelulaABean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
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
     * @return the archivoPlanilla
     */
    public Resource getArchivoPlanilla() {
        return archivoPlanilla;
    }

    /**
     * @param archivoPlanilla the archivoPlanilla to set
     */
    public void setArchivoPlanilla(Resource archivoPlanilla) {
        this.archivoPlanilla = archivoPlanilla;
    }

    /**
     * @return the archivoPlanillaBlanco
     */
    public Resource getArchivoPlanillaBlanco() {
        return archivoPlanillaBlanco;
    }

    /**
     * @param archivoPlanillaBlanco the archivoPlanillaBlanco to set
     */
    public void setArchivoPlanillaBlanco(Resource archivoPlanillaBlanco) {
        this.archivoPlanillaBlanco = archivoPlanillaBlanco;
    }

    /**
     * @return the t1
     */
    public int getT1() {
        return t1;
    }

    /**
     * @param t1 the t1 to set
     */
    public void setT1(int t1) {
        this.t1 = t1;
    }

    /**
     * @return the t2
     */
    public int getT2() {
        return t2;
    }

    /**
     * @param t2 the t2 to set
     */
    public void setT2(int t2) {
        this.t2 = t2;
    }

    /**
     * @return the t3
     */
    public int getT3() {
        return t3;
    }

    /**
     * @param t3 the t3 to set
     */
    public void setT3(int t3) {
        this.t3 = t3;
    }

    /**
     * @return the t4
     */
    public int getT4() {
        return t4;
    }

    /**
     * @param t4 the t4 to set
     */
    public void setT4(int t4) {
        this.t4 = t4;
    }

    /**
     * @return the t5
     */
    public int getT5() {
        return t5;
    }

    /**
     * @param t5 the t5 to set
     */
    public void setT5(int t5) {
        this.t5 = t5;
    }

}
