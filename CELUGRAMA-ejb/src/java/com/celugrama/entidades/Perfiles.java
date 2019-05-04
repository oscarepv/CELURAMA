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
@Table(name = "perfiles")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Perfiles.findAll", query = "SELECT p FROM Perfiles p"),
    @NamedQuery(name = "Perfiles.findById", query = "SELECT p FROM Perfiles p WHERE p.id = :id"),
    @NamedQuery(name = "Perfiles.findByConsulta", query = "SELECT p FROM Perfiles p WHERE p.consulta = :consulta"),
    @NamedQuery(name = "Perfiles.findByModificacion", query = "SELECT p FROM Perfiles p WHERE p.modificacion = :modificacion"),
    @NamedQuery(name = "Perfiles.findByBorrado", query = "SELECT p FROM Perfiles p WHERE p.borrado = :borrado"),
    @NamedQuery(name = "Perfiles.findByNuevo", query = "SELECT p FROM Perfiles p WHERE p.nuevo = :nuevo")})
public class Perfiles implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "consulta")
    private Boolean consulta;
    @Column(name = "modificacion")
    private Boolean modificacion;
    @Column(name = "borrado")
    private Boolean borrado;
    @Column(name = "nuevo")
    private Boolean nuevo;
    @JoinColumn(name = "grupo", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Codigos grupo;
    @JoinColumn(name = "menu", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Menusistema menu;

    public Perfiles() {
    }

    public Perfiles(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getConsulta() {
        return consulta;
    }

    public void setConsulta(Boolean consulta) {
        this.consulta = consulta;
    }

    public Boolean getModificacion() {
        return modificacion;
    }

    public void setModificacion(Boolean modificacion) {
        this.modificacion = modificacion;
    }

    public Boolean getBorrado() {
        return borrado;
    }

    public void setBorrado(Boolean borrado) {
        this.borrado = borrado;
    }

    public Boolean getNuevo() {
        return nuevo;
    }

    public void setNuevo(Boolean nuevo) {
        this.nuevo = nuevo;
    }

    public Codigos getGrupo() {
        return grupo;
    }

    public void setGrupo(Codigos grupo) {
        this.grupo = grupo;
    }

    public Menusistema getMenu() {
        return menu;
    }

    public void setMenu(Menusistema menu) {
        this.menu = menu;
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
        if (!(object instanceof Perfiles)) {
            return false;
        }
        Perfiles other = (Perfiles) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.celugrama.entidades.Perfiles[ id=" + id + " ]";
    }
    
}
