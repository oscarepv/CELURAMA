/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.convertidores;

import com.celugrama.entidades.Maestros;
import com.celugrama.excepciones.ConsultarException;
import com.celugrama.seguridad.MaestrosBean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author sigi-iepi
 */
@FacesConverter(forClass = Maestros.class)
public class Maestrosc implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Maestros codigo = null;
        try {
            if (value == null) {
                return null;
            }
            MaestrosBean bean = (MaestrosBean) context.getELContext().getELResolver().
                    getValue(context.getELContext(), null, "maestrosCelugrama");
            Integer id = Integer.parseInt(value);
            codigo = bean.traer(id);
        } catch (ConsultarException ex) {
            Logger.getLogger(Maestrosc.class.getName()).log(Level.SEVERE, null, ex);
        }
        return codigo;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Maestros codigo = (Maestros) value;
        if (codigo.getId() == null) {
            return "0";
        }
        return ((Maestros) value).getId().toString();
    }
}
