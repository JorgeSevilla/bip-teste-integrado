package com.example.ejb.service;

import com.example.ejb.model.Beneficio;
import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.List;

@Stateless
public class BeneficioEjbService {

    @PersistenceContext
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void transfer(Long fromId, Long toId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new EJBException("O monto deve ser um valos válido positivo");
        }

        try {
            Beneficio from = em.find(Beneficio.class, fromId);
            Beneficio to   = em.find(Beneficio.class, toId);

            if (from == null || to == null) {
                throw new EJBException("Conta de origem ou destino não encontada");
            }

            if (from.getValor().compareTo(amount) < 0) {
                throw new EJBException("Saldo insuficiente para transferência.");
            }

            // BUG: sem validações, sem locking, pode gerar saldo negativo e lost update
            from.setValor(from.getValor().subtract(amount));
            to.setValor(to.getValor().add(amount));

            em.merge(from);
            em.merge(to);
        } catch (OptimisticLockException ole) {
            throw new EJBException("A transferência não pôde ser concluída devido a uma modificação simultânea." +
                    " Tente novamente.", ole);
        } catch (Exception e) {
            throw new EJBException("Erro na transferência" + e.getMessage(), e);
        }

    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public List<Beneficio> findAll() {
        return em.createQuery("SELECT b FROM Beneficio b", Beneficio.class)
                .getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public Beneficio findById(Long id) {
        return em.find(Beneficio.class, id);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Beneficio create(Beneficio beneficio) {
        em.persist(beneficio);
        return beneficio;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Beneficio update(Long id, Beneficio beneficioData) {
        Beneficio existing = em.find(Beneficio.class, id);
        if (existing == null) {
            throw new EJBException("Beneficio com ID " + id + " não encontrado.");
        }

        existing.setNome(beneficioData.getNome());
        existing.setDescricao(beneficioData.getDescricao());
        existing.setValor(beneficioData.getValor());
        existing.setAtivo(beneficioData.getAtivo());

        return em.merge(existing);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void delete(Long id) {
        Beneficio beneficio = em.find(Beneficio.class, id);
        if (beneficio != null) {
            em.remove(beneficio);
        } else {
            throw new EJBException("Beneficio com ID " + id + " não encontrado.");
        }
    }
}
