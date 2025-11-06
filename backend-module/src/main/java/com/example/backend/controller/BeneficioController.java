package com.example.backend.controller;

import com.example.ejb.model.Beneficio;
import com.example.ejb.service.BeneficioEjbService;
import jakarta.ejb.EJB;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
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
    public ResponseEntity<Beneficio> criar(@Valid @RequestBody Beneficio beneficio) {
        beneficio.setId(null);
        beneficio.setVersion(null);

        Beneficio beneficioCriado = beneficioService.create(beneficio);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(beneficioCriado.getId())
                .toUri();

        return ResponseEntity.created(location).body(beneficioCriado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Beneficio> atualizar(@PathVariable Long id, @Valid @RequestBody Beneficio beneficioData) {
        Beneficio atualizado = beneficioService.update(id, beneficioData);
        return ResponseEntity.ok(atualizado);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        beneficioService.delete(id);
        return ResponseEntity.noContent().build();

    }

    public static record TransferenciaRequest(Long fromId, Long toId, BigDecimal amount) {
    }

    @PostMapping("/transferir")
    public ResponseEntity<String> transferir(@RequestBody TransferenciaRequest request) {
        beneficioService.transfer(request.fromId(), request.toId(), request.amount());

        return ResponseEntity.ok("Transferência completada com sucesso.");
    }
}
