/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.servicios;

import com.celugrama.entidades.Entidades;
import com.celugrama.excepciones.ConsultarException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author oscar
 */
@Stateless
public class EntidadesFacade extends AbstractFacade<Entidades> {

    @PersistenceContext(unitName = "CELUGRAMA-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public EntidadesFacade() {
        super(Entidades.class);
    }
    
    public Entidades login(String pin, String celular)
            throws ConsultarException {
        Entidades retorno = null;
        try {
            Map parametros = new HashMap();
            parametros.put(";where", "o.userid=:userid and o.pwd=:pwd and o.activo=true");
            parametros.put("userid", pin);
            parametros.put("pwd", celular);
//            parametros.put(";where", "o.pin=:pin and o.telefono=:celular and o.activo=true");
//            parametros.put("pin", pin);
//            parametros.put("celular", celular);
            List<Entidades> lista = super.encontarParametros(parametros);
            if ((lista == null) || (lista.isEmpty())) {
                return null;
            }
            retorno = lista.get(0);
        } catch (ConsultarException e) {
            throw new ConsultarException("ERROR LOGIN", e);
        }
        return retorno;
    }
    
}
