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
@Table(name = "crecimientos")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Crecimientos.findAll", query = "SELECT c FROM Crecimientos c"),
    @NamedQuery(name = "Crecimientos.findById", query = "SELECT c FROM Crecimientos c WHERE c.id = :id"),
    @NamedQuery(name = "Crecimientos.findByAsistencia", query = "SELECT c FROM Crecimientos c WHERE c.asistencia = :asistencia"),
    @NamedQuery(name = "Crecimientos.findByPre", query = "SELECT c FROM Crecimientos c WHERE c.pre = :pre"),
    @NamedQuery(name = "Crecimientos.findByEnc", query = "SELECT c FROM Crecimientos c WHERE c.enc = :enc"),
    @NamedQuery(name = "Crecimientos.findByPos", query = "SELECT c FROM Crecimientos c WHERE c.pos = :pos"),
    @NamedQuery(name = "Crecimientos.findByNivel1", query = "SELECT c FROM Crecimientos c WHERE c.nivel1 = :nivel1"),
    @NamedQuery(name = "Crecimientos.findByNivel2", query = "SELECT c FROM Crecimientos c WHERE c.nivel2 = :nivel2"),
    @NamedQuery(name = "Crecimientos.findByNivel3", query = "SELECT c FROM Crecimientos c WHERE c.nivel3 = :nivel3"),
    @NamedQuery(name = "Crecimientos.findByActivo", query = "SELECT c FROM Crecimientos c WHERE c.activo = :activo")})
public class Crecimientos implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "asistencia")
    private Boolean asistencia;
    @Column(name = "pre")
    private Boolean pre;
    @Column(name = "enc")
    private Boolean enc;
    @Column(name = "pos")
    private Boolean pos;
    @Column(name = "nivel1")
    private Boolean nivel1;
    @Column(name = "nivel2")
    private Boolean nivel2;
    @Column(name = "nivel3")
    private Boolean nivel3;
    @Column(name = "activo")
    private Boolean activo;
    @JoinColumn(name = "celula", referencedColumnName = "id")
    @ManyToOne
    private Celulas celula;
    @JoinColumn(name = "entidad", referencedColumnName = "id")
    @ManyToOne
    private Entidades entidad;

    public Crecimientos() {
    }

    public Crecimientos(Integer id) {
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

    public Boolean getPre() {
        return pre;
    }

    public void setPre(Boolean pre) {
        this.pre = pre;
    }

    public Boolean getEnc() {
        return enc;
    }

    public void setEnc(Boolean enc) {
        this.enc = enc;
    }

    public Boolean getPos() {
        return pos;
    }

    public void setPos(Boolean pos) {
        this.pos = pos;
    }

    public Boolean getNivel1() {
        return nivel1;
    }

    public void setNivel1(Boolean nivel1) {
        this.nivel1 = nivel1;
    }

    public Boolean getNivel2() {
        return nivel2;
    }

    public void setNivel2(Boolean nivel2) {
        this.nivel2 = nivel2;
    }

    public Boolean getNivel3() {
        return nivel3;
    }

    public void setNivel3(Boolean nivel3) {
        this.nivel3 = nivel3;
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

    public Entidades getEntidad() {
        return entidad;
    }

    public void setEntidad(Entidades entidad) {
        this.entidad = entidad;
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
        if (!(object instanceof Crecimientos)) {
            return false;
        }
        Crecimientos other = (Crecimientos) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.celugrama.entidades.Crecimientos[ id=" + id + " ]";
    }
    
}
