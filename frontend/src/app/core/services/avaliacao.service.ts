import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AvaliacaoRequest } from '../../models/avaliacao.model';
import { AvaliacaoPublic } from '../../models/avaliacao-public.model';
import { Page } from '../../models/professor.model';
import { MessageResponse } from '../../models/dto/auth.dto';
import { AvaliacaoPayload } from '../../models/avaliacao.model';

@Injectable({
  providedIn: 'root'
})
export class AvaliacaoService {
  private apiUrl = environment.apiUrl + '/avaliacoes';

  constructor(private http: HttpClient) { }

  submitAvaliacao(avaliacaoData: AvaliacaoRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(this.apiUrl, avaliacaoData);
  }

  getAvaliacoesByProfessorAndCadeira(professorId: number, cadeiraId: number, periodo: string | null, page: number, size: number): Observable<Page<AvaliacaoPublic>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (periodo) {
      params = params.set('periodo', periodo);
    }
    return this.http.get<Page<AvaliacaoPublic>>(`${this.apiUrl}/professor/${professorId}/cadeira/${cadeiraId}`, { params });
  }

  getCriterionEvaluationHistory(professorId: number, criterioId: number, periodo: string | null, page: number, size: number): Observable<Page<AvaliacaoPublic>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    if (periodo) {
      params = params.set('periodo', periodo);
    }
    return this.http.get<Page<AvaliacaoPublic>>(`${this.apiUrl}/criterio/${criterioId}/professor/${professorId}`, { params });
  }

  submeterAvaliacao(payload: AvaliacaoPayload): Observable<any> {
    return this.http.post(this.apiUrl, payload);
  }
} 