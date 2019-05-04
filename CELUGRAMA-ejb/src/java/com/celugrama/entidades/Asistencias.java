/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.entidades;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author oscar
 */
@Entity
@Table(name = "asistencias")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Asistencias.findAll", query = "SELECT a FROM Asistencias a"),
    @NamedQuery(name = "Asistencias.findById", query = "SELECT a FROM Asistencias a WHERE a.id = :id"),
    @NamedQuery(name = "Asistencias.findByAsistencia", query = "SELECT a FROM Asistencias a WHERE a.asistencia = :asistencia"),
    @NamedQuery(name = "Asistencias.findByLectura", query = "SELECT a FROM Asistencias a WHERE a.lectura = :lectura"),
    @NamedQuery(name = "Asistencias.findByActivo", query = "SELECT a FROM Asistencias a WHERE a.activo = :activo")})
public class Asistencias implements Serializable {

    @JoinColumn(name = "tema", referencedColumnName = "id")
    @ManyToOne
    private Temas tema;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "asistencia")
    private Boolean asistencia;
    @Column(name = "lectura")
    private Boolean lectura;
    @Column(name = "activo")
    private Boolean activo;
    @JoinColumn(name = "asistenciacelula", referencedColumnName = "id")
    @ManyToOne
    private Asistenciacelulas asistenciacelula;

    public Asistencias() {
    }

    public Asistencias(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getAsistencia() {
        return asistencia;
    }

    public void setAsistencia(Boolean asistencia) {
        this.asistencia = asistencia;
    }

    public Boolean getLectura() {
        return lectura;
    }

    public void setLectura(Boolean lectura) {
        this.lectura = lectura;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Asistenciacelulas getAsistenciacelula() {
        return asistenciacelula;
    }

    public void setAsistenciacelula(Asistenciacelulas asistenciacelula) {
        this.asistenciacelula = asistenciacelula;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Asistencias)) {
            return false;
        }
        Asistencias other = (Asistencias) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.celugrama.entidades.Asistencias[ id=" + id + " ]";
    }

    public Temas getTema() {
        return tema;
    }

    public void setTema(Temas tema) {
        this.tema = tema;
    }
    
}
