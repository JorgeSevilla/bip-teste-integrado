package com.example.backend.controller;

import com.example.ejb.model.Beneficio;
import com.example.ejb.service.BeneficioEjbService;
import jakarta.ejb.EJB;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/api/v1/beneficios")
public class BeneficioController {

    @EJB
    private BeneficioEjbService beneficioService;

    @GetMapping
    public List<Beneficio> list() {
        return beneficioService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Beneficio> obterPorId(@PathVariable Long id) {
        Beneficio beneficio = beneficioService.findById(id);
        if (beneficio == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(beneficio);
    }

    @PostMapping
    public Beneficio criar(@RequestBody Beneficio beneficio) {
        beneficio.setId(null);
        beneficio.setVersion(null);
        return beneficioService.create(beneficio);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Beneficio> atualizar(@PathVariable Long id, @RequestBody Beneficio beneficioData) {
        try {
            Beneficio atualizado = beneficioService.update(id, beneficioData);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        try {
            beneficioService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    public static record TransferenciaRequest(Long fromId, Long toId, BigDecimal amount) {
    }

    @PostMapping("/transferir")
    public ResponseEntity<String> transferir(@RequestBody TransferenciaRequest request) {
        try {
            beneficioService.transfer(request.fromId(), request.toId(), request.amount());

            return ResponseEntity.ok("Transferência completada com sucesso.");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro na transferência: " + e.getMessage());
        }
    }
}
