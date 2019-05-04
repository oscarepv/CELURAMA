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
@Table(name = "asistenciacelulas")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Asistenciacelulas.findAll", query = "SELECT a FROM Asistenciacelulas a"),
    @NamedQuery(name = "Asistenciacelulas.findById", query = "SELECT a FROM Asistenciacelulas a WHERE a.id = :id"),
    @NamedQuery(name = "Asistenciacelulas.findByActivo", query = "SELECT a FROM Asistenciacelulas a WHERE a.activo = :activo")})
public class Asistenciacelulas implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "activo")
    private Boolean activo;
    @JoinColumn(name = "celula", referencedColumnName = "id")
    @ManyToOne
    private Celulas celula;
    @JoinColumn(name = "asistente", referencedColumnName = "id")
    @ManyToOne
    private Entidades asistente;

    public Asistenciacelulas() {
    }

    public Asistenciacelulas(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Celulas getCelula() {
        return celula;
    }

    public void setCelula(Celulas celula) {
        this.celula = celula;
    }

    public Entidades getAsistente() {
        return asistente;
    }

    public void setAsistente(Entidades asistente) {
        this.asistente = asistente;
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
        if (!(object instanceof Asistenciacelulas)) {
            return false;
        }
        Asistenciacelulas other = (Asistenciacelulas) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.celugrama.entidades.Asistenciacelulas[ id=" + id + " ]";
    }
    
}
