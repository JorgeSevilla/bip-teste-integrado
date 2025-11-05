package com.example.ejb;

import jakarta.ejb.EJBException;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;

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
            Beneficio from = em.find(Beneficio.class, fromId, LockModeType.PESSIMISTIC_WRITE);
            Beneficio to   = em.find(Beneficio.class, toId, LockModeType.PESSIMISTIC_WRITE);

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
            throw new RuntimeException("A transferência não pôde ser concluída devido a uma modificação simultânea." +
                    " Tente novamente.", ole);
        } catch (Exception e) {
            throw new RuntimeException("Erro na transferência" + e.getMessage(), e);
        }

    }
}
