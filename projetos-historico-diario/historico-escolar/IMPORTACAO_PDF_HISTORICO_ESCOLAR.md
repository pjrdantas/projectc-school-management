# Importação de PDF do Histórico Escolar

Este documento descreve a opção adicionada na tela de Histórico Escolar para importar um arquivo PDF do histórico recebido de outra escola e preencher automaticamente os campos da tela.

## É possível?

Sim, é possível, mas a extração correta deve ser feita no backend Java.

O Angular não deve tentar interpretar sozinho o PDF oficial porque existem dois cenários diferentes:

1. **PDF textual**, gerado por sistema, onde o texto pode ser extraído diretamente.
2. **PDF digitalizado ou imagem**, onde será necessário OCR.

Por isso a tela Angular apenas faz o upload do arquivo e aguarda o backend retornar um payload estruturado com os campos do Histórico Escolar.

## Fluxo implementado na tela

1. Usuário clica em **Importar PDF**.
2. Seleciona um arquivo `.pdf`.
3. Angular envia o arquivo para o backend via `multipart/form-data`.
4. Backend lê o PDF, extrai os dados e devolve o mesmo formato usado pela tela.
5. Angular preenche os campos:
   - cabeçalho da escola;
   - dados do aluno;
   - anos/períodos;
   - componentes curriculares;
   - totais;
   - estudos realizados;
   - observações;
   - certificado.
6. A tela exibe aviso para conferência manual.
7. Usuário confere e clica em **Salvar**.

## Endpoint sugerido

```http
POST /api/historicos-escolares/importacao-pdf
Content-Type: multipart/form-data
```

Campo do arquivo:

```text
arquivo
```

Exemplo usando `curl`:

```bash
curl -X POST \
  http://localhost:8080/api/historicos-escolares/importacao-pdf \
  -F "arquivo=@historico-escolar.pdf"
```

## Response esperado

```json
{
  "historico": {
    "contexto": {
      "modo": "CADASTRO",
      "status": "PENDENTE",
      "serieMatriculaAtual": 6,
      "serieConcluidaOrigem": 5,
      "escolaOrigem": "EE Escola Estadual de Origem",
      "dataTransferencia": "20/06/2026",
      "bloqueado": false
    },
    "cabecalho": {
      "governo": "GOVERNO DO ESTADO DE SÃO PAULO",
      "secretaria": "SECRETARIA DE ESTADO DA EDUCAÇÃO",
      "diretoria": "DIRETORIA DE ENSINO - REGIÃO CARAGUATATUBA",
      "escola": "EE ESCOLA ESTADUAL DE ORIGEM",
      "atoLegal": "Ato Legal de criação:",
      "atoLegalCriacao": "Decreto Estadual nº 00.000/2000",
      "endereco": "Rua das Palmeiras",
      "numero": "100",
      "bairro": "Centro",
      "municipio": "Ubatuba",
      "cep": "11680-000",
      "telefone": "(12) 3832-0000",
      "email": "secretaria@escola.sp.gov.br"
    },
    "aluno": {
      "nome": "Nome do Aluno",
      "rg": "12.345.678-9",
      "ra": "000123456789",
      "nascimentoMunicipio": "Ubatuba",
      "nascimentoEstado": "SP",
      "nascimentoPais": "Brasil",
      "nascimentoData": "10/05/2015"
    },
    "periodos": [],
    "baseComum": [],
    "parteDiversificada": [],
    "totais": {
      "totalBaseComum": [],
      "totalParteDiversificada": [],
      "totalAulasAnuais": [],
      "totalCargaHoraria": []
    },
    "estudosRealizados": [],
    "observacoes": "Dados importados de PDF. Conferir antes de salvar.",
    "certificado": {
      "serieConcluida": 5,
      "diretor": "Diretor da Escola de Origem",
      "escola": "EE Escola Estadual de Origem",
      "rgAluno": "12.345.678-9",
      "ano": "2025",
      "publicacao": "DOE 000/2026",
      "data": "20/06/2026",
      "gerenteNome": "",
      "gerenteRg": "",
      "diretorNome": "Diretor da Escola de Origem",
      "diretorRg": ""
    }
  },
  "nomeArquivo": "historico-escolar.pdf",
  "confiancaGeral": 82,
  "avisos": [
    "Confira os campos extraídos antes de salvar.",
    "Assinaturas e carimbos não são importados automaticamente."
  ],
  "importadoEm": "2026-06-26T18:00:00.000Z"
}
```

## Regras no backend Java

O backend deve:

1. Aceitar somente PDF.
2. Validar tamanho máximo do arquivo.
3. Tentar extrair texto com PDFBox quando o PDF tiver texto pesquisável.
4. Usar OCR quando o PDF for imagem digitalizada.
5. Normalizar os campos para o contrato da tela.
6. Retornar nível de confiança geral.
7. Retornar avisos quando algum campo não for identificado com segurança.
8. Nunca marcar o histórico como completo automaticamente depois da importação.
9. Manter o status como `PENDENTE` até o usuário conferir e salvar.

## Bibliotecas Java sugeridas

Para PDF textual:

```text
Apache PDFBox
```

Para PDF digitalizado/imagem:

```text
Tesseract OCR via tess4j
```

Para normalização e validação:

```text
Java Regex
Bean Validation
DTOs específicos de importação
```

## Observações importantes

A importação deve ser tratada como **pré-preenchimento**, não como verdade absoluta.

Depois de importar, o usuário precisa conferir os dados antes de salvar, porque históricos podem vir de escolas diferentes, com layouts diferentes, campos desalinhados, baixa qualidade de digitalização ou notas pouco legíveis.
