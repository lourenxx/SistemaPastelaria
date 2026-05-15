package br.edu.faculdade.sistemapastelaria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteLoginDTO {
    private String email;
    private String senha;
}
