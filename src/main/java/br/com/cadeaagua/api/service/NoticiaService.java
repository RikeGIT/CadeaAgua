package br.com.cadeaagua.api.service;

import br.com.cadeaagua.api.entity.Noticia;
import br.com.cadeaagua.api.repository.NoticiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class NoticiaService {

    @Autowired
    private NoticiaRepository noticiaRepository;

    public Noticia salvarNoticia(Noticia noticia) {

        // campos não podem ser vazios
        if (noticia.getTitulo() == null || noticia.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O título da notícia não pode ficar em branco.");
        }
        if (noticia.getDescricao() == null || noticia.getDescricao().trim().isEmpty()) {
            throw new IllegalArgumentException("A notícia precisa ter um conteúdo/descrição.");
        }

        // Data automatica
        if (noticia.getData_publicacao() == null) {
            noticia.setData_publicacao(LocalDate.now());
        }

        // imagem padrão
        if (noticia.getImagem_url() == null || noticia.getImagem_url().trim().isEmpty()) {
            noticia.setImagem_url("/assets/hero-image.png"); // Puxa uma imagem que já existe no seu projeto
        }

        return noticiaRepository.save(noticia);
    }

    public List<Noticia> listarTodas() {
        return noticiaRepository.findAll();
    }

    // deletar noticia pelo id
    public void deletarNoticia(Integer id) {
        noticiaRepository.deleteById(id);
    }
}