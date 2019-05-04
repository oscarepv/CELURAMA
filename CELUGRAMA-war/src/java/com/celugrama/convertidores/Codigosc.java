/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.convertidores;

import com.celugrama.entidades.Codigos;
import com.celugrama.excepciones.ConsultarException;
import com.celugrama.seguridad.CodigosBean;
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
@FacesConverter(forClass = Codigos.class)
public class Codigosc implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        Codigos codigo = null;
        try {
            if (value == null) {
                return null;
            }
            CodigosBean bean = (CodigosBean) context.getELContext().getELResolver().
                    getValue(context.getELContext(), null, "codigosCelugrama");
            Integer id = Integer.parseInt(value);
            codigo = bean.traer(id);
        } catch (ConsultarException ex) {
            Logger.getLogger(Codigosc.class.getName()).log(Level.SEVERE, null, ex);
        }
        return codigo;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Codigos codigo = (Codigos) value;
        if (codigo.getId() == null) {
            return "0";
        }
        return ((Codigos) value).getId().toString();
    }
}
