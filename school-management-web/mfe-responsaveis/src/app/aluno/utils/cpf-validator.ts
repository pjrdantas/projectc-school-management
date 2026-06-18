export function validarCPF(cpf: string): boolean {
  cpf = cpf.replace(/\D/g, '');

  if (cpf.length !== 11 || /^(\d)\1+$/.test(cpf)) return false;

  let soma = 0;
  for (let i = 0; i < 9; i++)
    soma += parseInt(cpf[i]) * (10 - i);

  let d1 = (soma * 10) % 11;
  if (d1 === 10) d1 = 0;

  soma = 0;
  for (let i = 0; i < 10; i++)
    soma += parseInt(cpf[i]) * (11 - i);

  let d2 = (soma * 10) % 11;
  if (d2 === 10) d2 = 0;

  return d1 === +cpf[9] && d2 === +cpf[10];
}
