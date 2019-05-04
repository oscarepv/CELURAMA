/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.utilitarios;

/**
 *
 * @author edwin
 */
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.faces.application.Resource;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.icefaces.impl.push.http.DynamicResource.Options;

/**
 *
 * @author paulc
 */
public class Reportesds extends Resource implements Serializable {

    private final String resourceName;
    private HashMap<String, String> headers;
    private String path = "";
    private byte[] bytes;
    private final Date lastModified = null;
    private Map param;
    private String archivo;
    private final List ds;

    public Reportesds(Map param, String archivo, List ds, String resourceName) {
        this.param = param;
        this.archivo = archivo;
        this.ds = ds;
        this.resourceName = resourceName;
    }

//    @Override
    public InputStream open() throws IOException {
        try {

            return new ByteArrayInputStream(this.iceFacesReporte());

        } catch (JRException e) {
            MensajesErrores.fatal(e.getMessage());
            Logger.getLogger("Reportes").log(Level.SEVERE, null, e);
        }

        return null;
    }

    public byte[] iceFacesReporte() throws JRException {

//        JasperPrint jasperPrint = new JasperPrint();
        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(ds);
        JasperPrint jasperPrint = JasperFillManager.fillReport(archivo, param, beanCollectionDataSource);

//        jasperPrint.set
        byte[] fichero = JasperExportManager.exportReportToPdf(jasperPrint);
        return fichero;
    }

    public String calculateDigest() {
        return resourceName;
    }

    public Date lastModified() {
        return lastModified;
    }

    public void withOptions(Options arg0) throws IOException {

    }

    /**
     * @return the param
     */
    public Map getParam() {
        return param;
    }

    /**
     * @param param the param to set
     */
    public void setParam(Map param) {
        this.param = param;
    }

    /**
     * @return the archivo
     */
    public String getArchivo() {
        return archivo;
    }

    /**
     * @param archivo the archivo to set
     */
    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    @Override
    public InputStream getInputStream() {

        try {
            return new ByteArrayInputStream(iceFacesReporte());
        } catch (JRException ex) {
            Logger.getLogger(Reportesds.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String getRequestPath() {
        return path;
    }

    public void setRequestPath(String path) {
        this.path = path;
    }

    @Override
    public Map<String, String> getResponseHeaders() {
        return headers;
    }

    @Override
    public URL getURL() {
        return null;
    }

    @Override
    public boolean userAgentNeedsUpdate(FacesContext context) {
        return false;
    }
}
