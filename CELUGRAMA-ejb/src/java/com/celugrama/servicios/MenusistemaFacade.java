/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.celugrama.servicios;

import com.celugrama.entidades.Menusistema;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author oscar
 */
@Stateless
public class MenusistemaFacade extends AbstractFacade<Menusistema> {

    @PersistenceContext(unitName = "CELUGRAMA-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public MenusistemaFacade() {
        super(Menusistema.class);
    }
    
}
