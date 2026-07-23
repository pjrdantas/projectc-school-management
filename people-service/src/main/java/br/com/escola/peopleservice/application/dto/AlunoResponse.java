package br.com.escola.peopleservice.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.peopleservice.application.model.AlunoCriado;
import br.com.escola.peopleservice.application.model.AlunoAtualizado;
import br.com.escola.peopleservice.application.model.AlunoConsulta;

public record AlunoResponse(
        UUID id,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        LocalDate dataNascimento,
        String rg,
        String orgaoEmissorRg,
        String ufRg,
        String nacionalidade,
        String naturalidade,
        String sexo,
        String nomeSocial,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String statusAluno,
        UUID escolaId,
        String escolaNome,
        LocalDateTime createdAt) {

    public static AlunoResponse from(AlunoCriado aluno) {
        return new AlunoResponse(
                aluno.id(), aluno.nomeCompleto(), aluno.cpf(), aluno.email(), aluno.telefone(),
                aluno.dataNascimento(), aluno.rg(), aluno.orgaoEmissorRg(), aluno.ufRg(),
                aluno.nacionalidade(), aluno.naturalidade(), aluno.sexo(), aluno.nomeSocial(),
                aluno.cep(), aluno.logradouro(), aluno.numero(), aluno.complemento(), aluno.bairro(),
                aluno.cidade(), aluno.uf(), aluno.statusAluno(), aluno.escolaId(), aluno.escolaNome(),
                aluno.createdAt());
    }

    public static AlunoResponse from(AlunoAtualizado aluno) {
        return new AlunoResponse(
                aluno.id(), aluno.nomeCompleto(), aluno.cpf(), aluno.email(), aluno.telefone(),
                aluno.dataNascimento(), aluno.rg(), aluno.orgaoEmissorRg(), aluno.ufRg(),
                aluno.nacionalidade(), aluno.naturalidade(), aluno.sexo(), aluno.nomeSocial(),
                aluno.cep(), aluno.logradouro(), aluno.numero(), aluno.complemento(), aluno.bairro(),
                aluno.cidade(), aluno.uf(), aluno.statusAluno(), aluno.escolaId(), aluno.escolaNome(),
                aluno.createdAt());
    }

    public static AlunoResponse from(AlunoConsulta aluno) {
        return new AlunoResponse(
                aluno.id(), aluno.nomeCompleto(), aluno.cpf(), aluno.email(), aluno.telefone(),
                aluno.dataNascimento(), aluno.rg(), aluno.orgaoEmissorRg(), aluno.ufRg(),
                aluno.nacionalidade(), aluno.naturalidade(), aluno.sexo(), aluno.nomeSocial(),
                aluno.cep(), aluno.logradouro(), aluno.numero(), aluno.complemento(), aluno.bairro(),
                aluno.cidade(), aluno.uf(), aluno.statusAluno(), aluno.escolaId(), aluno.escolaNome(),
                aluno.createdAt());
    }
}
