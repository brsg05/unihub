import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Curso } from '../models/curso.model';

const API_URL = '/api/cursos';

@Injectable({
  providedIn: 'root'
})
export class CursoService {

  constructor(private http: HttpClient) { }

  getAllCursos(): Observable<Curso[]> {
    return this.http.get<Curso[]>(API_URL);
  }

  getCursoById(id: number): Observable<Curso> {
    return this.http.get<Curso>(`${API_URL}/${id}`);
  }

  // The DTO for create/update should only contain the name
  createCurso(cursoData: { nome: string }): Observable<Curso> {
    return this.http.post<Curso>(API_URL, cursoData);
  }

  updateCurso(id: number, cursoData: { nome: string }): Observable<Curso> {
    return this.http.put<Curso>(`${API_URL}/${id}`, cursoData);
  }

  deleteCurso(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`);
  }
} 