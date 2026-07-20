# school-management-service

Modulo monolitico residual em processo de descomissionamento na fase D16.

O modulo nao participa do `pom.xml` da raiz. Entretanto, a auditoria de contrato
da D16 encontrou rotas externas usadas pelos frontends que ainda nao possuem
owner modular completo no BFF. Por isso este diretorio ainda nao pode ser
tratado como arquivo historico nem removido da operacao.

Toda evolucao backend deve ocorrer nos servicos modulares ativos; este codigo
serve apenas como fonte de diagnostico dos contratos que faltam migrar.
