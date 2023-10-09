package br.com.femass.ProjetoDS1.controller;

//import br.com.femass.ProjetoDS1.domain.AutorComplementar.AutorComplementar;
//import br.com.femass.ProjetoDS1.domain.AutorComplementar.AutorComplementarRepository;
import br.com.femass.ProjetoDS1.domain.ValidacaoException;
import br.com.femass.ProjetoDS1.domain.pesquisador.Pesquisador;
import br.com.femass.ProjetoDS1.domain.pesquisador.PesquisadorRepository;
import br.com.femass.ProjetoDS1.domain.producao.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/producao")
public class ProducaoController {
    @Autowired
    private PesquisadorRepository repositoryPesquisador;
    @Autowired
    private ProducaoRepository repository;





    @PostMapping
    public ResponseEntity <Page<DadosListagemProducao>> cadastro(@PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC, size = Integer.MAX_VALUE ) Pageable paginacao, @RequestBody @Valid DadosCadastroProducao dados, UriComponentsBuilder uriBuilder){

        var producao = new Producao(dados);
        var finded = producao.encontrarArtigos(producao.EncontrarXML(dados.idPesquisador()));
        var livros = producao.encontrarLivroeCapitulo(producao.EncontrarXML(dados.idPesquisador()));
        var idProd = new Pesquisador();
        boolean flag = false;
        int conta = 0;
        for (var tot : finded) {
            String[] partes = tot.split("-(\\d)");

            if (partes.length == 2) {
                conta++;
                var prod = new Producao();
                prod.setStatus(true);
                prod.setTipo(Tipo.ARTIGO);
                String tituloDoArtigo = partes[0];
                String anoDoArtigo = partes[1];

                prod.setTitulo(tituloDoArtigo);
                prod.setAno(anoDoArtigo);

                var pesquisadorFind = repositoryPesquisador.getReferenceByidXMLAndStatusTrue(dados.idPesquisador());
                if(pesquisadorFind == null){
                    throw new ValidacaoException("Pesquisador não existe no banco");
                }

                var pesquisador = repositoryPesquisador.findAllByIdXMLAndIdAndStatusTrue(dados.idPesquisador(), pesquisadorFind.getId());
                var prodRepo = repository.getReferenceByTitulo(tituloDoArtigo);
                var pesquisadorArtigo = repository.findAllByPesquisadorIdAndTituloAndStatusTrue(pesquisadorFind.getId(), tituloDoArtigo);
                if(!pesquisadorArtigo.isEmpty()){
                    continue;
                }

                if(prodRepo != null){
                    var pesquisadorNovo = repositoryPesquisador.getReferenceByidXMLAndStatusTrue(dados.idPesquisador());
                    prodRepo.adicionar(pesquisadorNovo);

                    System.out.println(prodRepo);
                    repository.save(prodRepo);
                    idProd = pesquisadorFind;
                    continue;
                }

                prod.setPesquisador(pesquisador);

                System.out.println(prod);
                repository.save(prod);
                if (flag == false) {
                    idProd = pesquisadorFind;
                    flag = true;
                }
            }


        }
        for(var livro : livros){
            String[] partes = livro.split("-(\\d)");
            if (partes.length == 2) {
                conta++;
                var prod = new Producao();
                prod.setStatus(true);
                prod.setTipo(Tipo.LIVRO);
                String tituloDoArtigo = partes[0];
                String anoDoArtigo = partes[1];

                prod.setTitulo(tituloDoArtigo);
                prod.setAno(anoDoArtigo);

                var pesquisadorFind = repositoryPesquisador.getReferenceByidXMLAndStatusTrue(dados.idPesquisador());
                if(pesquisadorFind == null){
                    System.out.println("pesquisador não existe no banco");
                    return ResponseEntity.badRequest().build();
                }

                var pesquisador = repositoryPesquisador.findAllByIdXMLAndIdAndStatusTrue(dados.idPesquisador(), pesquisadorFind.getId());
                var prodRepo = repository.getReferenceByTitulo(tituloDoArtigo);
                var pesquisadorArtigo = repository.findAllByPesquisadorIdAndTituloAndStatusTrue(pesquisadorFind.getId(), tituloDoArtigo);
                if(!pesquisadorArtigo.isEmpty()){
                    if (conta>1){
                        System.out.println("item já cadastrado");
                        continue;
                    }else {
                        continue;
                    }
                }
                if(prodRepo != null){
                    var pesquisadorNovo = repositoryPesquisador.getReferenceByidXMLAndStatusTrue(dados.idPesquisador());
                    prodRepo.adicionar(pesquisadorNovo);

                    System.out.println(prodRepo);
                    repository.save(prodRepo);
                    idProd = pesquisadorFind;
                }

                prod.setPesquisador(pesquisador);

                System.out.println(prod);
                repository.save(prod);
                if (flag == false) {
                    idProd = pesquisadorFind;
                    flag = true;
                }
            }


        }
        if (!finded.isEmpty()){
            System.out.println("batata");
            var findAll = repository.findAllByPesquisadorIdAndStatusTrue(idProd.getId(), paginacao).map(DadosListagemProducao::new);

            var uri = uriBuilder.path("producao/id={id}").buildAndExpand(repositoryPesquisador.getReferenceByIdAndStatusTrue(idProd.getId())).toUri();

            return ResponseEntity.created(uri).body(findAll);
        }

        return ResponseEntity.notFound().build();


    }


    @GetMapping
    public ResponseEntity <Page<DadosListagemProducao>>listar (@PageableDefault (direction = Sort.Direction.DESC, size = Integer.MAX_VALUE)Pageable paginacao){
        var page = repository.findAllByStatusTrue(paginacao).map(DadosListagemProducao::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/maisdeumpesquisador")
    public ResponseEntity <Page<DadosListagemProducao>> listarMaisDeUm (@PageableDefault (direction = Sort.Direction.DESC, size = Integer.MAX_VALUE)Pageable paginacao){
        var page = repository.encontrarProducaoComMaisDeUmPesquisador(paginacao).map(DadosListagemProducao::new);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/xml={xml}")
    public ResponseEntity <Page<DadosListagemProducao>> listarPorIdPesquisador(@PageableDefault (direction = Sort.Direction.DESC, size = Integer.MAX_VALUE)Pageable paginacao, @PathVariable String xml){
        var pesquisador = repositoryPesquisador.getReferenceByidXMLAndStatusTrue(xml);

        var page = repository.findAllByPesquisadorIdAndStatusTrue(pesquisador.getId(), paginacao).map(DadosListagemProducao::new);

        return ResponseEntity.ok(page);
    }

    @GetMapping("/datas={anoInicio}-{anoFim}")
    public ResponseEntity <Page<DadosListagemProducao>> listarPorData(@PageableDefault (direction = Sort.Direction.DESC, size = Integer.MAX_VALUE)Pageable paginacao, @PathVariable String anoInicio, @PathVariable String anoFim){
        var page = repository.findAllByAnoBetweenAno(anoInicio, anoFim, paginacao).map(DadosListagemProducao::new);

        return ResponseEntity.ok(page);
    }

    @GetMapping("/pesquisador={XML}/datas={anoInicial}-{anoFinal}")
    public ResponseEntity <Page<DadosListagemProducao>> listarPorDataAndXML(@PageableDefault (direction = Sort.Direction.DESC, size = Integer.MAX_VALUE)Pageable paginacao, @PathVariable String XML, @PathVariable String anoInicial, @PathVariable String anoFinal){
        var pesquisador = repositoryPesquisador.getReferenceByidXMLAndStatusTrue(XML);

        var page = repository.findAllByAnoBetweenAndPesquisadorId(pesquisador.getId(), anoInicial, anoFinal, paginacao).map(DadosListagemProducao::new);

        return ResponseEntity.ok(page);
    }

    private record MensagemError(String mensagem){

    }
}
