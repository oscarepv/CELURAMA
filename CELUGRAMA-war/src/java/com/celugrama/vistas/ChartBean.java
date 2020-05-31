package com.celugrama.vistas;

import com.celugrama.comun.ParametrosCelugramaBean;
import com.celugrama.entidades.Asistenciacelulas;
import com.celugrama.entidades.Asistencias;
import com.celugrama.entidades.Crecimientos;
import com.celugrama.entidades.Celulas;
import com.celugrama.entidades.Perfiles;
import com.celugrama.entidades.Temas;
import com.celugrama.excepciones.ConsultarException;
import com.celugrama.servicios.AsistenciacelulasFacade;
import com.celugrama.servicios.AsistenciasFacade;
import com.celugrama.servicios.CelulasFacade;
import com.celugrama.servicios.CrecimientosFacade;
import com.celugrama.servicios.TemasFacade;
import com.celugrama.utilitarios.Formulario;
import com.celugrama.utilitarios.MensajesErrores;
import java.io.Serializable;
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
import javax.faces.bean.CustomScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import org.icefaces.ace.component.chart.Axis;
import org.icefaces.ace.component.chart.AxisType;
import org.icefaces.ace.model.chart.CartesianSeries;
import org.icefaces.ace.model.table.LazyDataModel;
import org.icefaces.ace.model.table.SortCriteria;

@ManagedBean(name = ChartBean.BEAN_NAME)
@CustomScoped(value = "#{window}")
public class ChartBean implements Serializable {

    public static final String BEAN_NAME = "chartBean";

    public String getBeanName() {
        return BEAN_NAME;
    }

    @ManagedProperty(value = "#{parametrosCelugrama}")
    private ParametrosCelugramaBean seguridadbean;

    private LazyDataModel<Celulas> celulas;
    private Celulas celula;
    private Formulario formulario = new Formulario();
    private List<Temas> listaTemas;
    private List<Asistenciacelulas> asitencias;
    private Perfiles perfil;

    private CartesianSeries barData = new CartesianSeries();
    private final String[] colorSet = new String[]{"#FF8C00"};
    private final String[] colorSet2 = new String[]{"#FFE135"};
    private final String[] colorSet3 = new String[]{"#a40000"};
    private String[] customDefaultColor = colorSet;
    private String[] customDefaultColor2 = colorSet2;
    private String[] customDefaultColor3 = colorSet3;
    private Axis barDemoDefaultAxis = new Axis();
    private Axis barDemoXAxis = new Axis();
    private Axis[] barDemoYAxes = new Axis[]{
        new Axis() {
            {
                setMin(0);
                setMax(100);
                setTickInterval("20");
                setFormatString("%d");
                setPad(0.0);
                setLabel("%");
            }
        }
    };
    private CartesianSeries barDataL = new CartesianSeries();
    private Axis barDemoDefaultAxisL = new Axis();
    private Axis barDemoXAxisL = new Axis();
    private Axis[] barDemoYAxesL = new Axis[]{
        new Axis() {
            {
                setMin(0);
                setMax(100);
                setTickInterval("20");
                setFormatString("%d");
                setPad(0.0);
                setLabel("%");
            }
        }
    };
    private CartesianSeries barDataU = new CartesianSeries();
    private Axis barDemoDefaultAxisU = new Axis();
    private Axis barDemoXAxisU = new Axis();
    private Axis[] barDemoYAxesU = new Axis[]{
        new Axis() {
            {
                setMin(0);
                setMax(100);
                setTickInterval("20");
                setFormatString("%d");
                setPad(0.0);
                setLabel("%");
            }
        }
    };
    private CartesianSeries barDataC = new CartesianSeries();
    private Axis barDemoDefaultAxisC = new Axis();
    private Axis barDemoXAxisC = new Axis();
    private Axis[] barDemoYAxesC = new Axis[]{
        new Axis() {
            {
                setMin(0);
                setMax(100);
                setTickInterval("20");
                setFormatString("%d");
                setPad(0.0);
                setLabel("%");
            }
        }
    };

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

    public ChartBean() {
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
        formulario.cancelar();

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

    public String buscar() {
//        if (!menusBean.getPerfil().getConsulta()) {
//            MensajesErrores.advertencia("No tiene autorización para consultar");
//            return null;
//        }
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

    public String ver() {
        if (!perfil.getConsulta()) {
            MensajesErrores.advertencia("No tiene autorización para consultar");
            return null;
        }
        celula = (Celulas) celulas.getRowData();
        buscarTemas();
        buscarAsistentesCelulaTotales();

        barData = new CartesianSeries() {
            {

                setType(CartesianSeries.CartesianType.BAR);
                for (Temas t : listaTemas) {
                    add(t.getContador(), traerPorcentajeAsistencia(t));
                }
                setLabel(TraerAsistentntesTotales() + " asistentes");
            }
        };

        barDemoDefaultAxis = new Axis() {
            {
                setTickAngle(-30);
            }
        };

        barDemoXAxis = new Axis() {
            {
                setType(AxisType.CATEGORY);
                setLabel("Sesiones");
            }
        };
        barDataL = new CartesianSeries() {
            {

                setType(CartesianSeries.CartesianType.BAR);
                for (Temas t : listaTemas) {
                    add(t.getContador(), traerPorcentajeLectura(t));
                }
                setLabel(TraerAsistentntesTotales() + " asistentes");
            }
        };

        barDemoDefaultAxisL = new Axis() {
            {
                setTickAngle(-30);
            }
        };

        barDemoXAxisL = new Axis() {
            {
                setType(AxisType.CATEGORY);
                setLabel("Sesiones");
            }
        };
        barDataU = new CartesianSeries() {
            {

                setType(CartesianSeries.CartesianType.BAR);
                setType(CartesianSeries.CartesianType.BAR);
                add("PRE", traerPorcentaje("pre"));
                add("ENC", traerPorcentaje("enc"));
                add("POS", traerPorcentaje("pos"));
                setLabel(TraerAsistentntesTotales() + " asistentes");
            }
        };

        barDemoDefaultAxisU = new Axis() {
            {
                setTickAngle(-30);
            }
        };

        barDemoXAxisU = new Axis() {
            {
                setType(AxisType.CATEGORY);
                setLabel("U. de la vida");
            }
        };

        barDataC = new CartesianSeries() {
            {
                setType(CartesianSeries.CartesianType.BAR);
                add("Nivel 1", traerPorcentaje("n1"));
                add("Nivel 2", traerPorcentaje("n2"));
                add("Nivel 3", traerPorcentaje("n3"));
                setLabel(TraerAsistentntesTotales() + " asistentes");
            }
        };

        barDemoDefaultAxisC = new Axis() {
            {
                setTickAngle(-30);
            }
        };

        barDemoXAxisC = new Axis() {
            {
                setType(AxisType.CATEGORY);
                setLabel("Capacitación Destino");
            }
        };
        formulario.editar();
        return null;
    }

    private int TraerAsistentntesTotales() {
        Map parametros = new HashMap();
        parametros.put(";where", "o.celula=:celula and o.activo=true");
        parametros.put("celula", celula);
        try {
            return ejbAsistenciacelulas.contar(parametros);
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    private Double traerPorcentajeAsistencia(Temas t) {
        Map parametros = new HashMap();
        parametros.put(";where", "o.asistenciacelula.celula=:celula and o.tema=:t ");
        parametros.put("celula", celula);
        parametros.put("t", t);
        try {
            double totalAsistencia = 0;
            double totalAsistenntes = 0;
            List<Asistencias> lista = ejbAsistencias.encontarParametros(parametros);
            for (Asistencias a : lista) {
                if (a.getAsistencia()) {
                    totalAsistencia += 1;
                }

            }
            parametros = new HashMap();
            parametros.put(";where", "o.celula=:celula and o.activo=true ");
            parametros.put("celula", celula);
            totalAsistenntes = ejbAsistenciacelulas.contar(parametros);
            return (totalAsistencia * 100) / totalAsistenntes;
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0.0;
    }

    private Double traerPorcentajeLectura(Temas t) {
        Map parametros = new HashMap();
        parametros.put(";where", "o.asistenciacelula.celula=:celula and o.tema=:t ");
        parametros.put("celula", celula);
        parametros.put("t", t);
        try {
            double totalAsistencia = 0;
            double totalAsistenntes = 0;
            List<Asistencias> lista = ejbAsistencias.encontarParametros(parametros);
            for (Asistencias a : lista) {
                if (a.getLectura()) {
                    totalAsistencia += 1;
                }

            }
            parametros = new HashMap();
            parametros.put(";where", "o.celula=:celula and o.activo=true ");
            parametros.put("celula", celula);
            totalAsistenntes = ejbAsistenciacelulas.contar(parametros);
            return (totalAsistencia * 100) / totalAsistenntes;
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0.0;
    }

    private Double traerPorcentaje(String caso) {
        Map parametros = new HashMap();
        parametros.put(";where", "o.celula=:celula");
        parametros.put("celula", celula);
        try {
            double totalAsistencia = 0;
            double totalAsistenntes = 0;
            List<Crecimientos> lista = ejbCrecimientos.encontarParametros(parametros);
            for (Crecimientos a : lista) {
                if (a.getPre() && caso.equals("pre")) {
                    totalAsistencia += 1;
                }

                if (a.getEnc() && caso.equals("enc")) {
                    totalAsistencia += 1;
                }

                if (a.getPos() && caso.equals("pos")) {
                    totalAsistencia += 1;
                }

                if (a.getNivel1() && caso.equals("n1")) {
                    totalAsistencia += 1;
                }
                if (a.getNivel2() && caso.equals("n2")) {
                    totalAsistencia += 1;
                }

                if (a.getNivel3() && caso.equals("n3")) {
                    totalAsistencia += 1;
                }

            }
            parametros = new HashMap();
            parametros.put(";where", "o.celula=:celula");
            parametros.put("celula", celula);
            totalAsistenntes = ejbCrecimientos.contar(parametros);
            return (totalAsistencia * 100) / totalAsistenntes;
        } catch (ConsultarException ex) {
            Logger.getLogger(CelulasBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0.0;
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

//    private CartesianSeries barData = new CartesianSeries() {
//        {
//
//            setType(CartesianType.BAR);
//            add("HDTV Receiver", 10);
//            add("Cup Holder Pinion Bob", randomizer.nextInt(20));
//            add("Generic Fog Lamp", randomizer.nextInt(20));
//            add("8 Track Control Module", randomizer.nextInt(20));
//            add("Sludge Pump Fourier Modulator", randomizer.nextInt(20));
//            add("Transceiver Spice Rack", randomizer.nextInt(20));
//            add("Hair Spray Danger Indicator", randomizer.nextInt(20));
//            setLabel("Product / Sales");
//        }
//    };
    public CartesianSeries getBarData() {
        return barData;
    }

    public void setBarData(CartesianSeries barData) {
        this.barData = barData;
    }

    public Axis getBarDemoXAxis() {
        return barDemoXAxis;
    }

    public void setBarDemoXAxis(Axis barDemoXAxis) {
        this.barDemoXAxis = barDemoXAxis;
    }

    public Axis getBarDemoDefaultAxis() {
        return barDemoDefaultAxis;
    }

    public void setBarDemoDefaultAxis(Axis barDemoDefaultAxis) {
        this.barDemoDefaultAxis = barDemoDefaultAxis;
    }

    public Axis[] getBarDemoYAxes() {
        return barDemoYAxes;
    }

    public void setBarDemoYAxis(Axis[] barDemoYAxis) {
        this.barDemoYAxes = barDemoYAxis;
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

    /**
     * @return the barDataL
     */
    public CartesianSeries getBarDataL() {
        return barDataL;
    }

    /**
     * @param barDataL the barDataL to set
     */
    public void setBarDataL(CartesianSeries barDataL) {
        this.barDataL = barDataL;
    }

    /**
     * @return the barDemoDefaultAxisL
     */
    public Axis getBarDemoDefaultAxisL() {
        return barDemoDefaultAxisL;
    }

    /**
     * @param barDemoDefaultAxisL the barDemoDefaultAxisL to set
     */
    public void setBarDemoDefaultAxisL(Axis barDemoDefaultAxisL) {
        this.barDemoDefaultAxisL = barDemoDefaultAxisL;
    }

    /**
     * @return the barDemoXAxisL
     */
    public Axis getBarDemoXAxisL() {
        return barDemoXAxisL;
    }

    /**
     * @param barDemoXAxisL the barDemoXAxisL to set
     */
    public void setBarDemoXAxisL(Axis barDemoXAxisL) {
        this.barDemoXAxisL = barDemoXAxisL;
    }

    /**
     * @return the barDemoYAxesL
     */
    public Axis[] getBarDemoYAxesL() {
        return barDemoYAxesL;
    }

    /**
     * @param barDemoYAxesL the barDemoYAxesL to set
     */
    public void setBarDemoYAxesL(Axis[] barDemoYAxesL) {
        this.barDemoYAxesL = barDemoYAxesL;
    }

    /**
     * @return the barDataU
     */
    public CartesianSeries getBarDataU() {
        return barDataU;
    }

    /**
     * @param barDataU the barDataU to set
     */
    public void setBarDataU(CartesianSeries barDataU) {
        this.barDataU = barDataU;
    }

    /**
     * @return the barDemoDefaultAxisU
     */
    public Axis getBarDemoDefaultAxisU() {
        return barDemoDefaultAxisU;
    }

    /**
     * @param barDemoDefaultAxisU the barDemoDefaultAxisU to set
     */
    public void setBarDemoDefaultAxisU(Axis barDemoDefaultAxisU) {
        this.barDemoDefaultAxisU = barDemoDefaultAxisU;
    }

    /**
     * @return the barDemoXAxisU
     */
    public Axis getBarDemoXAxisU() {
        return barDemoXAxisU;
    }

    /**
     * @param barDemoXAxisU the barDemoXAxisU to set
     */
    public void setBarDemoXAxisU(Axis barDemoXAxisU) {
        this.barDemoXAxisU = barDemoXAxisU;
    }

    /**
     * @return the barDemoYAxesU
     */
    public Axis[] getBarDemoYAxesU() {
        return barDemoYAxesU;
    }

    /**
     * @param barDemoYAxesU the barDemoYAxesU to set
     */
    public void setBarDemoYAxesU(Axis[] barDemoYAxesU) {
        this.barDemoYAxesU = barDemoYAxesU;
    }

    /**
     * @return the barDataC
     */
    public CartesianSeries getBarDataC() {
        return barDataC;
    }

    /**
     * @param barDataC the barDataC to set
     */
    public void setBarDataC(CartesianSeries barDataC) {
        this.barDataC = barDataC;
    }

    /**
     * @return the barDemoDefaultAxisC
     */
    public Axis getBarDemoDefaultAxisC() {
        return barDemoDefaultAxisC;
    }

    /**
     * @param barDemoDefaultAxisC the barDemoDefaultAxisC to set
     */
    public void setBarDemoDefaultAxisC(Axis barDemoDefaultAxisC) {
        this.barDemoDefaultAxisC = barDemoDefaultAxisC;
    }

    /**
     * @return the barDemoXAxisC
     */
    public Axis getBarDemoXAxisC() {
        return barDemoXAxisC;
    }

    /**
     * @param barDemoXAxisC the barDemoXAxisC to set
     */
    public void setBarDemoXAxisC(Axis barDemoXAxisC) {
        this.barDemoXAxisC = barDemoXAxisC;
    }

    /**
     * @return the barDemoYAxesC
     */
    public Axis[] getBarDemoYAxesC() {
        return barDemoYAxesC;
    }

    /**
     * @param barDemoYAxesC the barDemoYAxesC to set
     */
    public void setBarDemoYAxesC(Axis[] barDemoYAxesC) {
        this.barDemoYAxesC = barDemoYAxesC;
    }

    /**
     * @return the customDefaultColor
     */
    public String[] getCustomDefaultColor() {
        return customDefaultColor;
    }

    /**
     * @param customDefaultColor the customDefaultColor to set
     */
    public void setCustomDefaultColor(String[] customDefaultColor) {
        this.customDefaultColor = customDefaultColor;
    }

    /**
     * @return the customDefaultColor2
     */
    public String[] getCustomDefaultColor2() {
        return customDefaultColor2;
    }

    /**
     * @param customDefaultColor2 the customDefaultColor2 to set
     */
    public void setCustomDefaultColor2(String[] customDefaultColor2) {
        this.customDefaultColor2 = customDefaultColor2;
    }

    /**
     * @return the customDefaultColor3
     */
    public String[] getCustomDefaultColor3() {
        return customDefaultColor3;
    }

    /**
     * @param customDefaultColor3 the customDefaultColor3 to set
     */
    public void setCustomDefaultColor3(String[] customDefaultColor3) {
        this.customDefaultColor3 = customDefaultColor3;
    }
}
