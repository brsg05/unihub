import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ComentarioPublicDto } from '../../models/comentario.model'; // Assuming vote returns the updated comment

@Injectable({
  providedIn: 'root'
})
export class ComentarioService {
  private apiUrl = environment.apiUrl + '/comentarios';

  constructor(private http: HttpClient) { }

  upvoteComentario(comentarioId: number): Observable<ComentarioPublicDto> {
    return this.http.post<ComentarioPublicDto>(`${this.apiUrl}/${comentarioId}/vote/up`, {});
  }

  downvoteComentario(comentarioId: number): Observable<ComentarioPublicDto> {
    return this.http.post<ComentarioPublicDto>(`${this.apiUrl}/${comentarioId}/vote/down`, {});
  }
} 