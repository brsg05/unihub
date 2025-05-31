import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProfessorDto, ProfessorDetailDto, ProfessorRequest, Page } from '../../models/professor.model';

@Injectable({
  providedIn: 'root'
})
export class ProfessorService {
  private apiUrl = environment.apiUrl + '/professores';

  constructor(private http: HttpClient) { }

  getAllProfessores(page: number, size: number, filter?: string, periodo?: string): Observable<Page<ProfessorDto>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (filter) {
      params = params.set('filter', filter);
    }
    if (periodo && filter === 'periodo') { // Ensure periodo is only sent if filter is 'periodo'
      params = params.set('periodo', periodo);
    }
    return this.http.get<Page<ProfessorDto>>(this.apiUrl, { params });
  }

  getTopProfessores(limit: number = 5): Observable<ProfessorDto[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<ProfessorDto[]>(`${this.apiUrl}/top`, { params });
  }

  searchProfessores(nome: string, page: number, size: number): Observable<Page<ProfessorDto>> {
    const params = new HttpParams()
      .set('nome', nome)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<ProfessorDto>>(`${this.apiUrl}/search`, { params });
  }

  getProfessorById(id: number): Observable<ProfessorDetailDto> {
    return this.http.get<ProfessorDetailDto>(`${this.apiUrl}/${id}`);
  }

  createProfessor(professorData: ProfessorRequest): Observable<ProfessorDto> {
    return this.http.post<ProfessorDto>(this.apiUrl, professorData);
  }

  updateProfessor(id: number, professorData: ProfessorRequest): Observable<ProfessorDto> {
    return this.http.put<ProfessorDto>(`${this.apiUrl}/${id}`, professorData);
  }

  deleteProfessor(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
} 