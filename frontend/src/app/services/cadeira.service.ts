import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cadeira } from '../models/cadeira.model';

const API_URL = '/api/cadeiras';

@Injectable({
  providedIn: 'root'
})
export class CadeiraService {

  constructor(private http: HttpClient) { }

  getAllCadeiras(page: number = 0, size: number = 10, sort: string = 'nome'): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.http.get<any>(API_URL, { params });
  }

  getAllCadeirasList(): Observable<Cadeira[]> {
    return this.http.get<Cadeira[]>(`${API_URL}/all`);
  }

  getCadeiraById(id: number): Observable<Cadeira> {
    return this.http.get<Cadeira>(`${API_URL}/${id}`);
  }

  createCadeira(cadeiraData: any): Observable<Cadeira> {
    return this.http.post<Cadeira>(API_URL, cadeiraData);
  }

  updateCadeira(id: number, cadeiraData: any): Observable<Cadeira> {
    return this.http.put<Cadeira>(`${API_URL}/${id}`, cadeiraData);
  }

  deleteCadeira(id: number): Observable<void> {
    return this.http.delete<void>(`${API_URL}/${id}`);
  }
}
