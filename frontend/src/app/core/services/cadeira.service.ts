import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cadeira } from '../../models/cadeira.model';
import { Page } from '../../models/professor.model'; // Reusing Page interface

@Injectable({
  providedIn: 'root'
})
export class CadeiraService {
  private apiUrl = environment.apiUrl + '/cadeiras';

  constructor(private http: HttpClient) { }

  getAllCadeiras(page: number, size: number): Observable<Page<Cadeira>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<Cadeira>>(this.apiUrl, { params });
  }

  getCadeiraById(id: number): Observable<Cadeira> {
    return this.http.get<Cadeira>(`${this.apiUrl}/${id}`);
  }

  createCadeira(cadeiraData: Partial<Cadeira>): Observable<Cadeira> { // Partial for create as ID is not needed
    return this.http.post<Cadeira>(this.apiUrl, cadeiraData);
  }

  updateCadeira(id: number, cadeiraData: Cadeira): Observable<Cadeira> {
    return this.http.put<Cadeira>(`${this.apiUrl}/${id}`, cadeiraData);
  }

  deleteCadeira(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // If you need a non-paginated list for dropdowns, for example:
  getAllCadeirasList(): Observable<Cadeira[]> {
    return this.http.get<Cadeira[]>(`${this.apiUrl}/all`); // Assuming backend endpoint /api/cadeiras/all
  }
} 