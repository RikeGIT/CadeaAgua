package br.com.cadeaagua.api.repository;

import br.com.cadeaagua.api.entity.Noticia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticiaRepository extends JpaRepository<Noticia, Integer> {

    // Busca noticias
    @Query("SELECT n FROM Noticia n WHERE n.titulo LIKE %:palavraChave%")
    List<Noticia> buscarPorPalavraNoTitulo(@Param("palavraChave") String palavraChave);

    // Mostra as noticias mais novas
    @Query("SELECT n FROM Noticia n ORDER BY n.data_publicacao DESC")
    List<Noticia> buscarNoticiasMaisRecentes();

    // Mostra as noticias mais Antigas
    @Query("SELECT n FROM Noticia n ORDER BY n.data_publicacao ASC")
    List<Noticia> buscarNoticiasMaisAntigas();
}