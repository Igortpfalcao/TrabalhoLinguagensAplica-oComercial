package com.suporte.chamados_api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record NovoChamadoRequest(
        @NotBlank(message = "titulo e obrigatorio") String titulo,
        @NotBlank(message = "solicitante e obrigatorio") String solicitante,
        @NotBlank(message = "setor e obrigatorio") String setor,
        @NotNull(message = "prioridade e obrigatoria (BAIXA, MEDIA ou ALTA)") Prioridade prioridade,
        Status status) {
}
