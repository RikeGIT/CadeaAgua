package br.com.cadeaagua.api.controller;

import br.com.cadeaagua.api.entity.Cronograma;
import br.com.cadeaagua.api.repository.CronogramaRepository;
import br.com.cadeaagua.api.service.CronogramaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cronogramas")
public class CronogramaController {
    @Autowired
    private CronogramaService cronogramaService;

    @Autowired
    private CronogramaRepository cronogramaRepository;

    // Listar todos os cronogramas
    @GetMapping
    public List<Cronograma> listarTodos() {
        return cronogramaRepository.findAll();
    }

    // Criar novo cronograma
    // No Controller, mude para:
    @PostMapping
    public ResponseEntity<Cronograma> criar(@RequestBody Cronograma cronograma) {
        return ResponseEntity.ok(cronogramaService.salvarEAlertar(cronograma));
    }

    // Consultar status atual por região (Ação Principal do Dashboard)
    @GetMapping("/status/{idRegiao}")
    public ResponseEntity<?> consultarStatusAtual(@PathVariable Integer idRegiao) {
        LocalDate hoje = LocalDate.now();
        List<Cronograma> resultados = cronogramaRepository.findByRegiaoAndData(idRegiao, hoje);

        if (resultados.isEmpty()) {
            return ResponseEntity.ok("Sem informações de rodízio para hoje.");
        }
        return ResponseEntity.ok(resultados);
    }

    // Deletar cronograma
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        cronogramaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}