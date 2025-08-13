import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Professor, ProfessorDetailDto } from '../models/professor.model';

const API_URL = '/api/professores';

@Injectable({
  providedIn: 'root'
})
export class ProfessorService {

  constructor(private http: HttpClient) { }

  getAllProfessores(page: number = 0, size: number = 10, filter?: string, periodo?: string): Observable<any> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (filter) {
      params = params.set('filter', filter);
    }
    
    if (periodo) {
      params = params.set('periodo', periodo);
    }

    return this.http.get<any>(API_URL, { params });
  }

  getTopProfessores(limit: number = 5): Observable<Professor[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<Professor[]>(`${API_URL}/top`, { params });
  }

  searchProfessores(nome: string, page: number = 0, size: number = 10): Observable<any> {
    const params = new HttpParams()
      .set('nome', nome)
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<any>(`${API_URL}/search`, { params });
  }

  getProfessorById(id: number): Observable<ProfessorDetailDto> {
    return this.http.get<ProfessorDetailDto>(`${API_URL}/${id}`);
  }

  createProfessor(professorData: any): Observable<Professor> {
    return this.http.post<Professor>(API_URL, professorData);
  }

  updateProfessor(id: number, professorData: any): Observable<Professor> {
    return this.http.put<Professor>(`${API_URL}/${id}`, professorData);
  }

  deleteProfessor(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`);
  }
}
