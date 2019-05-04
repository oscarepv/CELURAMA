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
@Table(name = "gruposusuarios")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Gruposusuarios.findAll", query = "SELECT g FROM Gruposusuarios g"),
    @NamedQuery(name = "Gruposusuarios.findById", query = "SELECT g FROM Gruposusuarios g WHERE g.id = :id")})
public class Gruposusuarios implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @JoinColumn(name = "grupo", referencedColumnName = "id")
    @ManyToOne
    private Codigos grupo;
    @JoinColumn(name = "modulo", referencedColumnName = "id")
    @ManyToOne
    private Codigos modulo;
    @JoinColumn(name = "usuario", referencedColumnName = "id")
    @ManyToOne
    private Entidades usuario;

    public Gruposusuarios() {
    }

    public Gruposusuarios(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Codigos getGrupo() {
        return grupo;
    }

    public void setGrupo(Codigos grupo) {
        this.grupo = grupo;
    }

    public Codigos getModulo() {
        return modulo;
    }

    public void setModulo(Codigos modulo) {
        this.modulo = modulo;
    }

    public Entidades getUsuario() {
        return usuario;
    }

    public void setUsuario(Entidades usuario) {
        this.usuario = usuario;
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
        if (!(object instanceof Gruposusuarios)) {
            return false;
        }
        Gruposusuarios other = (Gruposusuarios) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.celugrama.entidades.Gruposusuarios[ id=" + id + " ]";
    }
    
}
