package com.example.ejb.service;

import com.example.ejb.model.Beneficio;
import jakarta.ejb.EJBException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BeneficioEjbServiceTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private BeneficioEjbService beneficioService;

    @Test
    void transfer_deveLancarExcecao_quandoSaldoForInsuficiente() {

        Beneficio origem = new Beneficio();
        origem.setId(1L);
        origem.setValor(new BigDecimal("100.00"));

        Beneficio destino = new Beneficio();
        destino.setId(2L);
        destino.setValor(new BigDecimal("50.00"));

        BigDecimal monto = new BigDecimal("150.00");

        when(em.find(Beneficio.class, 1L)).thenReturn(origem);

        when(em.find(Beneficio.class, 2L)).thenReturn(destino);


        EJBException exception = assertThrows(EJBException.class, () -> {
            beneficioService.transfer(1L, 2L, monto);
        });

        assertTrue(exception.getMessage().contains("Saldo insuficiente"));

        verify(em, never()).merge(any());
    }

    @Test
    void transfer_deveCompletar_quandoSaldoForSuficiente() {

        Beneficio origem = new Beneficio();
        origem.setId(1L);
        origem.setValor(new BigDecimal("200.00"));

        Beneficio destino = new Beneficio();
        destino.setId(2L);
        destino.setValor(new BigDecimal("50.00"));

        BigDecimal monto = new BigDecimal("100.00");

        when(em.find(Beneficio.class, 1L)).thenReturn(origem);
        when(em.find(Beneficio.class, 2L)).thenReturn(destino);


        beneficioService.transfer(1L, 2L, monto);

        assertEquals(new BigDecimal("100.00"), origem.getValor());
        assertEquals(new BigDecimal("150.00"), destino.getValor());

        verify(em, times(2)).merge(any(Beneficio.class));
    }
}
