/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.utilitarios;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.DottedLineSeparator;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

/**
 *
 * @author luis
 */
public class DocumentoPDF {

    private final File temp;
    private final Document documento;
    private PdfPTable tabla;
    private byte[] archivo;

    public DocumentoPDF(String titulo, List<String> titulos, String usuario) throws IOException, DocumentException {
        temp = File.createTempFile("" + Calendar.getInstance().getTimeInMillis(), ".pdf");
        documento = new Document(PageSize.A4);
        // left rigth top bottom
        documento.setMargins(45, 20, 80, 30);
//        documento = new Document(PageSize.A4, 15, 15, 120, 30);
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(temp));
        writer.setPageEvent(new headerFooter(titulos, usuario, titulo));
        documento.open();
    }

    public DocumentoPDF(String titulo, String usuario) throws IOException, DocumentException {
        temp = File.createTempFile("" + Calendar.getInstance().getTimeInMillis(), ".pdf");
        documento = new Document(PageSize.A4.rotate());
        // left rigth top bottom
        documento.setMargins(45, 20, 80, 30);
//        documento = new Document(PageSize.A4, 15, 15, 120, 30);
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(temp));
        writer.setPageEvent(new headerFooterRotate(usuario, titulo));
        documento.open();
    }

    public DocumentoPDF(String titulo, String empresa, String parametros, String usuario, boolean vertical) throws IOException, DocumentException {
        temp = File.createTempFile("" + Calendar.getInstance().getTimeInMillis(), ".pdf");
        if (vertical) {
            documento = new Document(PageSize.A4);
        } else {
            documento = new Document(PageSize.A4.rotate());
        }
        // left rigth top bottom
        documento.setMargins(30, 30, 25, 25);
//        documento = new Document(PageSize.A4, 15, 15, 120, 30);
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(temp));
        if (vertical) {
            //writer.setPageEvent(new cabeceraVertical(titulo));
        } else {
            writer.setPageEvent(new cabeceraHorizontal(parametros, empresa, usuario, titulo));
        }
        documento.open();
    }

    public void agregarLinea() {
        try {
            DottedLineSeparator separator = new DottedLineSeparator();
            separator.setPercentage(59500f / 523f);
            Chunk linebreak = new Chunk(separator);
            documento.add(linebreak);
        } catch (DocumentException ex) {
            Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void finDePagina() {
        documento.newPage();
    }

    public void agregarFila(int columnas, List<String> valores, boolean cabecera) {
        if (cabecera) {
            tabla = new PdfPTable(columnas);
            tabla.setWidthPercentage(100);
            tabla.setHeaderRows(1);
            for (String v : valores) {
                tabla.addCell(encabezadoIzquierda(v, 8));
            }
        } else {
            for (String v : valores) {
                tabla.addCell(celdaIzquierda(v, 1, columnas, cabecera));
            }
        }

        try {

            documento.add(tabla);
        } catch (DocumentException ex) {
            Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void agregarTabla(List<AuxiliarReporte> titulos, List<AuxiliarReporte> valores, int tamanio, int porcentaje, String tituloTabla, int tabla) {
        int filasTitulo = 1;
        try {
            if (titulos == null) {
                titulos = new LinkedList<>();
            }
            if (valores == null) {
                return;
            }
//            if (valores.isEmpty()) {
//                return;
//            }
            if (titulos.isEmpty()) {
                filasTitulo = 0;
            } else {

            }
            PdfPTable tablaInterna = new PdfPTable(tamanio);

            if (tabla == 1) {
                float[] medidaCeldas = {0.65f, 2.25f, 0.65f};
                tablaInterna.setWidths(medidaCeldas);
            } else if (tabla == 2) {
                float[] medidaCeldas = {0.40f, 0.60f};
                tablaInterna.setWidths(medidaCeldas);
            }

            tablaInterna.setWidthPercentage(porcentaje);
            tablaInterna.setHorizontalAlignment(Element.ALIGN_CENTER);

            if (!((tituloTabla == null) || (tituloTabla.isEmpty()))) {
                filasTitulo++;
                tablaInterna.setHeaderRows(2);
                tablaInterna.addCell(encabezadoTabla(tituloTabla, titulos.size()));
                documento.add(tablaInterna);
//                tablaInterna.setHeaderRows(filasTitulo);

            } else {
                tablaInterna.setHeaderRows(filasTitulo);
            }

            for (AuxiliarReporte v : titulos) {
                if (v.getColumnasO() != 0 && v.getFila() == 0) {
                    PdfPCell celdaFinal = new PdfPCell(encabezadoIzquierda((String) v.getValor(), v.getTamanio()));
                    // Indicamos cuantas columnas ocupa la celda
                    celdaFinal.setColspan(v.getColumnasO());
                    tablaInterna.addCell(celdaFinal);
                } else if (v.getColumnasO() == 0 && v.getFila() == 0) {

                    tablaInterna.addCell(encabezadoIzquierda((String) v.getValor(), v.getTamanio()));

                } else if (v.getColumnasO() == 0 && v.getFila() != 0) {
                    PdfPCell celdaFinal = new PdfPCell(encabezadoIzquierda((String) v.getValor(), v.getTamanio()));
                    celdaFinal.setRowspan(v.getFila());
                    tablaInterna.addCell(celdaFinal);
                }
                //tablaInterna.addCell(encabezadoIzquierda((String) v.getValor(), v.getTamanio()));
            }

            for (AuxiliarReporte v : valores) {
                switch (v.getDato()) {
                    case "String":

//                        if (v.getColumnasO() != 0) {
//                            PdfPCell celdaFinal = new PdfPCell(celda((String) v.getValor(), 1, true, 4, 7));
//                            // Indicamos cuantas columnas ocupa la celda
//                            celdaFinal.setColspan(v.getColumnasO());
//                            tablaInterna.addCell(celdaFinal);
//                        } else if (v.getColumnasO() == 0) {
//                            tablaInterna.addCell(celdaIzquierda((String) v.getValor()));
//
//                        }
                        if (v.getColumnasO() != 0 && v.getFila() == 0) {
                            PdfPCell celdaFinal = new PdfPCell(celda((String) v.getValor(), v.getAlineacion(), v.isNegrilla(), v.getColumnas(), v.getTamanio(), v.isColor()));
                            // Indicamos cuantas columnas ocupa la celda
                            celdaFinal.setColspan(v.getColumnasO());
                            tablaInterna.addCell(celdaFinal);
                        } else if (v.getColumnasO() == 0 && v.getFila() == 0) {

                            tablaInterna.addCell(celda((String) v.getValor(), v.getAlineacion(), v.isNegrilla(), v.getColumnas(), v.getTamanio(), v.isColor()));

                        } else if (v.getColumnasO() == 0 && v.getFila() != 0) {
                            PdfPCell celdaFinal = new PdfPCell(celda((String) v.getValor(), v.getAlineacion(), v.isNegrilla(), v.getColumnas(), v.getTamanio(), v.isColor()));
                            celdaFinal.setRowspan(v.getFila());
                            tablaInterna.addCell(celdaFinal);
                        }

                        break;
                    case "Double":
                        if (v.getValor() == null) {
                            tablaInterna.addCell(celdaIzquierda("", v.getAlineacion(), v.getTamanio(), v.isNegrilla()));
                        } else {
                            DecimalFormat df = new DecimalFormat("###,##0.00");
                            double valor = (v.getValor() == null ? 0.0 : (double) v.getValor());
                            tablaInterna.addCell(celdaDerecha(df.format(valor)));
                        }
                        break;
                    case "Date":
                        if (v.getValor() == null) {
                            tablaInterna.addCell(celdaIzquierda("", v.getAlineacion(), v.getTamanio(), v.isNegrilla()));
                        } else {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            tablaInterna.addCell(celdaIzquierda(sdf.format(v.getValor()), v.getAlineacion(), v.getTamanio(), v.isNegrilla()));
                        }
                        break;
                    case "Table":
                        tablaInterna.addCell(v.getValor1());
                        break;
                    case "Image":
                        if (v.getColumnasO() != 0 && v.getFila() == 0) {
                            PdfPCell celdaFinal = new PdfPCell(createImageCell((String) v.getValor()));

                            // Indicamos cuantas columnas ocupa la celda
                            celdaFinal.setColspan(v.getColumnasO());
                            tablaInterna.addCell(celdaFinal);
                        } else if (v.getColumnasO() == 0 && v.getFila() == 0) {
                            PdfPCell celdaFinal = new PdfPCell(createImageCell((String) v.getValor()));
                            tablaInterna.addCell(celdaFinal);

                        } else if (v.getColumnasO() == 0 && v.getFila() != 0) {
                            PdfPCell celdaFinal = new PdfPCell(createImageCell((String) v.getValor()));
                            celdaFinal.setRowspan(v.getFila());
                            celdaFinal.setHorizontalAlignment(Element.ALIGN_CENTER);
                            celdaFinal.setPadding(5f);
                            //celdaFinal.setVerticalAlignment(Element.ALIGN_CENTER);
                            tablaInterna.addCell(celdaFinal);
                        }
                        break;

                    case "image":
                        if (v.getColumnasO() != 0 && v.getFila() == 0) {
                            PdfPCell celdaFinal = new PdfPCell(createImageAnexoCell((String) v.getValor()));

                            // Indicamos cuantas columnas ocupa la celda
                            celdaFinal.setColspan(v.getColumnasO());
                            tablaInterna.addCell(celdaFinal);
                        } else if (v.getColumnasO() == 0 && v.getFila() == 0) {
                            PdfPCell celdaFinal = new PdfPCell(createImageAnexoCell((String) v.getValor()));
                            tablaInterna.addCell(celdaFinal);

                        } else if (v.getColumnasO() == 0 && v.getFila() != 0) {
                            PdfPCell celdaFinal = new PdfPCell(createImageAnexoCell((String) v.getValor()));
                            celdaFinal.setRowspan(v.getFila());
                            celdaFinal.setHorizontalAlignment(Element.ALIGN_CENTER);
                            celdaFinal.setPadding(5f);
                            //celdaFinal.setVerticalAlignment(Element.ALIGN_CENTER);
                            tablaInterna.addCell(celdaFinal);
                        }

                        break;
                    case "HTML":
                        //HTMLWorker htmlWorker = new HTMLWorker(documento);

                        try {
                            //htmlWorker.text((String) v.getValor());
                            List<Element> parseToLis = HTMLWorker.parseToList(new StringReader((String) v.getValor()), null);
                            //htmlWorker.parse(new StringReader((String) v.getValor()));
                            String valor = "", aux;
                            for (int k = 0; k < parseToLis.size(); ++k) {
                                aux = String.valueOf(parseToLis.get(k));
                                valor += aux.substring(1, aux.length() - 1);
                                if (parseToLis.size() > 1) {
                                    valor += "\n";
                                }
                            }

                            tablaInterna.addCell(celda(String.valueOf(valor), v.getAlineacion(), v.isNegrilla(), v.getColumnas(), 10, v.isColor()));

                        } catch (IOException e) {
                        }
                        break;

                }

            }
            documento.add(tablaInterna);
        } catch (DocumentException | IOException ex) {
            Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void agregarTablaTitulo(List<String> titulos, List<AuxiliarReporte> valores, int tamanio, int porcentaje, String tituloTabla, int tabla) {
        int filasTitulo = 1;
        try {
            if (titulos == null) {
                titulos = new LinkedList<>();
            }
            if (valores == null) {
                return;
            }
//            if (valores.isEmpty()) {
//                return;
//            }
            if (titulos.isEmpty()) {
                filasTitulo = 0;
            } else {

            }
            PdfPTable tablaInterna = new PdfPTable(tamanio);

            if (tabla == 1) {
                float[] medidaCeldas = {0.65f, 2.25f, 0.65f};
                tablaInterna.setWidths(medidaCeldas);
            } else if (tabla == 2) {
                float[] medidaCeldas = {0.40f, 0.60f};
                tablaInterna.setWidths(medidaCeldas);
            }

            tablaInterna.setWidthPercentage(porcentaje);
            tablaInterna.setHorizontalAlignment(Element.ALIGN_CENTER);

            if (!((tituloTabla == null) || (tituloTabla.isEmpty()))) {
                filasTitulo++;
                tablaInterna.setHeaderRows(2);
                tablaInterna.addCell(encabezadoTabla(tituloTabla, titulos.size()));
                documento.add(tablaInterna);
//                tablaInterna.setHeaderRows(filasTitulo);

            } else {
                tablaInterna.setHeaderRows(filasTitulo);
            }

            for (String v : titulos) {
                tablaInterna.addCell(encabezadoIzquierda(v, 8));
            }

            for (AuxiliarReporte v : valores) {
                switch (v.getDato()) {
                    case "String":

//                        if (v.getColumnasO() != 0) {
//                            PdfPCell celdaFinal = new PdfPCell(celda((String) v.getValor(), 1, true, 4, 7));
//                            // Indicamos cuantas columnas ocupa la celda
//                            celdaFinal.setColspan(v.getColumnasO());
//                            tablaInterna.addCell(celdaFinal);
//                        } else if (v.getColumnasO() == 0) {
//                            tablaInterna.addCell(celdaIzquierda((String) v.getValor()));
//
//                        }
                        if (v.getColumnasO() != 0 && v.getFila() == 0) {
                            PdfPCell celdaFinal = new PdfPCell(celdaTitulo((String) v.getValor(), v.getAlineacion(), v.isNegrilla(), v.getColumnas(), v.getTamanio(), v.isColor()));
                            // Indicamos cuantas columnas ocupa la celda
                            celdaFinal.setColspan(v.getColumnasO());
                            tablaInterna.addCell(celdaFinal);
                        } else if (v.getColumnasO() == 0 && v.getFila() == 0) {

                            tablaInterna.addCell(celdaTitulo((String) v.getValor(), v.getAlineacion(), v.isNegrilla(), v.getColumnas(), v.getTamanio(), v.isColor()));

                        } else if (v.getColumnasO() == 0 && v.getFila() != 0) {
                            PdfPCell celdaFinal = new PdfPCell(celdaTitulo((String) v.getValor(), v.getAlineacion(), v.isNegrilla(), v.getColumnas(), v.getTamanio(), v.isColor()));
                            celdaFinal.setRowspan(v.getFila());
                            tablaInterna.addCell(celdaFinal);
                        }

                        break;
                    case "Double":
                        if (v.getValor() == null) {
                            tablaInterna.addCell(celdaIzquierda("", v.getAlineacion(), v.getTamanio(), v.isNegrilla()));
                        } else {
                            DecimalFormat df = new DecimalFormat("###,##0.00");
                            double valor = (v.getValor() == null ? 0.0 : (double) v.getValor());
                            tablaInterna.addCell(celdaDerecha(df.format(valor)));
                        }
                        break;
                    case "Date":
                        if (v.getValor() == null) {
                            tablaInterna.addCell(celdaIzquierda("", v.getAlineacion(), v.getTamanio(), v.isNegrilla()));
                        } else {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            tablaInterna.addCell(celdaIzquierda(sdf.format(v.getValor()), v.getAlineacion(), v.getTamanio(), v.isNegrilla()));
                        }
                        break;
                    case "Table":
                        tablaInterna.addCell(v.getValor1());
                        break;
                    case "Image":
                        if (v.getColumnasO() != 0 && v.getFila() == 0) {
                            PdfPCell celdaFinal = new PdfPCell(createImageCell((String) v.getValor()));

                            // Indicamos cuantas columnas ocupa la celda
                            celdaFinal.setColspan(v.getColumnasO());
                            tablaInterna.addCell(celdaFinal);
                        } else if (v.getColumnasO() == 0 && v.getFila() == 0) {
                            PdfPCell celdaFinal = new PdfPCell(createImageCell((String) v.getValor()));
                            tablaInterna.addCell(celdaFinal);

                        } else if (v.getColumnasO() == 0 && v.getFila() != 0) {
                            PdfPCell celdaFinal = new PdfPCell(createImageCell((String) v.getValor()));
                            celdaFinal.setRowspan(v.getFila());
                            celdaFinal.setHorizontalAlignment(Element.ALIGN_CENTER);
                            celdaFinal.setPadding(5f);
                            //celdaFinal.setVerticalAlignment(Element.ALIGN_CENTER);
                            tablaInterna.addCell(celdaFinal);
                        }
                        break;

                    case "image":
                        if (v.getColumnasO() != 0 && v.getFila() == 0) {
                            PdfPCell celdaFinal = new PdfPCell(createImageAnexoCell((String) v.getValor()));

                            // Indicamos cuantas columnas ocupa la celda
                            celdaFinal.setColspan(v.getColumnasO());
                            tablaInterna.addCell(celdaFinal);
                        } else if (v.getColumnasO() == 0 && v.getFila() == 0) {
                            PdfPCell celdaFinal = new PdfPCell(createImageAnexoCell((String) v.getValor()));
                            tablaInterna.addCell(celdaFinal);

                        } else if (v.getColumnasO() == 0 && v.getFila() != 0) {
                            PdfPCell celdaFinal = new PdfPCell(createImageAnexoCell((String) v.getValor()));
                            celdaFinal.setRowspan(v.getFila());
                            celdaFinal.setHorizontalAlignment(Element.ALIGN_CENTER);
                            celdaFinal.setPadding(5f);
                            //celdaFinal.setVerticalAlignment(Element.ALIGN_CENTER);
                            tablaInterna.addCell(celdaFinal);
                        }

                        break;
                    case "HTML":
                        //HTMLWorker htmlWorker = new HTMLWorker(documento);

                        try {
                            //htmlWorker.text((String) v.getValor());
                            List<Element> parseToLis = HTMLWorker.parseToList(new StringReader((String) v.getValor()), null);
                            //htmlWorker.parse(new StringReader((String) v.getValor()));
                            String valor = "", aux;
                            for (int k = 0; k < parseToLis.size(); ++k) {
                                aux = String.valueOf(parseToLis.get(k));
                                valor += aux.substring(1, aux.length() - 1);
                                if (parseToLis.size() > 1) {
                                    valor += "\n";
                                }
                            }

                            tablaInterna.addCell(celda(valor, v.getAlineacion(), v.isNegrilla(), v.getColumnas(), 10, v.isColor()));

                        } catch (IOException e) {
                        }
                        break;

                }

            }
            documento.add(tablaInterna);
        } catch (DocumentException | IOException ex) {
            Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static PdfPCell createImageCell(String path) throws DocumentException, IOException {
        Image img = Image.getInstance(FacesContext.getCurrentInstance().getExternalContext().getRealPath(path));

        //Image img = Image.getInstance(path);
        img.scaleToFit(80f, 80f);
//        img.scaleAbsolute(80f, 70f);
        //img.setRotationDegrees(45f);
        //img.setAlignment(Element.ALIGN_CENTER);
        //img.setAbsolutePosition(150f, 150f);
        PdfPCell cell = new PdfPCell(img);
        cell.setBorder(0);
        cell.setBorderWidthTop(0.5f);
        cell.setBorderWidthLeft(0.5f);
        cell.setBorderWidthBottom(0.5f);

        return cell;
    }

    private static PdfPCell createImageAnexoCell(String directorio) throws DocumentException, IOException {

        Image img = Image.getInstance(directorio);
        img.setAlignment(Element.ALIGN_CENTER);
        img.scaleAbsoluteHeight(300f);
        img.scaleAbsoluteWidth(400f);
//        img.scaleToFit(400f, 500f);
//        img.scaleAbsolute(200f, 200f);
        //img.scalePercent(100);
        //img.setRotationDegrees(45f);
        //img.setAbsolutePosition(150f, 150f);
        PdfPCell cell = new PdfPCell(img);
        cell.setBorder(0);
        return cell;
    }

    //Retornar tabla
    public PdfPTable retornarTabla(List<AuxiliarReporte> titulos, List<AuxiliarReporte> valores, int tamanio, int porcentaje, String tituloTabla) throws DocumentException, IOException {
        int filasTitulo = 1;
        if (titulos == null) {
            titulos = new LinkedList<>();
        }
        if (valores == null) {
            return null;
        }
        if (valores.isEmpty()) {
            return null;
        }
        if (titulos.isEmpty()) {
            filasTitulo = 0;
        } else {

        }
        PdfPTable tablaInterna = new PdfPTable(tamanio);
        tablaInterna.setWidthPercentage(porcentaje);
        tablaInterna.setHorizontalAlignment(Element.ALIGN_LEFT);
        if (!((tituloTabla == null) || (tituloTabla.isEmpty()))) {
            filasTitulo++;
            tablaInterna.setHeaderRows(2);
            tablaInterna.addCell(encabezadoTabla(tituloTabla, titulos.size()));
            return tablaInterna;
//                tablaInterna.setHeaderRows(filasTitulo);

        } else {
            tablaInterna.setHeaderRows(filasTitulo);
        }

        for (AuxiliarReporte v : titulos) {
            tablaInterna.addCell(encabezadoIzquierda(String.valueOf(v.getValor()), v.getTamanio()));
        }
        for (AuxiliarReporte v : valores) {
            switch (v.getDato()) {
                case "String":

                    if (v.getColumnasO() != 0 && v.getFila() == 0) {
                        PdfPCell celdaFinal = new PdfPCell(celda((String) v.getValor(), v.getAlineacion(), v.isNegrilla(), 4, v.getTamanio(), v.isColor()));
                        // Indicamos cuantas columnas ocupa la celda
                        celdaFinal.setColspan(v.getColumnasO());
                        tablaInterna.addCell(celdaFinal);
                    } else if (v.getColumnasO() == 0 && v.getFila() == 0) {
                        tablaInterna.addCell(celdaIzquierda((String) v.getValor(), v.getAlineacion(), 10, v.isNegrilla()));

                    } else if (v.getColumnasO() == 0 && v.getFila() != 0) {
                        PdfPCell celdaFinal = new PdfPCell(celda((String) v.getValor(), v.getAlineacion(), v.isNegrilla(), 4, v.getTamanio(), v.isColor()));
                        celdaFinal.setRowspan(v.getFila());
                        tablaInterna.addCell(celdaFinal);
                    }

                    break;
                case "Double":
                    if (v.getValor() == null) {
                        tablaInterna.addCell(celdaIzquierda("", v.getAlineacion(), v.getTamanio(), v.isNegrilla()));
                    } else {
                        DecimalFormat df = new DecimalFormat("###,##0.00");
                        double valor = (v.getValor() == null ? 0.0 : (double) v.getValor());
                        tablaInterna.addCell(celdaDerecha(df.format(valor)));
                    }
                    break;
                case "Date":
                    if (v.getValor() == null) {
                        tablaInterna.addCell(celdaIzquierda("", v.getAlineacion(), v.getTamanio(), v.isNegrilla()));
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        tablaInterna.addCell(celdaIzquierda(sdf.format(v.getValor()), v.getAlineacion(), v.getTamanio(), v.isNegrilla()));
                    }
                    break;
                case "image":
                    if (v.getColumnasO() != 0 && v.getFila() == 0) {
                        PdfPCell celdaFinal = new PdfPCell(createImageAnexoCell((String) v.getValor()));
                        celdaFinal.setPadding(50f);
                        celdaFinal.setHorizontalAlignment(Element.ALIGN_CENTER);
                        // Indicamos cuantas columnas ocupa la celda
                        celdaFinal.setColspan(v.getColumnasO());
                        tablaInterna.addCell(celdaFinal);
                    } else if (v.getColumnasO() == 0 && v.getFila() == 0) {
                        PdfPCell celdaFinal = new PdfPCell(createImageAnexoCell((String) v.getValor()));
                        tablaInterna.addCell(celdaFinal);

                    } else if (v.getColumnasO() == 0 && v.getFila() != 0) {
                        PdfPCell celdaFinal = new PdfPCell(createImageAnexoCell((String) v.getValor()));
                        celdaFinal.setRowspan(v.getFila());
                        celdaFinal.setHorizontalAlignment(Element.ALIGN_CENTER);
                        celdaFinal.setPadding(50f);
                        //celdaFinal.setVerticalAlignment(Element.ALIGN_CENTER);
                        tablaInterna.addCell(celdaFinal);
                    }

                    break;
                case "HTML":
                    //HTMLWorker htmlWorker = new HTMLWorker(documento);

                    try {
                        //htmlWorker.text((String) v.getValor());
                        List<Element> parseToLis = HTMLWorker.parseToList(new StringReader((String) v.getValor()), null);
                        //htmlWorker.parse(new StringReader((String) v.getValor()));
                        String valor = "", aux;
                        for (int k = 0; k < parseToLis.size(); ++k) {
                            aux = String.valueOf(parseToLis.get(k));
                            valor += aux.substring(1, aux.length() - 1);
                            if (parseToLis.size() > 1) {
                                valor += "\n";
                            }
                        }

//                        tablaInterna.addCell(celda(String.valueOf(valor), v.getAlineacion(), v.isNegrilla(), v.getColumnas(), 10, v.isColor()));
                        if (v.getColumnasO() != 0 && v.getFila() == 0) {
                            PdfPCell celdaFinal = new PdfPCell(celda(valor, v.getAlineacion(), v.isNegrilla(), v.getColumnas(), 10, v.isColor()));
                            // Indicamos cuantas columnas ocupa la celda
                            celdaFinal.setColspan(v.getColumnasO());
                            tablaInterna.addCell(celdaFinal);
                        } else if (v.getColumnasO() == 0 && v.getFila() == 0) {
                            tablaInterna.addCell(celdaIzquierda((String) valor, AuxiliarReporte.ALIGN_LEFT, 10, v.isNegrilla()));

                        } else if (v.getColumnasO() == 0 && v.getFila() != 0) {
                            PdfPCell celdaFinal = new PdfPCell(celda(valor, v.getAlineacion(), v.isNegrilla(), v.getColumnas(), 10, v.isColor()));
                            celdaFinal.setRowspan(v.getFila());
                            tablaInterna.addCell(celdaFinal);
                        }

                    } catch (IOException e) {
                    }

//                case "check":
//                    if (v.getValor() == "OK") {
//                        tablaInterna.addCell(celda("SI", v.getAlineacion(), v.isNegrilla(), v.getColumnas(), 10, v.isColor()));
//
//                    } else if (v.getValor() == "NO") {
//                        tablaInterna.addCell(celda("NO", v.getAlineacion(), v.isNegrilla(), v.getColumnas(), 10, v.isColor()));
//
//                    }
//
//                    break;
            }
        }
        return tablaInterna;
    }

    public void agregarTablaReporte(List<AuxiliarReporte> titulos, List<AuxiliarReporte> valores,
            int tamanio, int porcentaje, String tituloTabla) {
        int filasTitulo = 1;
        try {
            if (titulos == null) {
                titulos = new LinkedList<>();
            }
            if (valores == null) {
                return;
            }
            if (valores.isEmpty()) {
                return;
            }
            if (titulos.isEmpty()) {
                filasTitulo = 0;
            } else {

            }
            float[] anchoColumnas = new float[tamanio];
            int i = 0;
            for (AuxiliarReporte v : titulos) {
                anchoColumnas[i++] = v.getColumnas();
            }
            PdfPTable tablaInterna = new PdfPTable(anchoColumnas);
//            tablaInterna.setTotalWidth(documento.getPageSize().getWidth() - 80);
//            tablaInterna.setLockedWidth(true);
            tablaInterna.setWidthPercentage(porcentaje);
            tablaInterna.setHorizontalAlignment(Element.ALIGN_LEFT);

            if (!((tituloTabla == null) || (tituloTabla.isEmpty()))) {
                filasTitulo++;
                tablaInterna.setHeaderRows(2);
                tablaInterna.addCell(encabezadoTabla(tituloTabla, titulos.size()));
                documento.add(tablaInterna);

            } else {
                tablaInterna.setHeaderRows(filasTitulo);
            }

            for (AuxiliarReporte v : titulos) {
                tablaInterna.addCell(encabezado((String) v.getValor(), v.getAlineacion(), v.isNegrilla(), v.getColumnas(), v.getTamanio()));
            }
            for (AuxiliarReporte v : valores) {
                switch (v.getDato()) {
                    case "String":
                        tablaInterna.addCell(celda((String) v.getValor(), v.getAlineacion(), v.isNegrilla(), v.getColumnas(), v.getTamanio(), v.isColor()));
                        break;
                    case "Double":
                        if (v.getValor() == null) {
                            tablaInterna.addCell(celdaIzquierda("", v.getAlineacion(), v.getTamanio(), v.isNegrilla()));
                        } else {
                            DecimalFormat df = new DecimalFormat("###,##0.00");
                            double valor = (v.getValor() == null ? 0.0 : (double) v.getValor());
                            tablaInterna.addCell(celda(df.format(valor), v.getAlineacion(), v.isNegrilla(), v.getColumnas(), v.getTamanio(), v.isColor()));
                        }
                        break;
                    case "Date":
                        if (v.getValor() == null) {
                            tablaInterna.addCell(celdaIzquierda("", v.getAlineacion(), v.getTamanio(), v.isNegrilla()));
                        } else {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            tablaInterna.addCell(celda(sdf.format(v.getValor()), v.getAlineacion(), v.isNegrilla(), v.getColumnas(), v.getTamanio(), v.isColor()));
                        }
                        break;
                }
            }

            documento.add(tablaInterna);
        } catch (DocumentException ex) {
            Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Recurso traerRecurso() throws IOException, DocumentException {
        documento.close();
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        return new Recurso(Files.readAllBytes(Paths.get(temp.getAbsolutePath())));
//        return new Recurso(ec, temp.getName(), Files.readAllBytes(Paths.get(temp.getAbsolutePath())));
    }

    private PdfPCell encabezadoIzquierda(String texto, int tamanio) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, new Font(Font.TIMES_ROMAN, tamanio, Font.BOLD)));
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setBorderColor(Color.GRAY);
        //celda.setBorderColor(Color.WHITE);
        celda.setBorder(0);
        celda.setBorderWidthTop(0.5f);
        celda.setBorderWidthLeft(0.5f);
        celda.setBorderWidthRight(0.5f);
        celda.setBorderWidthBottom(0.5f);
        return celda;
    }

    public void agregaTitulo(String texto) {
        Paragraph linea = new Paragraph(texto, new Font(Font.TIMES_ROMAN, 12, Font.BOLD));
        linea.setAlignment(Element.ALIGN_CENTER);
        try {
            documento.add(linea);
        } catch (DocumentException ex) {
            Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void agregaTituloIzquierda(String texto) {
        Paragraph linea = new Paragraph(texto, new Font(Font.TIMES_ROMAN, 12, Font.BOLD));
        linea.setAlignment(Element.ALIGN_LEFT);
        try {
            documento.add(linea);
        } catch (DocumentException ex) {
            Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private PdfPCell encabezado(String texto, int alinear, boolean bold, int columnas, int fontSize) {
//        Paragraph p;
//        p = new Paragraph(columnas, texto);
//        p.setFont(new Font(Font.TIMES_ROMAN, 6, (bold ? Font.BOLD : Font.NORMAL)));

//        PdfPCell celda = new PdfPCell();
        PdfPCell celda = new PdfPCell(new Phrase(columnas, texto, new Font(Font.TIMES_ROMAN, fontSize, (bold ? Font.BOLD : Font.NORMAL))));
//        celda.addElement(p);
        celda.setHorizontalAlignment(alinear);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
//        celda.setColspan(columnas);
//        celda.setMinimumHeight(columnas);titulo
//        celda.setBorderColor(Color.GRAY);
        celda.setBorder(0);
        celda.setBorderWidthTop(1);
        celda.setBorderWidthBottom(1);
        return celda;
    }

    private PdfPCell encabezadoDerecha(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, new Font(Font.TIMES_ROMAN, 6, Font.BOLD)));
        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
//        celda.setBorderColor(Color.GRAY);
        celda.setBorder(0);
        celda.setBorderWidthTop(1);
        celda.setBorderWidthBottom(1);
        return celda;
    }

    private PdfPCell encabezadoTabla(String texto, int columnas) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, new Font(Font.TIMES_ROMAN, 8, Font.BOLD)));
        celda.setHorizontalAlignment(Element.ALIGN_LEFT);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setColspan(columnas);
//        celda.setBorderColor(Color.GRAY);
        celda.setBorder(0);
        celda.setBorderWidthTop(1);
        celda.setBorderWidthLeft(1);
        celda.setBorderWidthRight(1);
        celda.setBorderWidthBottom(0);
        return celda;
    }

    private PdfPCell celdaIzquierda(String texto, int alineacion, int fontSize, boolean bold) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, new Font(Font.TIMES_ROMAN, fontSize, (bold ? Font.BOLD : Font.NORMAL))));
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        //celda.setBorderColor(Color.GRAY);
        celda.setBorderColor(Color.WHITE);
        celda.setBorder(0);
        celda.setBorderWidthTop(0.5f);
        celda.setBorderWidthLeft(0.5f);
        celda.setBorderWidthRight(0.5f);
        celda.setBorderWidthBottom(0.5f);
        return celda;
    }

    private PdfPCell celdaDerecha(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, new Font(Font.TIMES_ROMAN, 8, Font.NORMAL)));
        celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        //celda.setBorderColor(Color.GRAY);
        celda.setBorderColor(Color.WHITE);
        celda.setBorder(0);
        celda.setBorderWidthTop(0.5f);
        celda.setBorderWidthLeft(0.5f);
        celda.setBorderWidthRight(0.5f);
        celda.setBorderWidthBottom(0.5f);
        return celda;
    }

    private PdfPCell celda(String texto, int alinear, boolean bold, int columnas, int fontSize, boolean color) {
        Font fuente = new Font(Font.TIMES_ROMAN, fontSize, (bold ? Font.BOLD : Font.NORMAL));
        if (color) {
            fuente.setColor(Color.RED);
        }
//        PdfPCell celda = new PdfPCell(new Paragraph(columnas, texto, fuente));
        PdfPCell celda = new PdfPCell(new Phrase(columnas, texto, fuente));
        celda.setHorizontalAlignment(alinear);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
//        celda.setColspan(columnas);
        celda.setBorderColor(Color.GRAY);
//        celda.setBorder(0);
        celda.setBorderWidthTop(0.5f);
        celda.setBorderWidthLeft(0.5f);
        celda.setBorderWidthRight(0.5f);
        celda.setBorderWidthBottom(0.5f);
        return celda;
    }

    private PdfPCell celdaTitulo(String texto, int alinear, boolean bold, int columnas, int fontSize, boolean color) {
        Font fuente = new Font(Font.TIMES_ROMAN, fontSize, (bold ? Font.BOLD : Font.NORMAL));
        if (color) {
            fuente.setColor(Color.RED);
        }

        PdfPCell celda = new PdfPCell(new Phrase(columnas, texto, fuente));
        celda.setHorizontalAlignment(alinear);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
//        celda.setColspan(columnas);
//        celda.setBorderColor(Color.GRAY);
        celda.setBorder(0);
        celda.setBorderWidthTop(0.5f);
        celda.setBorderWidthLeft(0.5f);
        celda.setBorderWidthRight(0.5f);
        celda.setBorderWidthBottom(0.5f);
        return celda;
    }

    public void agregaParrafo(String texto) {

        Paragraph linea = new Paragraph(texto, new Font(Font.TIMES_ROMAN, 6, Font.NORMAL));
        linea.setAlignment(Element.ALIGN_LEFT);
        try {
            documento.add(linea);
        } catch (DocumentException ex) {
            Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void agregaParrafoCentro(String texto) {
        Paragraph linea = new Paragraph(texto, new Font(Font.TIMES_ROMAN, 6, Font.NORMAL));
        linea.setAlignment(Element.ALIGN_CENTER);
        try {
            documento.add(linea);
        } catch (DocumentException ex) {
            Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void agregaParrafo2(String texto) {
        Paragraph linea = new Paragraph(texto, new Font(Font.TIMES_ROMAN, 9, Font.NORMAL));
        linea.setAlignment(Element.ALIGN_JUSTIFIED);
        try {
            documento.add(linea);
        } catch (DocumentException ex) {
            Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void agregaParrafo(String texto, int alineacion, int tamanio, boolean bold) throws IOException {

        try {
            List<Element> parseToLis = HTMLWorker.parseToList(new StringReader(texto), null);
            String valor = "", aux;
            for (int k = 0; k < parseToLis.size(); ++k) {
                aux = String.valueOf(parseToLis.get(k));
                valor += aux.substring(1, aux.length() - 1);
                if (parseToLis.size() > 1) {
                    valor += "\n";
                }
            }

            Paragraph linea = new Paragraph(valor, new Font(Font.HELVETICA, tamanio, (bold ? Font.BOLD : Font.NORMAL)));
            linea.setAlignment(alineacion);
            documento.add(linea);
        } catch (DocumentException ex) {
            Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void agregaParrafo(String texto, int alineacion, int tamanio, boolean bold, boolean cursiva) {
        Paragraph linea = new Paragraph(texto, new Font((cursiva ? Font.ITALIC : Font.HELVETICA), tamanio, (bold ? Font.ITALIC : Font.NORMAL)));
        linea.setAlignment(alineacion);
        try {
            documento.add(linea);
        } catch (DocumentException ex) {
            Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the archivo
     */
    public byte[] getArchivo() {
        return archivo;
    }

    /**
     * @param archivo the archivo to set
     */
    public void setArchivo(byte[] archivo) {
        this.archivo = archivo;
    }

    class headerFooter extends PdfPageEventHelper {

        PdfTemplate t;
        Image total;
        List<String> titulos;
        String usuario;
        String titulo;

        public headerFooter(List<String> titulos, String usuario, String titulo) {
            this.titulos = titulos;
            this.usuario = usuario;
            this.titulo = titulo;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            t = writer.getDirectContent().createTemplate(0, 0);
            try {
                total = Image.getInstance(t);
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {

            try {
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.setWidths(new int[]{50, 530, 105});
                table.setTotalWidth(520);
                table.setLockedWidth(true);
                table.getDefaultCell().setFixedHeight(20);
//                table.getDefaultCell().setBorder(Rectangle.BOTTOM);
                Image izq = Image.getInstance(FacesContext.getCurrentInstance().getExternalContext().getRealPath("resources/images/logo.png"));
//                Image der = Image.getInstance(FacesContext.getCurrentInstance().getExternalContext().getRealPath("resources/imagenes/escuela.png"));
                izq.scaleAbsolute(45f, 45f);
//                izq.scaleAbsolute(106f, 54f);
//                izq.scaleAbsolute(45f, 22f);

                PdfPCell cell = new PdfPCell(izq);
                cell.setBorder(0);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(titulo, new Font(Font.TIMES_ROMAN, 10, Font.BOLD)));
                cell.setBorder(0);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

                table.addCell(cell);

//                cell = new PdfPCell(der);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                cell = new PdfPCell(new Phrase("FECHA1 : " + sdf.format(new Date()), new Font(Font.TIMES_ROMAN, 6, Font.BOLD)));
                cell.setBorder(0);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);
                table.writeSelectedRows(0, -1, 30, 820, writer.getDirectContent());
//                table.writeSelectedRows(0, -1, 30, 803, writer.getDirectContent());
//                for (String s:titulos){
//                     document.add(new Paragraph(s,new Font(Font.TIMES_ROMAN, 8, Font.BOLD)));
//                }
                ColumnText.showTextAligned(
                        writer.getDirectContent(),
                        Element.ALIGN_CENTER,
                        new Phrase(String.format("%d", writer.getPageNumber()), new Font(Font.TIMES_ROMAN, 6, Font.BOLD)),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.bottom() - 10, 0);
            } catch (BadElementException | IOException ex) {
                Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DocumentException ex) {
                Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(t, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(writer.getPageNumber()), new Font(Font.TIMES_ROMAN, 6, Font.BOLD)),
                    2, 4, 0);
        }
    }

    class headerFooterRotate extends PdfPageEventHelper {

        PdfTemplate t;
        Image total;
        String usuario;
        String titulo;

        public headerFooterRotate(String usuario, String titulo) {
            this.usuario = usuario;
            this.titulo = titulo;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            t = writer.getDirectContent().createTemplate(0, 0);
            try {
                total = Image.getInstance(t);
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {

            try {
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.setWidths(new int[]{50, 530, 205});
                table.setTotalWidth(800);
                table.setLockedWidth(true);
                table.getDefaultCell().setFixedHeight(20);
//                table.getDefaultCell().setBorder(Rectangle.BOTTOM);
                Image izq = Image.getInstance(FacesContext.getCurrentInstance().getExternalContext().getRealPath("resources/images/logo.png"));
//                Image der = Image.getInstance(FacesContext.getCurrentInstance().getExternalContext().getRealPath("resources/imagenes/escuela.png"));
                izq.scaleAbsolute(45f, 45f);
//                izq.scaleAbsolute(106f, 54f);
//                izq.scaleAbsolute(45f, 22f);

                PdfPCell cell = new PdfPCell(izq);
                cell.setBorder(0);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                table.addCell(cell);

                cell = new PdfPCell(new Phrase(titulo, new Font(Font.TIMES_ROMAN, 10, Font.BOLD)));
                cell.setBorder(0);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

                table.addCell(cell);

//                cell = new PdfPCell(der);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                cell = new PdfPCell(new Phrase("FECHA2 : " + sdf.format(new Date()), new Font(Font.TIMES_ROMAN, 6, Font.BOLD)));
                cell.setBorder(0);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);
                table.writeSelectedRows(0, -1, 30, 580, writer.getDirectContent());
//                table.writeSelectedRows(0, -1, 30, 803, writer.getDirectContent());
//                for (String s:titulos){
//                     document.add(new Paragraph(s,new Font(Font.TIMES_ROMAN, 8, Font.BOLD)));
//                }
                ColumnText.showTextAligned(
                        writer.getDirectContent(),
                        Element.ALIGN_CENTER,
                        new Phrase(String.format("%d", writer.getPageNumber()), new Font(Font.TIMES_ROMAN, 6, Font.BOLD)),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.bottom() - 10, 0);
            } catch (BadElementException | IOException ex) {
                Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DocumentException ex) {
                Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(t, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(writer.getPageNumber()), new Font(Font.TIMES_ROMAN, 6, Font.BOLD)),
                    2, 4, 0);
        }
    }

    class cabeceraVertical extends PdfPageEventHelper {

        PdfTemplate t;
        Image total;
        String parametros;
        String empresa;
        String usuario;
        String titulo;

        public cabeceraVertical(String parametros, String empresa, String usuario, String titulo) {
            this.parametros = parametros;
            this.empresa = empresa;
            this.usuario = usuario;
            this.titulo = titulo;
        }

        public cabeceraVertical(String parametros) {
            this.titulo = parametros;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            t = writer.getDirectContent().createTemplate(0, 0);
            try {
                total = Image.getInstance(t);
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {

//            try {
//                PdfPTable table = new PdfPTable(2);
//                table.setWidthPercentage(100);
//                table.setWidths(new int[]{580, 105});
//                table.setTotalWidth(520);
//                table.setLockedWidth(true);
//                table.getDefaultCell().setFixedHeight(20);
//                table.getDefaultCell().setBorder(Rectangle.BOTTOM);
//                izq.scaleAbsolute(45f, 22f);
//                PdfPCell cell = new PdfPCell(new Phrase(titulo, new Font(Font.TIMES_ROMAN, 10, Font.BOLD)));
//                cell.setBorder(0);
//                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//
//                table.addCell(cell);
//                cell = new PdfPCell(der);
//                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//                cell = new PdfPCell(new Phrase(" FECHA : " + sdf.format(new Date()), new Font(Font.TIMES_ROMAN, 6, Font.BOLD)));
//                cell.setBorder(0);
//                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
//                table.addCell(cell);
//                table.writeSelectedRows(0, -1, 30, 590, writer.getDirectContent());
//                // Nuevo
//                table = new PdfPTable(1);
//                table.setWidthPercentage(100);
//                table.setWidths(new int[]{800});
//                table.setTotalWidth(800);
//                table.setLockedWidth(true);
//                table.getDefaultCell().setFixedHeight(20);
//                cell = new PdfPCell(new Phrase(parametros, new Font(Font.TIMES_ROMAN, 8, Font.BOLD)));
//                cell.setBorder(0);
//                cell.setColspan(2);
//                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
//                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//
//                table.addCell(cell);
//                table.writeSelectedRows(0, -1, 30, 820, writer.getDirectContent());
//                table.writeSelectedRows(0, -1, 30, 803, writer.getDirectContent());
//                for (String s:titulos){
//                     document.add(new Paragraph(s,new Font(Font.TIMES_ROMAN, 8, Font.BOLD)));
//                }
//                ColumnText.showTextAligned(
//                        writer.getDirectContent(),
//                        Element.ALIGN_CENTER,
//                        new Phrase(String.format("%d", writer.getPageNumber()), new Font(Font.TIMES_ROMAN, 6, Font.BOLD)),
//                        (document.right() - document.left()) / 2 + document.leftMargin(),
//                        document.bottom() - 10, 0);
//            } catch (BadElementException ex) {
//                Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (DocumentException ex) {
//                Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//        }
//
//        @Override
//        public void onCloseDocument(PdfWriter writer, Document document) {
//            ColumnText.showTextAligned(t, Element.ALIGN_LEFT,
//                    new Phrase(String.valueOf(writer.getPageNumber()), new Font(Font.TIMES_ROMAN, 6, Font.BOLD)),
//                    2, 4, 0);
//        }
        }
    }

    class cabeceraHorizontal extends PdfPageEventHelper {

        PdfTemplate t;
        Image total;
        String parametros;
        String empresa;
        String usuario;
        String titulo;

        public cabeceraHorizontal(String parametros, String empresa, String usuario, String titulo) {
            this.parametros = parametros;
            this.empresa = empresa;
            this.usuario = usuario;
            this.titulo = titulo;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            t = writer.getDirectContent().createTemplate(0, 0);
            try {
                total = Image.getInstance(t);
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {

            try {
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new int[]{580, 205});
                table.setTotalWidth(800);
                table.setLockedWidth(true);
                table.getDefaultCell().setFixedHeight(20);
                PdfPCell cell = new PdfPCell(new Phrase(titulo, new Font(Font.TIMES_ROMAN, 10, Font.BOLD)));
                cell.setBorder(0);
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

                table.addCell(cell);

//                cell = new PdfPCell(der);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                cell = new PdfPCell(new Phrase("FECHA3 : " + sdf.format(new Date()), new Font(Font.TIMES_ROMAN, 6, Font.BOLD)));
                cell.setBorder(0);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);
//                table.writeSelectedRows(0, -1, 30, 590, writer.getDirectContent());
//                // Nuevo
//                table = new PdfPTable(1);
//                table.setWidthPercentage(100);
//                table.setWidths(new int[]{800});
//                table.setTotalWidth(800);
//                table.setLockedWidth(true);
//                table.getDefaultCell().setFixedHeight(20);

                cell = new PdfPCell(new Phrase(parametros, new Font(Font.TIMES_ROMAN, 8, Font.BOLD)));
                cell.setBorder(0);
                cell.setColspan(2);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

                table.addCell(cell);
                table.writeSelectedRows(0, -1, 30, 570, writer.getDirectContent());
//                table.writeSelectedRows(0, -1, 30, 803, writer.getDirectContent());
//                for (String s:titulos){
//                     document.add(new Paragraph(s,new Font(Font.TIMES_ROMAN, 8, Font.BOLD)));
//                }
                ColumnText.showTextAligned(
                        writer.getDirectContent(),
                        Element.ALIGN_CENTER,
                        new Phrase(String.format("%d", writer.getPageNumber()), new Font(Font.TIMES_ROMAN, 6, Font.BOLD)),
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.bottom() - 10, 0);
            } catch (BadElementException ex) {
                Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DocumentException ex) {
                Logger.getLogger(DocumentoPDF.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(t, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(writer.getPageNumber()), new Font(Font.TIMES_ROMAN, 6, Font.BOLD)),
                    2, 4, 0);
        }
    }
}
