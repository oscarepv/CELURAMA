/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.excepciones;

import javax.ejb.ApplicationException;

/**
 *
 * @author maquina01
 */
@ApplicationException(rollback = true)
public class InsertarException extends Exception{
    public InsertarException(String message, Throwable cause) {
        super("Error al ejecutar la inserción del Registro  :\n" + message, cause);
    }

    public InsertarException(String message) {
        super("Error al ejecutar la inserción del Registro  :\n" + message);
    }
    
    
}
