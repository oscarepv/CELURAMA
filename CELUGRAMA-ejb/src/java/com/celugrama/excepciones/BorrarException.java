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
public class BorrarException extends Exception {

    public BorrarException(String message, Throwable cause) {
        super("Error al ejecutar el borrado :\n" + message, cause);
    }

    public BorrarException(String message) {
        super("Error al ejecutar el borrado :\n" + message);
    }
}
