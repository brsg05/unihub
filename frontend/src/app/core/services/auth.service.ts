import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { User } from '../../models/user.model';
import { environment } from '../../../environments/environment';
import { Router } from '@angular/router';
import { LoginRequest, RegisterRequest, MessageResponse } from '../../models/dto/auth.dto';
import { ERole } from '../../models/erole.model';

const API_URL = environment.apiUrl + '/users/';

const HTTP_OPTIONS_JSON = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;
  private readonly TOKEN_KEY = 'unihub_auth_token';
  private readonly USER_KEY = 'unihub_auth_user';

  constructor(private http: HttpClient, private router: Router) {
    const storedUserString = localStorage.getItem(this.USER_KEY);
    if (storedUserString) {
      const storedUserWithToken = JSON.parse(storedUserString);
      const { accessToken, ...userModelFields } = storedUserWithToken;
      this.currentUserSubject = new BehaviorSubject<User | null>(userModelFields as User);
      if (accessToken) localStorage.setItem(this.TOKEN_KEY, accessToken);
    } else {
      this.currentUserSubject = new BehaviorSubject<User | null>(null);
    }
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  public getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /** Login que aceita token como string (text/plain) ou { token: string } */
  login(credentials: LoginRequest): Observable<User> {
    return this.http.post<any>(
      API_URL + 'login',
      credentials,
      {
        ...HTTP_OPTIONS_JSON,
        // Garante que aceitamos respostas text/plain sem erro de parsing no Angular
        responseType: 'text' as 'json'
      }
    ).pipe(
      map((response: any) => {
        const rawToken = this.extractToken(response);
        if (!rawToken) throw new Error('Token ausente na resposta do login.');

        const payload = this.decodeJwtPayload(rawToken) as {
          userId?: number;
          role?: string;
          email?: string;
          username?: string;
          sub?: string;
        };

        // Monta o User a partir das claims do back
        const roleString = (payload.role || 'ROLE_USER').toUpperCase();
        const isAdmin = roleString.includes('ADMIN');
        const role: ERole = isAdmin ? ERole.ROLE_ADMIN : ERole.ROLE_USER;

        const userForApp: User = {
          id: payload.userId ?? 0,
          username: payload.username ?? payload.email ?? payload.sub ?? '',
          email: payload.email ?? '',
          role
        };

        const userForStorage = { ...userForApp, accessToken: rawToken };
        localStorage.setItem(this.USER_KEY, JSON.stringify(userForStorage));
        localStorage.setItem(this.TOKEN_KEY, rawToken);
        this.currentUserSubject.next(userForApp);

        return userForApp;
      })
    );
  }

  register(registrationData: RegisterRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(API_URL + 'register', registrationData, HTTP_OPTIONS_JSON);
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  public isAdmin(): boolean {
    const user = this.currentUserValue;
    return !!user && user.role === ERole.ROLE_ADMIN;
  }

  /** Extrai token quando resposta é string, JSON string ou objeto { token } */
  private extractToken(response: any): string | null {
    if (response == null) return null;

    // Se vier como string, pode ser o token puro ou um JSON stringificado
    if (typeof response === 'string') {
      const trimmed = response.trim();
      // Tenta interpretar como JSON stringificado
      if ((trimmed.startsWith('{') && trimmed.endsWith('}')) || (trimmed.startsWith('"') && trimmed.endsWith('"'))) {
        try {
          const maybeObj = JSON.parse(trimmed);
          if (maybeObj && typeof maybeObj === 'object') {
            if (typeof maybeObj.token === 'string') return maybeObj.token.trim();
            if (typeof maybeObj.accessToken === 'string') return maybeObj.accessToken.trim();
          }
          // Se for uma string JSON simples (ex: "<token>") após parse vira string novamente
          if (typeof maybeObj === 'string') return maybeObj.trim();
        } catch {
          // Ignora erro e trata como token puro abaixo
        }
      }
      return trimmed;
    }

    // Caso a resposta já seja objeto
    if (typeof response.token === 'string') return response.token.trim();
    if (typeof response.accessToken === 'string') return response.accessToken.trim();
    return null;
  }

  /** Decodificação segura de Base64URL do payload do JWT */
  private decodeJwtPayload(token: string): unknown {
    const parts = token.split('.');
    if (parts.length < 2) throw new Error('JWT inválido.');
    const base64Url = parts[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(base64.length + (4 - (base64.length % 4)) % 4, '=');
    const json = decodeURIComponent(
      atob(padded)
        .split('')
        .map(c => `%${('00' + c.charCodeAt(0).toString(16)).slice(-2)}`)
        .join('')
    );
    return JSON.parse(json);
  }
}
