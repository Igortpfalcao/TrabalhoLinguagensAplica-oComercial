<<<<<<< HEAD
# TrabalhoLinguagensAplica-oComercial
=======
# Chamados API

## Executar
    mvn spring-boot:run
Servidor em http://localhost:8080

## Requisicoes de exemplo
    curl http://localhost:8080/chamados
    curl http://localhost:8080/chamados/1
    curl "http://localhost:8080/chamados/setor?nome=financeiro"
    curl "http://localhost:8080/chamados/prioridade?nivel=alta"
    curl "http://localhost:8080/chamados/busca?texto=impressora"
    curl http://localhost:8080/chamados/criticos
    curl http://localhost:8080/chamados/resumo
    curl -i -X POST http://localhost:8080/chamados \
      -H "Content-Type: application/json" \
      -d '{"titulo":"Monitor sem imagem","solicitante":"Maria Silva","setor":"Comercial","prioridade":"ALTA"}'
>>>>>>> 0b38683 (Add everything)
