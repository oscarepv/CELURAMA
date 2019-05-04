/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.utilitarios;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author luisfernando
 */
public class MensajesErrores {

    public static void error(String mensaje) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage message = new FacesMessage();
        message.setSeverity(FacesMessage.SEVERITY_ERROR);
        message.setSummary(mensaje);
        message.setDetail(mensaje);
        context.addMessage("ERROR", message);
    }

    public static void fatal(String mensaje) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage message = new FacesMessage();
        message.setSeverity(FacesMessage.SEVERITY_FATAL);
        message.setSummary(mensaje);
        message.setDetail(mensaje);
        context.addMessage("FATAL", message);
    }

    public static void informacion(String mensaje) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage message = new FacesMessage();
        message.setSeverity(FacesMessage.SEVERITY_INFO);
        message.setSummary(mensaje);
        message.setDetail(mensaje);
        context.addMessage("INFORMACION", message);
    }

    public static void advertencia(String mensaje) {
        FacesContext context = FacesContext.getCurrentInstance();
        FacesMessage message = new FacesMessage();
        message.setSeverity(FacesMessage.SEVERITY_WARN);
        message.setSummary(mensaje);
        message.setDetail(mensaje);
        context.addMessage("ADVERTENCIA", message);
    }
}
