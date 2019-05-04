/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.utilitarios;

//import static ec.gob.registrocivil.siab.utilitarios.Reportesds.exportReportToPdf;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.faces.application.Resource;
import javax.faces.context.FacesContext;

/**
 *
 * @author oracle
 */
public class Recurso extends Resource implements java.io.Serializable {

    private String path = "";
    private final HashMap<String, String> headers;
    private final byte[] bytes;

    public Recurso(byte[] bytes) {
        this.bytes = bytes;
        this.headers = new HashMap<>();
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(this.bytes);
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
