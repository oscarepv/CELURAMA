/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.utilitarios;

import java.io.Serializable;
import javax.faces.component.UIData;

/**
 *
 * @author luisfernando
 */
public class Formulario implements Serializable {

    private static final long serialVersionUID = 1L;
    private boolean mostrar;
    private boolean nuevo;
    private boolean modificar;
    private boolean borrar;
    private UIData fila;
    private int indiceFila;
    private String pantalla = "csv";

    //    private DataExporter dataExporter= new DataExporter();
    public Formulario() {
        nuevo = false;
        modificar = false;
        borrar = false;
        mostrar = false;
//        fila=new UIData();
    }

    public void insertar() {
        nuevo = true;
        modificar = false;
        borrar = false;
        mostrar = true;
    }

    public void editar() {
        nuevo = false;
        modificar = true;
        borrar = false;
        mostrar = true;
    }

    public void eliminar() {
        nuevo = false;
        modificar = false;
        borrar = true;
        mostrar = true;
    }

    /**
     * @return the mostrar
     */
    public boolean isMostrar() {
        return mostrar;
    }

    /**
     * @param mostrar the mostrar to set
     */
    public void setMostrar(boolean mostrar) {
        this.mostrar = mostrar;
    }

    /**
     * @return the nuevo
     */
    public boolean isNuevo() {
        return nuevo;
    }

    /**
     * @param nuevo the nuevo to set
     */
    public void setNuevo(boolean nuevo) {
        this.nuevo = nuevo;
    }

    /**
     * @return the modificar
     */
    public boolean isModificar() {
        return modificar;
    }

    /**
     * @param modificar the modificar to set
     */
    public void setModificar(boolean modificar) {
        this.modificar = modificar;
    }

    /**
     * @return the borrar
     */
    public boolean isBorrar() {
        return borrar;
    }

    /**
     * @param borrar the borrar to set
     */
    public void setBorrar(boolean borrar) {
        this.borrar = borrar;
    }

    /**
     * @return the fila
     */
    public UIData getFila() {
        return fila;
    }

    /**
     * @param fila the fila to set
     */
    public void setFila(UIData fila) {
        this.fila = fila;
    }

    public String cancelar() {
        nuevo = false;
        modificar = false;
        borrar = false;
        mostrar = false;
        return null;
    }

    /**
     * @return the indiceFila
     */
    public int getIndiceFila() {
        return indiceFila;
    }

    /**
     * @param indiceFila the indiceFila to set
     */
    public void setIndiceFila(int indiceFila) {
        this.indiceFila = indiceFila;
    }

    /**
     * @return the pantalla
     */
    public String getPantalla() {
        return pantalla;
    }

    /**
     * @param pantalla the pantalla to set
     */
    public void setPantalla(String pantalla) {
        this.pantalla = pantalla;
    }
//    /**
//     * @return the dataExporter
//     */
//    public DataExporter getDataExporter() {
//        return dataExporter;
//    }
//
//    /**
//     * @param dataExporter the dataExporter to set
//     */
//    public void setDataExporter(DataExporter dataExporter) {
//         dataExporter.setResource(null); 
//        this.dataExporter = dataExporter;
//    }
}
