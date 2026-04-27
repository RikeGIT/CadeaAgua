package br.com.cadeaagua.api.repository;

import br.com.cadeaagua.api.entity.Cronograma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CronogramaRepository extends JpaRepository<Cronograma, Integer> {

    // Busca o cronograma ativo para uma região em uma data específica
    @Query("SELECT c FROM Cronograma c WHERE c.regiao.id_regiao = :idRegiao " +
            "AND :data BETWEEN c.data_inicio AND c.data_fim")
    List<Cronograma> findByRegiaoAndData(@Param("idRegiao") Integer idRegiao,
                                         @Param("data") LocalDate data);
}
