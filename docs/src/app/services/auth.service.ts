import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Auth } from '../models/auth.model';
import { AuthRequest } from '../models/auth-request.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = '/api/usuarios';

  constructor(private http: HttpClient) {}

  findAll(): Observable<Auth[]> {
    return this.http.get<Auth[]>(this.baseUrl);
  }

  findById(id: number): Observable<Auth> {
    return this.http.get<Auth>(`${this.baseUrl}/${id}`);
  }

  create(user: AuthRequest): Observable<Auth> {
    return this.http.post<Auth>(this.baseUrl, user);
  }

  update(id: number, user: AuthRequest): Observable<Auth> {
    return this.http.put<Auth>(`${this.baseUrl}/${id}`, user);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
