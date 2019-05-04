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
@Table(name = "entidades")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Entidades.findAll", query = "SELECT e FROM Entidades e"),
    @NamedQuery(name = "Entidades.findById", query = "SELECT e FROM Entidades e WHERE e.id = :id"),
    @NamedQuery(name = "Entidades.findByNombres", query = "SELECT e FROM Entidades e WHERE e.nombres = :nombres"),
    @NamedQuery(name = "Entidades.findByEmail", query = "SELECT e FROM Entidades e WHERE e.email = :email"),
    @NamedQuery(name = "Entidades.findByUserid", query = "SELECT e FROM Entidades e WHERE e.userid = :userid"),
    @NamedQuery(name = "Entidades.findByPwd", query = "SELECT e FROM Entidades e WHERE e.pwd = :pwd"),
    @NamedQuery(name = "Entidades.findByIdentificacion", query = "SELECT e FROM Entidades e WHERE e.identificacion = :identificacion"),
    @NamedQuery(name = "Entidades.findByDireccion", query = "SELECT e FROM Entidades e WHERE e.direccion = :direccion"),
    @NamedQuery(name = "Entidades.findByActivo", query = "SELECT e FROM Entidades e WHERE e.activo = :activo"),
    @NamedQuery(name = "Entidades.findByGenero", query = "SELECT e FROM Entidades e WHERE e.genero = :genero"),
    @NamedQuery(name = "Entidades.findByFecha", query = "SELECT e FROM Entidades e WHERE e.fecha = :fecha"),
    @NamedQuery(name = "Entidades.findByTelefono", query = "SELECT e FROM Entidades e WHERE e.telefono = :telefono"),
    @NamedQuery(name = "Entidades.findByCelular", query = "SELECT e FROM Entidades e WHERE e.celular = :celular"),
    @NamedQuery(name = "Entidades.findByTipo", query = "SELECT e FROM Entidades e WHERE e.tipo = :tipo"),
    @NamedQuery(name = "Entidades.findByServicio", query = "SELECT e FROM Entidades e WHERE e.servicio = :servicio"),
    @NamedQuery(name = "Entidades.findByComite", query = "SELECT e FROM Entidades e WHERE e.comite = :comite"),
    @NamedQuery(name = "Entidades.findByCodigo", query = "SELECT e FROM Entidades e WHERE e.codigo = :codigo")})
public class Entidades implements Serializable {

    @Column(name = "edad")
    private Integer edad;
    @Size(max = 2147483647)
    @Column(name = "sexo")
    private String sexo;

    @OneToMany(mappedBy = "lider")
    private List<Celulas> celulasList;
    @OneToMany(mappedBy = "asistente")
    private List<Asistenciacelulas> asistenciacelulasList;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 2147483647)
    @Column(name = "nombres")
    private String nombres;
    // @Pattern(regexp="[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", message="Invalid email")//if the field contains email address consider using this annotation to enforce field validation
    @Size(max = 2147483647)
    @Column(name = "email")
    private String email;
    @Size(max = 2147483647)
    @Column(name = "userid")
    private String userid;
    @Size(max = 2147483647)
    @Column(name = "pwd")
    private String pwd;
    @Size(max = 2147483647)
    @Column(name = "identificacion")
    private String identificacion;
    @Size(max = 2147483647)
    @Column(name = "direccion")
    private String direccion;
    @Column(name = "activo")
    private Boolean activo;
    @Size(max = 2147483647)
    @Column(name = "genero")
    private String genero;
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @Size(max = 2147483647)
    @Column(name = "telefono")
    private String telefono;
    @Size(max = 2147483647)
    @Column(name = "celular")
    private String celular;
    @Size(max = 2147483647)
    @Column(name = "tipo")
    private String tipo;
    @Size(max = 2147483647)
    @Column(name = "servicio")
    private String servicio;
    @Size(max = 2147483647)
    @Column(name = "comite")
    private String comite;
    @Size(max = 2147483647)
    @Column(name = "codigo")
    private String codigo;
    @JoinColumn(name = "roles", referencedColumnName = "id")
    @ManyToOne
    private Roles roles;

    public Entidades() {
    }

    public Entidades(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public String getComite() {
        return comite;
    }

    public void setComite(String comite) {
        this.comite = comite;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Roles getRoles() {
        return roles;
    }

    public void setRoles(Roles roles) {
        this.roles = roles;
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
        if (!(object instanceof Entidades)) {
            return false;
        }
        Entidades other = (Entidades) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return nombres;
    }

    @XmlTransient
    public List<Celulas> getCelulasList() {
        return celulasList;
    }

    public void setCelulasList(List<Celulas> celulasList) {
        this.celulasList = celulasList;
    }

    @XmlTransient
    public List<Asistenciacelulas> getAsistenciacelulasList() {
        return asistenciacelulasList;
    }

    public void setAsistenciacelulasList(List<Asistenciacelulas> asistenciacelulasList) {
        this.asistenciacelulasList = asistenciacelulasList;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }
    
}
