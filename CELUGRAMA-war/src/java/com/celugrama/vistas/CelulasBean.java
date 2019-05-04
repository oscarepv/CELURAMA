/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.vistas;

import com.celugrama.comun.ParametrosCelugramaBean;
import com.celugrama.entidades.Asistenciacelulas;
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
import com.celugrama.servicios.AsistenciacelulasFacade;
import com.celugrama.servicios.CelulasFacade;
import com.celugrama.servicios.CodigosFacade;
import com.celugrama.servicios.CrecimientosFacade;
import com.celugrama.servicios.EntidadesFacade;
import com.celugrama.servicios.TemasFacade;
import com.celugrama.utilitarios.Formulario;
import com.celugrama.utilitarios.MensajesErrores;
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
import org.icefaces.ace.model.table.LazyDataModel;
import org.icefaces.ace.model.table.SortCriteria;

/**
 *
 * @author oscar
 */
@ManagedBean(name = "celulasCelugrama")
@ViewScoped
public class CelulasBean {

    @ManagedProperty(value = "#{parametrosCelugrama}")
    private ParametrosCelugramaBean seguridadbean;

    private LazyDataModel<Celulas> celulas;
    private Celulas celula;
    private Formulario formulario = new Formulario();
    private Formulario formularioTemas = new Formulario();
    private Formulario formularioTema = new Formulario();
    private Formulario formularioAsistentes = new Formulario();
    private Perfiles perfil;
    private List<Temas> listaTemas;
    private Temas tema;
    private List<Entidades> asistentesTotales;
    private List<Asistenciacelulas> asitencias;
    private List<Asistenciacelulas> asitenciasaux;
    private List<Asistenciacelulas> asitenciasAuxGrabar;
    private List<Crecimientos> listaCrecimientos;
    private Codigos red;
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

    public CelulasBean() {
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
        String nombreForma = "CelulasVista";
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

                    where += " and upper(o." + clave + ") like :" + clave;
                    parametros.put(clave, valor.toUpperCase() + "%");
                }

                int total = 0;

                try {
                    where += " and o.lider=:lider";
                    parametros.put("lider", seguridadbean.getLogueado());
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
        celula = new Celulas();
        int valor = 0;
        String codigo = generarCodigo(valor);
        Boolean parar = false;
        while (!parar) {
            Map parametros = new HashMap();
            parametros.put(";where", "o.codigo=:codigo");
            parametros.put("codigo", codigo);
            List<Celulas> listadoCelulas;
            try {
                listadoCelulas = ejbCelulas.encontarParametros(parametros);
                if (listadoCelulas.isEmpty()) {
                    parar = true;
                    break;
                }
            } catch (ConsultarException ex) {
                Logger.getLogger(AsistentesBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            valor++;
            codigo = generarCodigo(valor);

        }
        celula.setCodigo(codigo);
        asitenciasAuxGrabar = new LinkedList<>();
        listaCrecimientos = new LinkedList<>();
        formulario.insertar();
        return null;
    }

    private String generarCodigo(int valor) {
        try {
            if (valor != 0) {
                Map parametros = new HashMap();
                parametros.put(";orden", "o.id desc");
                return "CODC-" + (ejbCelulas.contar(parametros) + valor);
            } else {
                Map parametros = new HashMap();
                parametros.put(";orden", "o.id desc");
                return "CODC-" + ejbCelulas.contar(parametros);
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
            celula.setRed(red.getParametros());
            celula.setActivo(Boolean.TRUE);
            celula.setLider(getSeguridadbean().getLogueado());
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

        } catch (InsertarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger("").log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    private boolean validar() {

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

    public String buscarCelula() {
        Map parametros = new HashMap();
        parametros.put(";where", "o.activo=true and o.codigo=:codigo and o.lider=:lider");
        parametros.put("codigo", pbuscar);
        parametros.put("lider", seguridadbean.getLogueado());
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

    public String modificar() {
        if (!perfil.getModificacion()) {
            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
            return null;
        }
        celula = (Celulas) celulas.getRowData();
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
            celula.setActivo(Boolean.FALSE);
            ejbCelulas.edit(celula, getSeguridadbean().getLogueado().getUserid());
        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger("").log(Level.SEVERE, null, ex);
        }

        formulario.cancelar();
        buscar();
        return null;
    }

    public void buscarTemas() {
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
        int valor = 0;
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
                Logger.getLogger(AsistentesBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            valor++;
            codigo = generarCodigoT(valor);

        }
        tema.setCodigo(codigo);
        formularioTema.insertar();
        return null;
    }

    private String generarCodigoT(int valor) {
        try {
            if (valor != 0) {
                Map parametros = new HashMap();
                parametros.put(";orden", "o.id desc");
                return "CODT-" + (ejbTemas.contar(parametros) + valor);
            } else {
                Map parametros = new HashMap();
                parametros.put(";orden", "o.id desc");
                return "CODT-" + ejbTemas.contar(parametros);
            }
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasABean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public String insertarTema() {
        if (!validarTema()) {
            return null;
        }
        tema.setCelula(celula);
        tema.setActivo(true);
        try {
            ejbTemas.create(tema, getSeguridadbean().getLogueado().getUserid());
        } catch (InsertarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
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

    public Boolean validarEdicion() {
        if (traerAnio(celula.getFecha()) != traerAnio(new Date()) || !traerMesActual(celula.getFecha()).equals(traerMesActual(new Date()))) {
            if (traerAnio(celula.getFecha()) != traerAnio(new Date()) || !traerMes(celula.getFecha()).equals(traerMesActual(new Date()))) {
                return false;
            }
        }
        return true;
    }

    public String traerMes(Date fecha) {
        if (fecha != null) {
            Calendar act = Calendar.getInstance();
            act.setTime(fecha);
            act.add(Calendar.MONTH, 1);
            return strMonths[act.get(Calendar.MONTH)];
        }
        return null;
    }

    public String traerMesActual(Date fecha) {
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
            ejbTemas.remove(tema, getSeguridadbean().getLogueado().getUserid());
        } catch (BorrarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private Asistenciacelulas buscarAsistentesCelula(Entidades e) {
        Map parametros = new HashMap();
        parametros.put(";where", "o.celula=:celula and o.asistente=:asistente and o.asistente.activo=true");
        parametros.put("celula", celula);
        parametros.put("asistente", e);
        try {
            List<Asistenciacelulas> ac = ejbAsistenciacelulas.encontarParametros(parametros);
            if (!ac.isEmpty()) {
                return ac.get(0);
            }
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        formularioAsistentes.cancelar();
        buscarAsistentesCelulaTotales();
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
}
