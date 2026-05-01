import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function urlValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) return null; // permite vazio
    try {
      const url = new URL(control.value); // valida URL
      return null; // URL válida
    } catch (e) {
      return { invalidUrl: true }; // URL inválida
    }
  };
}
