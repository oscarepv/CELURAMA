/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.servicios;

import com.celugrama.entidades.Codigos;
import com.celugrama.excepciones.ConsultarException;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author oscar
 */
@Stateless
public class CodigosFacade extends AbstractFacade<Codigos> {

    @PersistenceContext(unitName = "CELUGRAMA-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CodigosFacade() {
        super(Codigos.class);
    }

    public Codigos traerCodigo(String maestro, String codigo) throws ConsultarException {
        String sql = "Select object(o) from Codigos as o"
                + " where  o.activo=true and  "
                + " o.codigo=:codigo and o.maestro.codigo=:maestro";
        Query q = em.createQuery(sql);
        q.setParameter("maestro", maestro);
        q.setParameter("codigo", codigo);
        List<Codigos> lista = q.getResultList();
        if ((lista == null) || (lista.isEmpty())) {
            return null;
        }
        return lista.get(0);
    }

    public List<Codigos> traerCodigos(String maestro) throws ConsultarException {
        String sql = "Select object(o) from Codigos as o"
                + " where o.maestro.codigo=:maestroParametro and o.activo=true"
                + " order by o.nombre";
        Query q = em.createQuery(sql);
        q.setParameter("maestroParametro", maestro);

        return q.getResultList();
    }

    public List<Codigos> traerCodigosParametros(String maestro, String parametro) throws ConsultarException {
        String sql = "Select object(o) from Codigos as o"
                + " where o.maestro.codigo=:maestroParametro and o.activo=true and o.parametros=:parametro"
                + " order by o.nombre";
        Query q = em.createQuery(sql);
        q.setParameter("maestroParametro", maestro);
        q.setParameter("parametro", parametro);
        return q.getResultList();
    }
    
     public List<Codigos> traerCodigosOrdenadoCodigo(String maestro) throws ConsultarException {
        String sql = "Select object(o) from Codigos as o"
                + " where o.maestro.codigo=:maestroParametro and o.activo=true"
                + " order by o.codigo,o.nombre";
        Query q = em.createQuery(sql);
        q.setParameter("maestroParametro", maestro);
        return q.getResultList();
    }
     
     public Codigos traerCodigosNombre(String maestro, String nombre) throws ConsultarException {
        String sql = "Select object(o) from Codigos as o"
                + " where o.maestro.codigo=:maestroParametro and o.activo=true and o.nombre=:nombre"
                + " order by o.nombre";
        Query q = em.createQuery(sql);
        q.setParameter("maestroParametro", maestro);
        q.setParameter("nombre", nombre);
        List<Codigos> lc = q.getResultList();
        for (Codigos c : lc) {
            return c;
        }
        return null;
    }
  

}
