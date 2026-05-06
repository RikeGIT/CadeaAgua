package br.com.cadeaagua.api.service;

import br.com.cadeaagua.api.entity.Noticia;
import br.com.cadeaagua.api.repository.NoticiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class NoticiaService {

    @Autowired
    private NoticiaRepository noticiaRepository;

    public Noticia salvarNoticia(Noticia noticia) {
        if (noticia.getTitulo() == null || noticia.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O titulo da noticia nao pode ficar em branco.");
        }

        if (noticia.getDescricao() == null || noticia.getDescricao().trim().isEmpty()) {
            throw new IllegalArgumentException("A descricao da noticia nao pode ficar em branco.");
        }

        if (noticia.getConteudo() == null || noticia.getConteudo().trim().isEmpty()) {
            noticia.setConteudo(noticia.getDescricao());
        }

        if (noticia.getData_publicacao() == null) {
            noticia.setData_publicacao(LocalDate.now());
        }

        if (noticia.getImagem_url() == null || noticia.getImagem_url().trim().isEmpty()) {
            noticia.setImagem_url("/assets/hero-image.png");
        }

        return noticiaRepository.save(noticia);
    }

    public List<Noticia> listarTodas() {
        return noticiaRepository.findAll();
    }

    public Optional<Noticia> buscarPorId(Integer id) {
        return noticiaRepository.findById(id);
    }

    public void deletarNoticia(Integer id) {
        noticiaRepository.deleteById(id);
    }
}
