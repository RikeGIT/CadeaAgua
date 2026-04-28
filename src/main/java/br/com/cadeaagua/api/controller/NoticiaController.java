package br.com.cadeaagua.api.controller;

import br.com.cadeaagua.api.entity.Noticia;
import br.com.cadeaagua.api.service.NoticiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/noticias")
@CrossOrigin("*") // Permite que o seu frontend HTML acesse a API sem erros de CORS
public class NoticiaController {

    @Autowired
    private NoticiaService noticiaService;

    // Rota para listar todas as notícias (Read)
    @GetMapping
    public List<Noticia> listarTodas() {
        return noticiaService.listarTodas();
    }

    // Rota para salvar uma nova notícia (Create)
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Noticia noticia) {
        try {
            // Chamamos o Service para que as regras de negócio sejam aplicadas
            Noticia salva = noticiaService.salvarNoticia(noticia);
            return ResponseEntity.ok(salva);
        } catch (IllegalArgumentException e) {
            // Se o Service lançar um erro de validação (ex: título vazio), retornamos erro 400
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Rota para deletar uma notícia (Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        // Seguindo o padrão do seu CronogramaController
        noticiaService.deletarNoticia(id);
        return ResponseEntity.noContent().build();
    }
}