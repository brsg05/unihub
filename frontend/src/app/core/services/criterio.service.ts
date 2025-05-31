import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Criterio } from '../../models/criterio.model';
import { Page } from '../../models/professor.model'; // Reusing Page interface

@Injectable({
  providedIn: 'root'
})
export class CriterioService {
  private apiUrl = environment.apiUrl + '/criterios';

  constructor(private http: HttpClient) { }

  getAllCriterios(page: number, size: number): Observable<Page<Criterio>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<Criterio>>(this.apiUrl, { params });
  }

  getCriterioById(id: number): Observable<Criterio> {
    return this.http.get<Criterio>(`${this.apiUrl}/${id}`);
  }

  createCriterio(criterioData: Partial<Criterio>): Observable<Criterio> { // Partial for create
    return this.http.post<Criterio>(this.apiUrl, criterioData);
  }

  updateCriterio(id: number, criterioData: Partial<Criterio>): Observable<Criterio> {
    return this.http.put<Criterio>(`${this.apiUrl}/${id}`, criterioData);
  }

  deleteCriterio(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // If you need a non-paginated list for dropdowns, for example:
  getAllCriteriosList(): Observable<Criterio[]> {
    return this.http.get<Criterio[]>(`${this.apiUrl}/all`); // Assuming backend endpoint /api/criterios/all
  }
} 