package com.suporte.chamados_api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    // "Banco de dados" em memoria (thread-safe)
    private final List<Chamado> chamados = new CopyOnWriteArrayList<>();
    private final AtomicLong sequencia = new AtomicLong(0);

    public ChamadoController() {
        carregarDadosIniciais();
    }


    @GetMapping
    public List<Chamado> listarTodos() {
        return chamados;
    }

    @GetMapping("/{id}")
    public Chamado buscarPorId(@PathVariable Long id) {
        return chamados.stream()
                .filter(c -> c.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Chamado " + id + " nao encontrado"));
    }

    @GetMapping("/setor")
    public List<Chamado> filtrarPorSetor(@RequestParam String nome) {
        String alvo = normalizar(nome);
        return chamados.stream()
                .filter(c -> normalizar(c.setor()).equals(alvo))
                .toList();
    }


    @GetMapping("/prioridade")
    public List<Chamado> filtrarPorPrioridade(@RequestParam String nivel) {
        Prioridade prioridade = converterPrioridade(nivel);
        return chamados.stream()
                .filter(c -> c.prioridade() == prioridade)
                .toList();
    }

    @GetMapping("/busca")
    public List<Chamado> buscarPorTitulo(@RequestParam String texto) {
        String trecho = normalizar(texto);
        return chamados.stream()
                .filter(c -> normalizar(c.titulo()).contains(trecho))
                .toList();
    }


    @GetMapping("/criticos")
    public List<Chamado> listarCriticos() {
        return chamados.stream()
                .filter(c -> c.prioridade() == Prioridade.ALTA)
                .filter(c -> c.status() != Status.RESOLVIDO)
                .toList();
    }


    @GetMapping("/resumo")
    public Map<String, Long> resumo() {
        Map<String, Long> resumo = new LinkedHashMap<>();
        resumo.put("abertos", contar(Status.ABERTO));
        resumo.put("emAndamento", contar(Status.EM_ANDAMENTO));
        resumo.put("resolvidos", contar(Status.RESOLVIDO));
        resumo.put("total", (long) chamados.size());
        return resumo;
    }

    @PostMapping
    public ResponseEntity<Chamado> cadastrar(@Valid @RequestBody NovoChamadoRequest req) {
        Chamado novo = new Chamado(
                sequencia.incrementAndGet(),
                req.titulo().trim(),
                req.solicitante().trim(),
                req.setor().trim(),
                req.prioridade(),
                req.status() != null ? req.status() : Status.ABERTO);

        chamados.add(novo);

        return ResponseEntity
                .created(URI.create("/chamados/" + novo.id()))
                .body(novo);
    }



    private long contar(Status status) {
        return chamados.stream().filter(c -> c.status() == status).count();
    }

    /** Remove acentos e ignora maiusculas/minusculas ("Médio" == "medio"). */
    private String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT);
    }

    private Prioridade converterPrioridade(String nivel) {
        try {
            return Prioridade.valueOf(normalizar(nivel).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Prioridade invalida: '" + nivel + "'. Use baixa, media ou alta.");
        }
    }

    private void adicionar(String titulo, String solicitante, String setor,
                           Prioridade prioridade, Status status) {
        chamados.add(new Chamado(sequencia.incrementAndGet(),
                titulo, solicitante, setor, prioridade, status));
    }

    private void carregarDadosIniciais() {
        adicionar("Impressora do 2o andar nao imprime", "Ana Souza", "Financeiro", Prioridade.MEDIA, Status.ABERTO);
        adicionar("Servidor de arquivos fora do ar", "Carlos Lima", "TI", Prioridade.ALTA, Status.EM_ANDAMENTO);
        adicionar("Solicitar instalacao do Office", "Beatriz Alves", "RH", Prioridade.BAIXA, Status.RESOLVIDO);
        adicionar("Sistema de folha de pagamento com erro", "Marcos Pereira", "RH", Prioridade.ALTA, Status.ABERTO);
        adicionar("Troca de mouse e teclado", "Juliana Costa", "Comercial", Prioridade.BAIXA, Status.ABERTO);
        adicionar("Impressora fiscal travando", "Roberto Dias", "Financeiro", Prioridade.ALTA, Status.ABERTO);
        adicionar("VPN nao conecta em home office", "Fernanda Rocha", "Comercial", Prioridade.MEDIA, Status.EM_ANDAMENTO);
        adicionar("Acesso negado ao ERP", "Paulo Menezes", "Financeiro", Prioridade.ALTA, Status.RESOLVIDO);
        adicionar("Lentidao na rede do almoxarifado", "Luciana Torres", "Logistica", Prioridade.MEDIA, Status.ABERTO);
        adicionar("Backup diario falhou", "Carlos Lima", "TI", Prioridade.ALTA, Status.ABERTO);
        adicionar("Configurar e-mail de novo colaborador", "Beatriz Alves", "RH", Prioridade.BAIXA, Status.EM_ANDAMENTO);
        adicionar("Scanner de codigo de barras sem leitura", "Thiago Nunes", "Logistica", Prioridade.MEDIA, Status.RESOLVIDO);
    }
}
