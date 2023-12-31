package br.com.femass.ProjetoDS1.repository.producao;

import br.com.femass.ProjetoDS1.domain.autor.Autor;
import br.com.femass.ProjetoDS1.domain.producao.Producao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProducaoRepository extends JpaRepository<Producao, Long> {



    Page<Producao> findAll(Pageable paginacao);

    Producao getReferenceByTitulo(String tituloDoArtigo);

    //List<Producao> findAllByAutoresId (Long id, String titulo);

    Page<Producao> findAllByStatusTrue(Pageable paginacao);



//    List<Producao> findAllByPesquisadorIdAndStatusTrue(Long id);


    @Query("Select p from Producao p where p.ano between :anoInicial and :anoFinal ")
    Page<Producao> findAllByAnoBetweenAno(String anoInicial, String anoFinal, Pageable pageable);


    @Query("SELECT p FROM Producao p JOIN p.autores a WHERE a.id = :autorId AND p.titulo = :tituloDoArtigo")
    List<Producao> findAllByAutorIdAndTituto(@Param("autorId") Long id, @Param("tituloDoArtigo") String tituloDoArtigo);

    @Query("SELECT DISTINCT p FROM Producao p JOIN p.autores a WHERE a.id = :autorId")
    Page<Producao> findAllByAutorId(@Param("autorId") Long id, Pageable pageable);

    @Query("SELECT p FROM Producao p " +
            "JOIN p.autores autor " +
            "WHERE SIZE(p.autores) > 1 " +
            "AND TYPE(autor) = Pesquisador " +
            "GROUP BY p " +
            "HAVING COUNT(autor) > 1")
    Page<Producao> encontrarProducaoComMaisDeUmPesquisador(Pageable paginacao);


    @Query("SELECT p FROM Producao p JOIN p.autores a WHERE a.id = :autorId and p.ano between :anoInicial and :anoFinal")
    Page<Producao> findAllByAnoBetweenAndAutorId(@Param("autorId") Long id, @Param("anoInicial") String anoInicial, @Param("anoFinal") String anoFinal, Pageable paginacao);

    @Query("SELECT p FROM Producao p JOIN p.autores a JOIN Pesquisador pesq ON a.id = pesq.id WHERE pesq.instituto.id = :id and pesq.id= :idPesq")
    Page<Producao> findAllByIdInstituto(Long id, Long idPesq, Pageable paginacao);

    Page<Producao> findAllByAutoresIsInAndStatusTrue(List<Autor> autores, Pageable paginacao);

    Page<Producao> findAllByAutoresIsInAndStatusTrueAndAnoBetween(List<Autor> autores,String dataInicial, String dataFinal, Pageable paginacao);

    @Query("SELECT COUNT(p) FROM Producao p")
    Long totalProducao();
}
