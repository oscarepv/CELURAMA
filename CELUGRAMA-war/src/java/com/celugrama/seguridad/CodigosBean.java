/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.seguridad;

//import com.icesoft.faces.component.selectinputtext.SelectInputText;
import com.celugrama.comun.ParametrosCelugramaBean;
import com.celugrama.entidades.Codigos;
import com.celugrama.entidades.Maestros;
import com.celugrama.entidades.Perfiles;
import com.celugrama.excepciones.ConsultarException;
import com.celugrama.excepciones.GrabarException;
import com.celugrama.excepciones.InsertarException;
import com.celugrama.servicios.CodigosFacade;
import com.celugrama.utilitarios.Combos;
import com.celugrama.utilitarios.Constantes;
import com.celugrama.utilitarios.Formulario;
import com.celugrama.utilitarios.MensajesErrores;
import java.io.Serializable;
import java.util.ArrayList;
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
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import org.icefaces.ace.event.TextChangeEvent;

/**
 *
 * @author sigi-iepi
 */
@ManagedBean(name = "codigosCelugrama")
@ViewScoped
public class CodigosBean implements Serializable {

    /**
     * Creates a new instance of CodigosBean
     */
    public CodigosBean() {
    }
    @ManagedProperty(value = "#{parametrosCelugrama}")
    private ParametrosCelugramaBean seguridadbean;
    private Formulario formulario = new Formulario();
    private Formulario formularioBuscar = new Formulario();
    private List<Codigos> codigos;
    private List<Codigos> codigosAutocompletar;
    private Codigos codigo;
    private String nombre;
    private Integer maestro;
    private Maestros maestroEntidad;
    private String codigoMaestro;
    @EJB
    private CodigosFacade ejbCodigos;
    private Perfiles perfil;

  

   @PostConstruct
   private void activar()  {

        Map params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String perfil = (String) params.get("p");
        String nombreForma = "CodigosVista";
        String redirect = (String) params.get("faces-redirect");
        if (redirect == null) {
            return;
        }
        String empleadoString = (String) params.get("x");
        if (empleadoString != null) {
            return;
        }
        if (perfil == null) {
            MensajesErrores.fatal("Sin perfil válido");
            getSeguridadbean().cerraSession();
        }
        this.setPerfil(getSeguridadbean().traerPerfil(perfil));
        if (this.getPerfil() == null) {
            MensajesErrores.fatal("Sin perfil válido");
            getSeguridadbean().cerraSession();
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
  

    // colocamos los metodos en verbo
    public String crear() {
        // se deberia chequear perfil?
//        if (!menusBean.getPerfil().getNuevo()) {
//            MensajesErrores.advertencia("No tiene autorización para crear un registro");
//            return null;
//        }
        if (maestroEntidad == null) {
            MensajesErrores.advertencia("Seleccione una tabla maestra primero");
            return null;
        }

        codigo = new Codigos();
        setCodigoMaestro(getMaestroEntidad().getCodigo());
        codigo.setMaestro(getMaestroEntidad());
//        

        formulario.insertar();
        return null;
    }

    public String modificar() {
//        if (!menusBean.getPerfil().getModificacion()) {
//            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
//        }
        codigo = codigos.get(formulario.getFila().getRowIndex());
        formulario.editar();
        return null;
    }

    public String eliminar() {
//        if (!menusBean.getPerfil().getBorrado()) {
//            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
//        }
        codigo = codigos.get(formulario.getFila().getRowIndex());
        formulario.eliminar();
        return null;
    }

    public String cancelar() {
        formulario.cancelar();
        buscar();
        return null;
    }
    // buscar

    public String buscar() {
//        if ((codigoMaestro == null) || (codigoMaestro.isEmpty())) {
//            MensajesErrores.advertencia("Seleccione una tabla maestra primero");
//            return null;
//        }
//        try {
//
//            maestroEntidad = ejbCodigos.traerMaestroCodigo(codigoMaestro,Combos.getModuloStr());
//        } catch (ConsultarException ex) {
//            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
//            Logger.getLogger(CodigosBean.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        if (!menusBean.getPerfil().getConsulta()) {
//            MensajesErrores.advertencia("No tiene autorización para consultar");
//            return null;
//        }
        if (getMaestroEntidad() == null) {
            MensajesErrores.advertencia("Seleccione un maestro primero");
            return null;
        }
        try {
            Map parametros = new HashMap();
            parametros.put(";where", "o.maestro=:maestroParametro and o.activo=true");
            parametros.put("maestroParametro", maestroEntidad);
//            parametros.put("modulo", maestroEntidad.getCodigo());
            codigos = ejbCodigos.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CodigosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    // acciones de base de datos

    private boolean validar() {
        if ((codigo.getCodigo() == null) || (codigo.getCodigo().isEmpty())) {
            MensajesErrores.advertencia("Es necesario código");
            return true;
        }
        if ((codigo.getNombre() == null) || (codigo.getNombre().isEmpty())) {
            MensajesErrores.advertencia("Es necesario nombre");
            return true;
        }
        if ((codigo.getDescripcion() == null) || (codigo.getDescripcion().isEmpty())) {
            MensajesErrores.advertencia("Es necesario descripción");
            return true;
        }
        return false;
    }

    public String insertar() {
//        if (!menusBean.getPerfil().getNuevo()) {
//            MensajesErrores.advertencia("No tiene autorización para crear un registro");
//            return null;
//        }
        if (validar()) {
            return null;
        }
        try {
            //            codigo.setIdmaestro(maestroEntidad);
            codigo.setActivo(Boolean.TRUE);
            ejbCodigos.create(codigo, getSeguridadbean().getLogueado().getUserid());
        } catch (InsertarException ex) {
            Logger.getLogger(CodigosBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        formulario.cancelar();
        buscar();
        return null;
    }

    public String grabar() {
//        if (!menusBean.getPerfil().getModificacion()) {
//            MensajesErrores.advertencia("No tiene autorización para modificar un registro");
//            return null;
//        }
        if (validar()) {
            return null;
        }
        try {
            codigo.setActivo(Boolean.TRUE);
            ejbCodigos.edit(codigo, getSeguridadbean().getLogueado().getUserid());

        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CodigosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }

    public String borrar() {
//        if (!menusBean.getPerfil().getBorrado()) {
//            MensajesErrores.advertencia("No tiene autorización para borrar un registro");
//            return null;
//        }
        try {
            codigo.setActivo(Boolean.FALSE);
            ejbCodigos.edit(codigo, getSeguridadbean().getLogueado().getUserid());

        } catch (GrabarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CodigosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        formulario.cancelar();
        buscar();
        return null;
    }
    // falta el combo

    public Codigos traer(Integer id) throws ConsultarException {
        return ejbCodigos.find(id);
    }

   

    public static SelectItem[] getSelectItems(List<Codigos> entities, boolean selectOne) {
        if (entities == null) {
            return null;
        }
        int size = selectOne ? entities.size() + 1 : entities.size();
        SelectItem[] items = new SelectItem[size];
        int i = 0;
        if (selectOne) {
            items[0] = new SelectItem(null, "---");
            i++;
        }
        for (Codigos x : entities) {
            items[i++] = new SelectItem(x.getCodigo(), x.toString());
        }
        return items;
    }

    private List<Codigos> traer(String maestro) {
        try {
            return ejbCodigos.traerCodigos(maestro);

        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage() + "-" + ex.getCause());
            Logger.getLogger(CodigosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public SelectItem[] getComboGrupoUsuariosF() {
        return Combos.getSelectItems(traer(Constantes.GRUPO_USUARIO), false);
    }

    public SelectItem[] getComboGrupoUsuarios() {
        return Combos.getSelectItems(traer(Constantes.GRUPO_USUARIO), true);
    }

    public SelectItem[] getComboClasificacionActivosf() {
        return Combos.getSelectItems(traer(Constantes.CLASIFICACION_ACTIVOS), false);
    }

    public SelectItem[] getComboClasificacionActivos() {
        return Combos.getSelectItems(traer(Constantes.CLASIFICACION_ACTIVOS), true);
    }

    public SelectItem[] getComboTelefonosF() {
        return getSelectItems(traer(Constantes.TIPO_TELEFONOS), false);
    }

    public SelectItem[] getComboTelefonos() {
        return getSelectItems(traer(Constantes.TIPO_TELEFONOS), true);
    }

    public SelectItem[] getComboTipoNombramientos() {
        return getSelectItems(traer(Constantes.TIPO_TELEFONOS), true);
    }

    public SelectItem[] getComboOperadoresF() {
        return getSelectItems(traer(Constantes.OPERADORES), false);
    }

    public SelectItem[] getComboOperadores() {
        return getSelectItems(traer(Constantes.OPERADORES), true);
    }

    public SelectItem[] getComboModulos() {
        return Combos.getSelectItems(traer(Constantes.MODULOS), true);
    }

    public SelectItem[] getComboNiveleducacion() {
        return Combos.getSelectItems(traer(Constantes.NIVEL_EDUCACION), true);
    }

    public SelectItem[] getComboNiveleducacionf() {
        return Combos.getSelectItems(traer(Constantes.NIVEL_EDUCACION), false);
    }

    public SelectItem[] getComboModulosF() {
        return Combos.getSelectItems(traer(Constantes.MODULOS), false);
    }
    
    public SelectItem[] getComboRed() {
        return Combos.getSelectItems(traer(Constantes.RED), false);
    }

    public SelectItem[] getComboFuenteFin() {
        return Combos.getSelectItems(getFuentesFinanciamiento(), false);
    }

    public SelectItem[] getComboFuenteFinv() {
        return Combos.getSelectItems(traer(Constantes.FUENTE_FINANACIAMIENTO), true);
    }

    public List<Codigos> getFuentesFinanciamiento() {
        return traer(Constantes.FUENTE_FINANACIAMIENTO);
    }

    public List<Codigos> getFirmasTesoreria() {
        return traer(Constantes.FIRMAS_TESORERIA);
    }

    public int getTamanoFinanciamiento() {
        return traer(Constantes.FUENTE_FINANACIAMIENTO).size();
    }

    public SelectItem[] getComboDocumentosPresupuestof() {
        return Combos.getSelectItems(traer(Constantes.DOCUMENTOS_PRESUPUESTO), false);
    }

    public SelectItem[] getComboSectores() {
        return Combos.getSelectItems(traer(Constantes.SECTORES), false);
    }

    public SelectItem[] getComboSanciones() {
        return Combos.getSelectItems(traer(Constantes.SANCIONES), true);
    }

    public SelectItem[] getComboFamiliaSuministros() {
        return Combos.getSelectItems(traer(Constantes.FAMILIA_SUMINISTROS), true);
    }

    public SelectItem[] getComboTipoPrestamos() {
        return Combos.getSelectItems(traer(Constantes.TIPO_PRESTAMOS), true);
    }

    public SelectItem[] getComboTipoPrestamosf() {
        return Combos.getSelectItems(traer(Constantes.TIPO_PRESTAMOS), false);
    }

    public SelectItem[] getComboDocumentosPresupuesto() {
        return Combos.getSelectItems(traer(Constantes.DOCUMENTOS_PRESUPUESTO), true);
    }

    public SelectItem[] getComboDocumentos() {
        return Combos.getSelectItems(traer(Constantes.DOCUMENTOS), true);
    }

    public SelectItem[] getComboReportes() {
        return Combos.getSelectItems(traer(Constantes.REPORTES), true);
    }

    public SelectItem[] getComboMetodosDepreciacion() {
        return Combos.getSelectItems(traer(Constantes.METODOS_DEPRECIACION), true);
    }

    public SelectItem[] getComboBancos() {
        return Combos.getSelectItems(traer(Constantes.BANCOS), true);
    }

    public SelectItem[] getComboSustentos() {
        return Combos.getSelectItems(traer(Constantes.SUSTENTOS), false);
    }

    public List<Codigos> getSustentos() {
        return traer(Constantes.SUSTENTOS);
    }

    public SelectItem[] getComboInstituciones() {
        return Combos.getSelectItems(traer(Constantes.INSTITUCIONES), true);
    }

    public SelectItem[] getComboTipoOficina() {
        return Combos.getSelectItems(traer(Constantes.TIPO_OFICINA), true);
    }

    public SelectItem[] getComboMonedas() {
        return Combos.getSelectItems(traer(Constantes.MONEDAS), true);
    }

    public List<SelectItem> getListaBancos() {
        List<Codigos> entities = traer(Constantes.BANCOS);
        int size = entities.size() + 1;

        List<SelectItem> retorno = new ArrayList<>();
        for (Codigos x : entities) {
            retorno.add(new SelectItem(x));
        }
        return retorno;

    }

    public List<Codigos> getListadoBancos() {
        return traer(Constantes.BANCOS);
    }

    public List<Codigos> getListaCuetasCobrar() {
        return traer(Constantes.CUENTAS_COBAR);
    }

    public SelectItem[] getComboClasificacionproveedores() {
        return Combos.getSelectItems(traer(Constantes.CLASIFICACION_PROVEEDORES), true);
    }

    public SelectItem[] getComboClasificacionproveedoresf() {
        return Combos.getSelectItems(traer(Constantes.CLASIFICACION_PROVEEDORES), false);
    }
//    public SelectItem[] getComboNivelEducacionf() {
//        return Combos.getSelectItems(traer(Constantes.NIVEL_EDUCACION), false);
//    }

    public SelectItem[] getComboTipoCuentaBancaria() {
        return Combos.getSelectItems(traer(Constantes.TIPO_CUENTA_BANCARIA), true);
    }

    public SelectItem[] getComboFormaPagoContrato() {
        return Combos.getSelectItems(traer(Constantes.FORMA_PAGO), false);
    }

    public SelectItem[] getComboEquivalenciaTipoAsiento() {
        return Combos.getSelectItems(traer(Constantes.EQUIVALENCIA_TIPO_ASIENTO), true);
    }

    public SelectItem[] getComboEstadoActivos() {
        return Combos.getSelectItems(traer(Constantes.ESTADO_ACTIVOS), true);
    }

    public SelectItem[] getAuxiliaresCuentas() {
        return Combos.getSelectItems(traer(Constantes.AUXILIARES_CUENTAS), true);
    }

    public List<Codigos> getDocumentosPresupuesto() {
        return traer(Constantes.DOCUMENTOS_PRESUPUESTO);
    }

    public List<Codigos> getListaTipoestablecimiento() {
        return traer(Constantes.TIPO_ESTABLECIMIENTO);
    }

    public SelectItem[] getComboTipoestablecimiento() {
        return Combos.getSelectItems(traer(Constantes.TIPO_ESTABLECIMIENTO), true);
    }

    public List<Codigos> getListaCargos() {
        return traer(Constantes.CARGOS);
    }

    public SelectItem[] getComboCargos() {
        return Combos.getSelectItems(traer(Constantes.CARGOS), true);
    }

    public SelectItem[] getComboTitulos() {
        return Combos.getSelectItems(traer(Constantes.TITULOS), true);
    }

    public List<Codigos> getListaTitulos() {
        return traer(Constantes.TITULOS);
    }

    public SelectItem[] getComboTipoGarantias() {
        return Combos.getSelectItems(traer(Constantes.TIPO_GARANTIAS), true);
    }

    public SelectItem[] getComboTipoModificaciones() {
        return Combos.getSelectItems(traer(Constantes.TIPO_MODIFICACIONES), true);
    }

    public SelectItem[] getComboAseguradoras() {
        return Combos.getSelectItems(traer(Constantes.ASEGURADORAS), true);
    }

    public SelectItem[] getComboTipoAcciones() {
        return Combos.getSelectItems(traer(Constantes.TIPO_ACCIONES), true);
    }

    public SelectItem[] getComboTipoAccionesf() {
        return Combos.getSelectItems(traer(Constantes.TIPO_ACCIONES), false);
    }

    public List<Codigos> getListaTipoAcciones() {
        return traer(Constantes.TIPO_ACCIONES);
    }

    public List<Codigos> getListaBancosProveedor() {
        return traer(Constantes.BANCOS);
    }

    public SelectItem[] getComboTipoReforma() {
        return Combos.getSelectItems(traer(Constantes.TIPO_REFORMA), true);
    }

    public SelectItem[] getComboFormaDePago() {
        return Combos.getSelectItems(traer(Constantes.FORMA_DE_PAGO), true);
    }

    public SelectItem[] getComboTipoArchivos() {
//        List<Codigos> lista = traer(Constantes.TIPO_ARCHIVOS);
//        return Combos.comboToStrings(lista, false);
        return null;

    }

    public SelectItem[] getComboCompetencias() {
        return Combos.getSelectItems(traer(Constantes.COMPETENCIAS), true);
    }

    public SelectItem[] getComboMaestro(String maestro) {
        List<Codigos> listaCodigos = traer(maestro);

        SelectItem[] items = new SelectItem[listaCodigos.size()];
        int i = 0;
        for (Codigos x : listaCodigos) {
            items[i++] = new SelectItem(x.getCodigo(), x.toString());
        }
        return items;
    }

    public Codigos traerCodigo(String maestro, String codigo) {
        try {
            return ejbCodigos.traerCodigo(maestro, codigo);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(CodigosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public List<Codigos> traerCodigoParametros(String maestro, String parametro) {
        try {
            return ejbCodigos.traerCodigosParametros(maestro, parametro);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(CodigosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public List<Codigos> traerCodigoMaestro(String maestro) {
        try {
            return ejbCodigos.traerCodigos(maestro);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(CodigosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public List<Codigos> traerCodigoOrdenadoCodigo(String maestro) {
        try {
            return ejbCodigos.traerCodigosOrdenadoCodigo(maestro);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(CodigosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Codigos traerCodigoNombre(String maestro, String nombre) {
        try {
            return ejbCodigos.traerCodigosNombre(maestro, nombre);
        } catch (ConsultarException ex) {
            MensajesErrores.fatal(ex.getMessage());
            Logger.getLogger(CodigosBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void cambiaCodigos(ValueChangeEvent event) {
        codigo = null;
        if (codigosAutocompletar == null) {
            return;
        }
        String newWord = (String) event.getNewValue();
        for (Codigos c : codigosAutocompletar) {
            if (c.getNombre().compareToIgnoreCase(newWord) == 0) {
                codigo = c;
                return;
            }
        }
    }

    public void codigosChangeEventHandler(TextChangeEvent event) {

        // get the new value typed by component user.
        String newWord = (String) event.getNewValue();
        Map parametros = new HashMap();
        String where = " o.activo=true and  o.maestro.codigo=:maestroParametro";
        where += " and  upper(o.nombre) like :codigo";
        parametros.put("codigo", "%" + newWord.toUpperCase() + "%");
        parametros.put("maestroParametro", Constantes.BANCOS);
        parametros.put(";where", where);
        parametros.put(";orden", " o.nombre");
        int total = 15;
        // Contadores

        parametros.put(";inicial", 0);
        parametros.put(";final", total);
        try {
            codigosAutocompletar = ejbCodigos.encontarParametros(parametros);
        } catch (ConsultarException ex) {
            MensajesErrores.error(ex.getMessage() + ":" + ex.getCause());
            Logger.getLogger("ERROR").log(Level.SEVERE, null, ex);
        }
    }

//    public void cambiaCodigo(ValueChangeEvent event) {
//        //clasificador = null;
//
//        if (event.getComponent() instanceof SelectInputText) {
//            // get the number of displayable records from the component
//            SelectInputText autoComplete
//                    = (SelectInputText) event.getComponent();
//            // get the new value typed by component user.
//            String newWord = (String) event.getNewValue();
//            //        getEmpresasBeans().setComercial("");
//            if ((newWord == null) || (newWord.isEmpty())) {
//                codigo = null;
//            }
//            List<Codigos> aux = new LinkedList<>();
//            // traer la consulta
//            Map parametros = new HashMap();
//            String where = " o.activo=true and  o.maestro.codigo=:maestroParametro";
//            where += " and  upper(o.nombre) like :codigo";
//            parametros.put("codigo", "%" + newWord.toUpperCase() + "%");
//            parametros.put("maestroParametro", Constantes.BANCOS);
//            parametros.put(";where", where);
//            parametros.put(";orden", " o.nombre");
//            int total = ((SelectInputText) event.getComponent()).getRows();
//            // Contadores
//
//            parametros.put(";inicial", 0);
//            parametros.put(";final", total);
//            try {
//                aux = ejbCodigos.encontarParametros(parametros);
//            } catch (ConsultarException ex) {
//                MensajesErrores.error(ex.getMessage() + ":" + ex.getCause());
//                Logger.getLogger("ERROR").log(Level.SEVERE, null, ex);
//            }
//            codigosAutocompletar = new ArrayList();
//            for (Codigos e : aux) {
//                SelectItem s = new SelectItem(e, e.getNombre());
//                codigosAutocompletar.add(s);
//            }
//            if (autoComplete.getSelectedItem() != null) {
//                codigo = (Codigos) autoComplete.getSelectedItem().getValue();
//            } else {
//
//                Codigos tmp = null;
//                for (int i = 0, max = codigosAutocompletar.size(); i < max; i++) {
//                    SelectItem e = (SelectItem) codigosAutocompletar.get(i);
//                    if (e.getLabel().compareToIgnoreCase(newWord) == 0) {
//                        tmp = (Codigos) e.getValue();
//                    }
//
//                }
//                if (tmp != null) {
//                    codigo = tmp;
//                }
//            }
//
//        }
//    }
    /**
     * @return the formularioBuscar
     */
    public Formulario getFormularioBuscar() {
        return formularioBuscar;
    }

    /**
     * @param formularioBuscar the formularioBuscar to set
     */
    public void setFormularioBuscar(Formulario formularioBuscar) {
        this.formularioBuscar = formularioBuscar;
    }

    public String fActiva() {
        formularioBuscar.insertar();
        codigos = traer(Constantes.BANCOS);
        return null;
    }

    /**
     * @return the codigosAutocompletar
     */
    public List<Codigos> getCodigosAutocompletar() {
        return codigosAutocompletar;
    }

    /**
     * @param codigosAutocompletar the codigosAutocompletar to set
     */
    public void setCodigosAutocompletar(List<Codigos> codigosAutocompletar) {
        this.codigosAutocompletar = codigosAutocompletar;
    }

    public SelectItem[] getComboGenero() {
        return Combos.getSelectItems(traer(Constantes.GENERO), true);
    }

    public SelectItem[] getComboGrpSanguineo() {
        return Combos.getSelectItems(traer(Constantes.GRUPO_SANGUINEO), true);
    }

//    //Estado Civil
    public SelectItem[] getComboEstadoCivil() {
        return Combos.getSelectItems(traer(Constantes.ESTADO_CIVIL), true);
    }

    public SelectItem[] getComboTipoActa() {
        return Combos.getSelectItems(traer(Constantes.ACTAS), true);
    }
//    //
//   

    public SelectItem[] getComboPatentezco() {
        return Combos.getSelectItems(traer(Constantes.PARENTESCO), true);
    }

    //Tipos de Contrato
    public SelectItem[] getComboTipoContrato() {
        return Combos.getSelectItems(traer(Constantes.TIPODE_DE_CONTRATO), true);
    }

    //Asientos Tipo
    public SelectItem[] getComboAsientosTipo() {
        return Combos.getSelectItems(traer(Constantes.ASIENTOS_TIPO), true);
    }

    public Codigos getSustentoDefault() {
        List<Codigos> lista = traerCodigoParametros(Constantes.SUSTENTOS, "default");
        for (Codigos c : lista) {
            return c;
        }
        return null;
    }

    public Codigos getSustentoActivos() {
        List<Codigos> lista = traerCodigoParametros(Constantes.SUSTENTOS, "activos");
        for (Codigos c : lista) {
            return c;
        }
        return null;
    }

    public Codigos getCuentasMultas() {
        List<Codigos> lista = traer(Constantes.CUENTAS_MULTAS);
        for (Codigos c : lista) {
            return c;
        }
        return null;
    }

    /**
     * @return the nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
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
     * @return the codigos
     */
    public List<Codigos> getCodigos() {
        return codigos;
    }

    /**
     * @param codigos the codigos to set
     */
    public void setCodigos(List<Codigos> codigos) {
        this.codigos = codigos;
    }

    /**
     * @return the codigo
     */
    public Codigos getCodigo() {
        return codigo;
    }

    /**
     * @param codigo the codigo to set
     */
    public void setCodigo(Codigos codigo) {
        this.codigo = codigo;
    }

    /**
     * @return the maestro
     */
    public Integer getMaestro() {
        return maestro;
    }

    /**
     * @param maestro the maestro to set
     */
    public void setMaestro(Integer maestro) {
        this.maestro = maestro;
    }

    /**
     * @return the maestroEntidad
     */
    public Maestros getMaestroEntidad() {
        return maestroEntidad;
    }

    /**
     * @param maestroEntidad the maestroEntidad to set
     */
    public void setMaestroEntidad(Maestros maestroEntidad) {
        this.maestroEntidad = maestroEntidad;
    }

    /**
     * @return the codigoMaestro
     */
    public String getCodigoMaestro() {
        return codigoMaestro;
    }

    /**
     * @param codigoMaestro the codigoMaestro to set
     */
    public void setCodigoMaestro(String codigoMaestro) {
        this.codigoMaestro = codigoMaestro;
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
