package br.edu.faculdade.sistemapastelaria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Getter
    private long id;

    @Getter @Setter
    private String nome;

    @Getter @Setter
    private String login;

    @Getter @Setter
    private String senha;

    @Getter @Setter
    private boolean ativo;

    public Usuario() {}

    public Usuario(long id, String nome, String login, String senha, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.ativo = ativo;
    }




}
