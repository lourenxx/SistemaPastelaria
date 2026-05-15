package br.edu.faculdade.sistemapastelaria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessaoDTO {
    private String tipo;
    private Long id;
    private String nome;
    private String email;
}
