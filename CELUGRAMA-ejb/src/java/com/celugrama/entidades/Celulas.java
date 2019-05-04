/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.entidades;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author oscar
 */
@Entity
@Table(name = "celulas")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Celulas.findAll", query = "SELECT c FROM Celulas c"),
    @NamedQuery(name = "Celulas.findById", query = "SELECT c FROM Celulas c WHERE c.id = :id"),
    @NamedQuery(name = "Celulas.findByCodigo", query = "SELECT c FROM Celulas c WHERE c.codigo = :codigo"),
    @NamedQuery(name = "Celulas.findByDireccion", query = "SELECT c FROM Celulas c WHERE c.direccion = :direccion"),
    @NamedQuery(name = "Celulas.findBySector", query = "SELECT c FROM Celulas c WHERE c.sector = :sector"),
    @NamedQuery(name = "Celulas.findByNombreanfitrion", query = "SELECT c FROM Celulas c WHERE c.nombreanfitrion = :nombreanfitrion"),
    @NamedQuery(name = "Celulas.findByNombretimoteo", query = "SELECT c FROM Celulas c WHERE c.nombretimoteo = :nombretimoteo"),
    @NamedQuery(name = "Celulas.findByTelefono", query = "SELECT c FROM Celulas c WHERE c.telefono = :telefono"),
    @NamedQuery(name = "Celulas.findByRed", query = "SELECT c FROM Celulas c WHERE c.red = :red"),
    @NamedQuery(name = "Celulas.findByFecha", query = "SELECT c FROM Celulas c WHERE c.fecha = :fecha"),
    @NamedQuery(name = "Celulas.findByPromedioa", query = "SELECT c FROM Celulas c WHERE c.promedioa = :promedioa"),
    @NamedQuery(name = "Celulas.findByInvitados", query = "SELECT c FROM Celulas c WHERE c.invitados = :invitados"),
    @NamedQuery(name = "Celulas.findByCelulasn", query = "SELECT c FROM Celulas c WHERE c.celulasn = :celulasn"),
    @NamedQuery(name = "Celulas.findByActivo", query = "SELECT c FROM Celulas c WHERE c.activo = :activo")})
public class Celulas implements Serializable {

    @Column(name = "hora")
    @Temporal(TemporalType.TIME)
    private Date hora;

    @OneToMany(mappedBy = "celula")
    private List<Asistenciacelulas> asistenciacelulasList;

    @OneToMany(mappedBy = "celula")
    private List<Temas> temasList;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 2147483647)
    @Column(name = "codigo")
    private String codigo;
    @Size(max = 2147483647)
    @Column(name = "direccion")
    private String direccion;
    @Size(max = 2147483647)
    @Column(name = "sector")
    private String sector;
    @Size(max = 2147483647)
    @Column(name = "nombreanfitrion")
    private String nombreanfitrion;
    @Size(max = 2147483647)
    @Column(name = "nombretimoteo")
    private String nombretimoteo;
    @Size(max = 2147483647)
    @Column(name = "telefono")
    private String telefono;
    @Size(max = 2147483647)
    @Column(name = "red")
    private String red;
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @Column(name = "promedioa")
    private Integer promedioa;
    @Column(name = "invitados")
    private Integer invitados;
    @Column(name = "celulasn")
    private Integer celulasn;
    @Column(name = "activo")
    private Boolean activo;
    @JoinColumn(name = "lider", referencedColumnName = "id")
    @ManyToOne
    private Entidades lider;

    public Celulas() {
    }

    public Celulas(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getNombreanfitrion() {
        return nombreanfitrion;
    }

    public void setNombreanfitrion(String nombreanfitrion) {
        this.nombreanfitrion = nombreanfitrion;
    }

    public String getNombretimoteo() {
        return nombretimoteo;
    }

    public void setNombretimoteo(String nombretimoteo) {
        this.nombretimoteo = nombretimoteo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getRed() {
        return red;
    }

    public void setRed(String red) {
        this.red = red;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Integer getPromedioa() {
        return promedioa;
    }

    public void setPromedioa(Integer promedioa) {
        this.promedioa = promedioa;
    }

    public Integer getInvitados() {
        return invitados;
    }

    public void setInvitados(Integer invitados) {
        this.invitados = invitados;
    }

    public Integer getCelulasn() {
        return celulasn;
    }

    public void setCelulasn(Integer celulasn) {
        this.celulasn = celulasn;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Entidades getLider() {
        return lider;
    }

    public void setLider(Entidades lider) {
        this.lider = lider;
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
        if (!(object instanceof Celulas)) {
            return false;
        }
        Celulas other = (Celulas) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.celugrama.entidades.Celulas[ id=" + id + " ]";
    }

    @XmlTransient
    public List<Temas> getTemasList() {
        return temasList;
    }

    public void setTemasList(List<Temas> temasList) {
        this.temasList = temasList;
    }

    @XmlTransient
    public List<Asistenciacelulas> getAsistenciacelulasList() {
        return asistenciacelulasList;
    }

    public void setAsistenciacelulasList(List<Asistenciacelulas> asistenciacelulasList) {
        this.asistenciacelulasList = asistenciacelulasList;
    }

    public Date getHora() {
        return hora;
    }

    public void setHora(Date hora) {
        this.hora = hora;
    }
    
}
