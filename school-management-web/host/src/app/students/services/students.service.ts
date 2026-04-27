import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Student, StudentInput } from '../models/student.model';

const STORAGE_KEY = 'students-crud-v1';

@Injectable({ providedIn: 'root' })
export class StudentsService {
  private readonly studentsSubject = new BehaviorSubject<Student[]>(this.load());
  readonly students$ = this.studentsSubject.asObservable();

  list(): Student[] {
    return this.studentsSubject.value;
  }

  getById(id: string): Student | undefined {
    return this.studentsSubject.value.find(student => student.id === id);
  }

  isCpfInUse(cpf: string, exceptStudentId?: string): boolean {
    return this.studentsSubject.value.some(
      student => student.cpf === cpf && student.id !== exceptStudentId,
    );
  }

  create(input: StudentInput): Student {
    const student: Student = {
      id: crypto.randomUUID(),
      ...input,
      createdAt: new Date().toISOString(),
    };

    this.commit([student, ...this.studentsSubject.value]);
    return student;
  }

  update(id: string, input: StudentInput): Student | null {
    let updated: Student | null = null;

    const next = this.studentsSubject.value.map(student => {
      if (student.id !== id) {
        return student;
      }

      updated = { ...student, ...input };
      return updated;
    });

    if (!updated) {
      return null;
    }

    this.commit(next);
    return updated;
  }

  remove(id: string): boolean {
    const next = this.studentsSubject.value.filter(student => student.id !== id);
    if (next.length === this.studentsSubject.value.length) {
      return false;
    }

    this.commit(next);
    return true;
  }

  private commit(students: Student[]): void {
    this.studentsSubject.next(students);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(students));
  }

  private load(): Student[] {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
      return [];
    }

    try {
      return JSON.parse(raw) as Student[];
    } catch {
      return [];
    }
  }
}
