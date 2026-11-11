package com.suporte.chamados_api;

public record Chamado(
        Long id,
        String titulo,
        String solicitante,
        String setor,
        Prioridade prioridade,
        Status status) {
}
