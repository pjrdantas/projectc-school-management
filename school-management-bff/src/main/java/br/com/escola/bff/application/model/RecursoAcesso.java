package br.com.escola.bff.application.model;

public enum RecursoAcesso {
    USUARIOS("usuarios"),
    PERFIS("perfis"),
    PERMISSOES("permissoes");

    private final String path;

    RecursoAcesso(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }
}
