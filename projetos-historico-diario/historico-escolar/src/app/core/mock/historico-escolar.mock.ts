import { HistoricoEscolarResponse } from '../models/historico-escolar-api.models';

const criarValores = () => Array.from({ length: 9 }, () => '');

export const HISTORICO_ESCOLAR_CADASTRO_MOCK: HistoricoEscolarResponse = {
  contexto: {
    modo: 'CADASTRO',
    status: 'RASCUNHO',
    serieMatriculaAtual: 6,
    serieConcluidaOrigem: 5,
    escolaOrigem: '',
    dataTransferencia: '',
    bloqueado: false
  },
  cabecalho: {
    governo: '',
    secretaria: '',
    diretoria: '',
    escola: '',
    atoLegal: 'Ato Legal de criação:',
    atoLegalCriacao: '',
    endereco: '',
    numero: '',
    bairro: '',
    municipio: '',
    cep: '',
    telefone: '',
    email: ''
  },
  aluno: {
    nome: '',
    rg: '',
    ra: '',
    nascimentoMunicipio: '',
    nascimentoEstado: '',
    nascimentoPais: '',
    nascimentoData: ''
  },
  periodos: [
    { ordem: 1, anoLetivo: '', serie: '1º Ano', equivalencia: '1ª série' },
    { ordem: 2, anoLetivo: '', serie: '2º Ano', equivalencia: '1ª série' },
    { ordem: 3, anoLetivo: '', serie: '3º Ano', equivalencia: '2ª série' },
    { ordem: 4, anoLetivo: '', serie: '4º Ano', equivalencia: '3ª série' },
    { ordem: 5, anoLetivo: '', serie: '5º Ano', equivalencia: '4ª série' },
    { ordem: 6, anoLetivo: '', serie: '6º Ano', equivalencia: '5ª série' },
    { ordem: 7, anoLetivo: '', serie: '7º Ano', equivalencia: '6ª série' },
    { ordem: 8, anoLetivo: '', serie: '8º Ano', equivalencia: '7ª série' },
    { ordem: 9, anoLetivo: '', serie: '9º Ano', equivalencia: '8ª série' }
  ],
  baseComum: [
    { ordem: 1, nome: 'Língua Portuguesa', valores: criarValores() },
    { ordem: 2, nome: 'Língua Estrangeira - Inglês', valores: criarValores() },
    { ordem: 3, nome: 'Arte', valores: criarValores() },
    { ordem: 4, nome: 'Educação Física', valores: criarValores() },
    { ordem: 5, nome: 'História', valores: criarValores() },
    { ordem: 6, nome: 'Geografia', valores: criarValores() },
    { ordem: 7, nome: 'Matemática', valores: criarValores() },
    { ordem: 8, nome: 'Ciências Físicas e Biológicas', valores: criarValores() },
    { ordem: 9, nome: 'Ensino Religioso', valores: criarValores() }
  ],
  parteDiversificada: [
    { ordem: 1, nome: 'Projeto de Vida', valores: criarValores() },
    { ordem: 2, nome: 'Tecnologia e Inovação', valores: criarValores() },
    { ordem: 3, nome: '', valores: criarValores() },
    { ordem: 4, nome: '', valores: criarValores() },
    { ordem: 5, nome: '', valores: criarValores() },
    { ordem: 6, nome: '', valores: criarValores() },
    { ordem: 7, nome: '', valores: criarValores() },
    { ordem: 8, nome: '', valores: criarValores() },
    { ordem: 9, nome: '', valores: criarValores() },
    { ordem: 10, nome: '', valores: criarValores() }
  ],
  totais: {
    totalBaseComum: criarValores(),
    totalParteDiversificada: criarValores(),
    totalAulasAnuais: criarValores(),
    totalCargaHoraria: criarValores()
  },
  estudosRealizados: [
    { ordem: 1, serieAno: '1º Ano', ano: '', escola: '', municipio: '', uf: '' },
    { ordem: 2, serieAno: '2º Ano', ano: '', escola: '', municipio: '', uf: '' },
    { ordem: 3, serieAno: '3º Ano', ano: '', escola: '', municipio: '', uf: '' },
    { ordem: 4, serieAno: '4º Ano', ano: '', escola: '', municipio: '', uf: '' },
    { ordem: 5, serieAno: '5º Ano', ano: '', escola: '', municipio: '', uf: '' },
    { ordem: 6, serieAno: '6º Ano', ano: '', escola: '', municipio: '', uf: '' },
    { ordem: 7, serieAno: '7º Ano', ano: '', escola: '', municipio: '', uf: '' },
    { ordem: 8, serieAno: '8º Ano', ano: '', escola: '', municipio: '', uf: '' },
    { ordem: 9, serieAno: '9º Ano', ano: '', escola: '', municipio: '', uf: '' }
  ],
  observacoes: '',
  certificado: {
    serieConcluida: 5,
    diretor: '',
    escola: '',
    rgAluno: '',
    ano: '',
    publicacao: '',
    data: '',
    gerenteNome: '',
    gerenteRg: '',
    diretorNome: '',
    diretorRg: ''
  },
  pendencias: []
};


export const HISTORICO_ESCOLAR_IMPORTACAO_PDF_MOCK: HistoricoEscolarResponse = {
  contexto: {
    modo: 'CADASTRO',
    status: 'PENDENTE',
    serieMatriculaAtual: 6,
    serieConcluidaOrigem: 5,
    escolaOrigem: 'EE Escola Estadual de Origem',
    dataTransferencia: '20/06/2026',
    bloqueado: false
  },
  cabecalho: {
    governo: 'GOVERNO DO ESTADO DE SÃO PAULO',
    secretaria: 'SECRETARIA DE ESTADO DA EDUCAÇÃO',
    diretoria: 'DIRETORIA DE ENSINO - REGIÃO CARAGUATATUBA',
    escola: 'EE ESCOLA ESTADUAL DE ORIGEM',
    atoLegal: 'Ato Legal de criação:',
    atoLegalCriacao: 'Decreto Estadual nº 00.000/2000',
    endereco: 'Rua das Palmeiras',
    numero: '100',
    bairro: 'Centro',
    municipio: 'Ubatuba',
    cep: '11680-000',
    telefone: '(12) 3832-0000',
    email: 'secretaria@escola.sp.gov.br'
  },
  aluno: {
    nome: 'Aluno Importado do PDF',
    rg: '12.345.678-9',
    ra: '000123456789',
    nascimentoMunicipio: 'Ubatuba',
    nascimentoEstado: 'SP',
    nascimentoPais: 'Brasil',
    nascimentoData: '10/05/2015'
  },
  periodos: [
    { ordem: 1, anoLetivo: '2021', serie: '1º Ano', equivalencia: '1ª série' },
    { ordem: 2, anoLetivo: '2022', serie: '2º Ano', equivalencia: '1ª série' },
    { ordem: 3, anoLetivo: '2023', serie: '3º Ano', equivalencia: '2ª série' },
    { ordem: 4, anoLetivo: '2024', serie: '4º Ano', equivalencia: '3ª série' },
    { ordem: 5, anoLetivo: '2025', serie: '5º Ano', equivalencia: '4ª série' },
    { ordem: 6, anoLetivo: '', serie: '6º Ano', equivalencia: '5ª série' },
    { ordem: 7, anoLetivo: '', serie: '7º Ano', equivalencia: '6ª série' },
    { ordem: 8, anoLetivo: '', serie: '8º Ano', equivalencia: '7ª série' },
    { ordem: 9, anoLetivo: '', serie: '9º Ano', equivalencia: '8ª série' }
  ],
  baseComum: [
    { ordem: 1, nome: 'Língua Portuguesa', valores: ['8,0', '8,5', '9,0', '8,0', '8,5', '', '', '', ''] },
    { ordem: 2, nome: 'Língua Estrangeira - Inglês', valores: ['', '', '8,0', '8,0', '8,0', '', '', '', ''] },
    { ordem: 3, nome: 'Arte', valores: ['9,0', '8,0', '8,5', '9,0', '8,0', '', '', '', ''] },
    { ordem: 4, nome: 'Educação Física', valores: ['9,0', '9,0', '9,0', '8,5', '9,0', '', '', '', ''] },
    { ordem: 5, nome: 'História', valores: ['8,0', '8,0', '8,0', '8,5', '8,0', '', '', '', ''] },
    { ordem: 6, nome: 'Geografia', valores: ['8,0', '8,5', '8,0', '8,5', '8,0', '', '', '', ''] },
    { ordem: 7, nome: 'Matemática', valores: ['7,5', '8,0', '8,0', '8,0', '8,5', '', '', '', ''] },
    { ordem: 8, nome: 'Ciências Físicas e Biológicas', valores: ['8,0', '8,0', '8,5', '8,0', '8,0', '', '', '', ''] },
    { ordem: 9, nome: 'Ensino Religioso', valores: ['S', 'S', 'S', 'S', 'S', '', '', '', ''] }
  ],
  parteDiversificada: [
    { ordem: 1, nome: 'Projeto de Vida', valores: ['S', 'S', 'S', 'S', 'S', '', '', '', ''] },
    { ordem: 2, nome: 'Tecnologia e Inovação', valores: ['S', 'S', 'S', 'S', 'S', '', '', '', ''] },
    { ordem: 3, nome: '', valores: criarValores() },
    { ordem: 4, nome: '', valores: criarValores() },
    { ordem: 5, nome: '', valores: criarValores() },
    { ordem: 6, nome: '', valores: criarValores() },
    { ordem: 7, nome: '', valores: criarValores() },
    { ordem: 8, nome: '', valores: criarValores() },
    { ordem: 9, nome: '', valores: criarValores() },
    { ordem: 10, nome: '', valores: criarValores() }
  ],
  totais: {
    totalBaseComum: ['1000', '1000', '1000', '1000', '1000', '', '', '', ''],
    totalParteDiversificada: ['', '', '', '', '80', '', '', '', ''],
    totalAulasAnuais: ['1000', '1000', '1000', '1000', '1080', '', '', '', ''],
    totalCargaHoraria: ['833', '833', '833', '833', '900', '', '', '', '']
  },
  estudosRealizados: [
    { ordem: 1, serieAno: '1º Ano', ano: '2021', escola: 'EE Escola Estadual de Origem', municipio: 'Ubatuba', uf: 'SP' },
    { ordem: 2, serieAno: '2º Ano', ano: '2022', escola: 'EE Escola Estadual de Origem', municipio: 'Ubatuba', uf: 'SP' },
    { ordem: 3, serieAno: '3º Ano', ano: '2023', escola: 'EE Escola Estadual de Origem', municipio: 'Ubatuba', uf: 'SP' },
    { ordem: 4, serieAno: '4º Ano', ano: '2024', escola: 'EE Escola Estadual de Origem', municipio: 'Ubatuba', uf: 'SP' },
    { ordem: 5, serieAno: '5º Ano', ano: '2025', escola: 'EE Escola Estadual de Origem', municipio: 'Ubatuba', uf: 'SP' },
    { ordem: 6, serieAno: '6º Ano', ano: '', escola: '', municipio: '', uf: '' },
    { ordem: 7, serieAno: '7º Ano', ano: '', escola: '', municipio: '', uf: '' },
    { ordem: 8, serieAno: '8º Ano', ano: '', escola: '', municipio: '', uf: '' },
    { ordem: 9, serieAno: '9º Ano', ano: '', escola: '', municipio: '', uf: '' }
  ],
  observacoes: 'Dados importados de PDF. Conferir todos os campos antes de salvar.',
  certificado: {
    serieConcluida: 5,
    diretor: 'Diretor da Escola de Origem',
    escola: 'EE Escola Estadual de Origem',
    rgAluno: '12.345.678-9',
    ano: '2025',
    publicacao: 'DOE 000/2026',
    data: '20/06/2026',
    gerenteNome: '',
    gerenteRg: '',
    diretorNome: 'Diretor da Escola de Origem',
    diretorRg: ''
  },
  pendencias: [
    {
      codigo: 'CONFERIR_IMPORTACAO_PDF',
      severidade: 'AVISO',
      aba: 'CADASTRO',
      mensagem: 'Dados importados automaticamente do PDF. Confira os campos antes de salvar.'
    }
  ]
};
