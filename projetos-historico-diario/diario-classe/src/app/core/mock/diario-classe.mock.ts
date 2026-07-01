import { DiarioClasseResponse, FrequenciaStatus } from '../models/diario-classe-api.models';

const hojeMock = new Date();
const anoMock = hojeMock.getFullYear();
const mesMock = hojeMock.getMonth() + 1;
const mesMockComZero = String(mesMock).padStart(2, '0');
const dataAtualMock = hojeMock.toISOString().slice(0, 10);

export const DIARIO_CLASSE_MOCK: DiarioClasseResponse = {
  cabecalho: {
    idDiarioClasse: `diario-${anoMock}-${mesMockComZero}-8a-matematica`,
    idEscola: 'esc-001',
    escola: 'EMEF Professora Helena Duarte',
    diretoriaEnsino: 'Centro-Oeste',
    municipio: 'São Paulo',
    anoLetivo: anoMock,
    mes: mesMock,
    dataAtual: dataAtualMock,
    idTurma: 'turma-8a-2026',
    turmaSerie: '8º A',
    turno: 'Manhã',
    idDisciplina: 'disc-matematica',
    disciplina: 'Matemática',
    idProfessor: 'prof-001',
    professor: 'Marina Albuquerque'
  },
  alunos: [
    aluno('aluno-001', 1, 'Ana Clara Santos', ['P', 'P', 'P', 'P', 'F', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'F', '', '', 'P', 'P', 'P', 'P']),
    aluno('aluno-002', 2, 'Bruno Oliveira Silva', ['P', 'P', 'F', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P']),
    aluno('aluno-003', 3, 'Carlos Eduardo Lima', ['P', 'P', 'P', 'P', 'F', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'F', '', '', 'P', 'P', 'P', 'P']),
    aluno('aluno-004', 4, 'Daniela Ferreira Costa', ['P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', '.', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P']),
    aluno('aluno-005', 5, 'Emilly Vitória Rocha', ['P', 'F', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'F', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P']),
    aluno('aluno-006', 6, 'Gabriel Henrique Souza', ['P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'F', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P']),
    aluno('aluno-007', 7, 'Isabela Martins Alves', ['P', 'P', 'P', 'P', 'F', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', '.', 'P', 'F', '', '', 'P', 'P', 'P', 'P']),
    aluno('aluno-008', 8, 'João Pedro Mendes', ['F', 'F', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'F']),
    aluno('aluno-009', 9, 'Kauã Matheus Lima', ['P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P']),
    aluno('aluno-010', 10, 'Larissa Gabriely Dias', ['P', 'F', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P']),
    aluno('aluno-011', 11, 'Maria Eduarda Araújo', ['P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'F', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P']),
    aluno('aluno-012', 12, 'Nathan Alves Cardoso', ['P', 'P', 'F', 'P', 'P', '', '', 'P', 'P', 'P', 'F', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P']),
    aluno('aluno-013', 13, 'Rafaela de Carvalho', ['P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P']),
    aluno('aluno-014', 14, 'Vitor Hugo Martins', ['P', 'F', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'P', 'P', '', '', 'P', 'P', 'P', 'F'])
  ],
  conteudosPlanejados: [
    { idPlanejamentoAula: 'plan-001', periodo: '01 a 05', descricao: 'Números Naturais e Operações' },
    { idPlanejamentoAula: 'plan-002', periodo: '08 a 12', descricao: 'Frações: conceito e representação' },
    { idPlanejamentoAula: 'plan-003', periodo: '15 a 19', descricao: 'Frações equivalentes' },
    { idPlanejamentoAula: 'plan-004', periodo: '22 a 26', descricao: 'Problemas envolvendo frações' },
    { idPlanejamentoAula: 'plan-005', periodo: '29 a 30', descricao: 'Revisão e resolução de exercícios' }
  ],
  observacoes: ['', '', '', '', '', ''],
  avaliacoes: [
    { idAvaliacao: 'aval-001', data: `10/${mesMockComZero}`, descricao: 'Prova Mensal - Números Naturais', turma: '8º A', valor: '0 a 10' },
    { idAvaliacao: 'aval-002', data: `24/${mesMockComZero}`, descricao: 'Trabalho em grupo - Frações', turma: '8º A', valor: '0 a 10' },
    { idAvaliacao: 'aval-003', data: `30/${mesMockComZero}`, descricao: 'Prova Bimestral', turma: '8º A', valor: '0 a 10' }
  ],
  assinatura: {
    nomeProfessor: '',
    dataAssinatura: ''
  },
  bloqueado: false
};

function aluno(idAluno: string, numeroChamada: number, nome: string, frequencia: string[]): DiarioClasseResponse['alunos'][number] {
  return {
    idAluno,
    numeroChamada,
    nome,
    frequencias: Object.fromEntries(Array.from({ length: 31 }, (_, index) => [index + 1, (frequencia[index] ?? '') as FrequenciaStatus | ''])) as Record<number, FrequenciaStatus | ''>
  };
}
