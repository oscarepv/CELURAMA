/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.utilitarios;

import com.lowagie.text.pdf.PdfPTable;

/**
 *
 * @author edwin
 */
public class AuxiliarReporte {
    private String dato;
    private float longitud;
    private int alineacion;
    private int columnas;
    private int tamanio;
    private int columnasO;
    private int fila;
    private boolean negrilla;
    private boolean color;
    private Object valor;
    private PdfPTable valor1;
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;

    public AuxiliarReporte() {
    }

    public AuxiliarReporte(String dato, Object valor) {
        this.dato = dato;
        this.valor = valor;
    }

    public AuxiliarReporte(String dato, int columnas, int alineacion, int tamanio, boolean negrilla, PdfPTable valor, int tcolumnas,int fila, boolean color) {
        this.dato = dato;
        this.columnas = columnas;
        this.alineacion = alineacion;
        this.tamanio = tamanio;
        this.negrilla = negrilla;
        this.valor1 = valor;
        this.columnasO = tcolumnas;
        this.fila = fila;
        this.color = color;
    }
    
    public AuxiliarReporte(String dato, int columnas, int alineacion, int tamanio, boolean negrilla, Object valor, int tcolumnas, int fila, boolean color) {
        this.dato = dato;
        this.columnas = columnas;
        this.alineacion = alineacion;
        this.tamanio = tamanio;
        this.negrilla = negrilla;
        this.valor = valor;
        this.columnasO = tcolumnas;
        this.fila = fila;
        this.color = color;
    }

    public boolean isColor() {
        return color;
    }

    public void setColor(boolean color) {
        this.color = color;
    }

    
    public PdfPTable getValor1() {
        return valor1;
    }

    public void setValor1(PdfPTable valor1) {
        this.valor1 = valor1;
    }

    public int getFila() {
        return fila;
    }

    public void setFila(int fila) {
        this.fila = fila;
    }
    
    
    
    
    /**
     * @return the valor
     */
    public Object getValor() {
        return valor;
    }

    /**
     * @param valor the valor to set
     */
    public void setValor(Object valor) {
        this.valor = valor;
    }

    /**
     * @return the dato
     */
    public String getDato() {
        return dato;
    }

    /**
     * @param dato the dato to set
     */
    public void setDato(String dato) {
        this.dato = dato;
    }

    /**
     * @return the longitud
     */
    public float getLongitud() {
        return longitud;
    }

    /**
     * @param longitud the longitud to set
     */
    public void setLongitud(float longitud) {
        this.longitud = longitud;
    }

    /**
     * @return the alineacion
     */
    public int getAlineacion() {
        return alineacion;
    }

    /**
     * @param alineacion the alineacion to set
     */
    public void setAlineacion(int alineacion) {
        this.alineacion = alineacion;
    }

    /**
     * @return the tamanio
     */
    public int getTamanio() {
        return tamanio;
    }

    /**
     * @param tamanio the tamanio to set
     */
    public void setTamanio(int tamanio) {
        this.tamanio = tamanio;
    }

    /**
     * @return the negrilla
     */
    public boolean isNegrilla() {
        return negrilla;
    }

    /**
     * @param negrilla the negrilla to set
     */
    public void setNegrilla(boolean negrilla) {
        this.negrilla = negrilla;
    }

    /**
     * @return the columnas
     */
    public int getColumnas() {
        return columnas;
    }

    /**
     * @param columnas the columnas to set
     */
    public void setColumnas(int columnas) {
        this.columnas = columnas;
    }
    
      public int getColumnasO() {
        return columnasO;
    }

    public void setColumnasO(int columnasO) {
        this.columnasO = columnasO;
    }
}