package br.com.femass.ProjetoDS1.repository.autorComplementar;

import br.com.femass.ProjetoDS1.domain.autorComplementar.AutorComplementar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutorComplementarRepository extends JpaRepository<AutorComplementar, Long> {

    List<AutorComplementar> findAllByNomeCita(String nomeCita);
    List<AutorComplementar> findAllByNomeCompleto(String nomeCompleto);
    AutorComplementar getReferenceByNomeCita(String nomeCita);
    AutorComplementar getReferenceByNomeCompleto(String nomeCompleto);


}
