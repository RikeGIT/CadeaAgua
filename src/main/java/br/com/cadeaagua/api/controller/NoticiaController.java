package br.com.cadeaagua.api.controller;

import br.com.cadeaagua.api.entity.Noticia;
import br.com.cadeaagua.api.service.NoticiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/noticias")
@CrossOrigin("*")
public class NoticiaController {

    @Autowired
    private NoticiaService noticiaService;

    // Rota para listar todas as noticias.
    @GetMapping
    public List<Noticia> listarTodas() {
        return noticiaService.listarTodas();
    }

    // Rota para buscar uma noticia especifica pelo ID.
    @GetMapping("/{id}")
    public ResponseEntity<Noticia> buscarPorId(@PathVariable Integer id) {
        return noticiaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Rota para salvar uma nova noticia.
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Noticia noticia) {
        try {
            Noticia salva = noticiaService.salvarNoticia(noticia);
            return ResponseEntity.ok(salva);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Rota para deletar uma noticia.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        noticiaService.deletarNoticia(id);
        return ResponseEntity.noContent().build();
    }
}
