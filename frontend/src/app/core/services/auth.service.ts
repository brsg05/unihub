import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { User } from '../../models/user.model';
import { environment } from '../../../environments/environment';
import { Router } from '@angular/router';
import { LoginRequest, JwtResponse, RegisterRequest, MessageResponse } from '../../models/dto/auth.dto';
import { ERole } from '../../models/erole.model';

const API_URL = environment.apiUrl + '/auth/';

const HTTP_OPTIONS = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;
  private readonly TOKEN_KEY = 'unihub_auth_token';
  private readonly USER_KEY = 'unihub_auth_user';

  constructor(private http: HttpClient, private router: Router) {
    const storedUserString = localStorage.getItem(this.USER_KEY);
    if (storedUserString) {
      const storedUserWithToken = JSON.parse(storedUserString);
      // Separate the User model part from the accessToken for currentUserSubject
      const { accessToken, ...userModelFields } = storedUserWithToken;
      this.currentUserSubject = new BehaviorSubject<User | null>(userModelFields as User);
      // Ensure TOKEN_KEY is also populated if user session exists
      if (accessToken) {
        localStorage.setItem(this.TOKEN_KEY, accessToken);
      }
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

  login(credentials: LoginRequest): Observable<User> {
    return this.http.post<JwtResponse>(API_URL + 'login', credentials, HTTP_OPTIONS)
      .pipe(
        map((response: JwtResponse) => {
          const userRoleString = response.roles && response.roles.length > 0 ? response.roles[0] : undefined;
          let userRole: ERole = ERole.ROLE_USER; // Default to ROLE_USER
          if (userRoleString === 'ROLE_ADMIN') {
            userRole = ERole.ROLE_ADMIN;
          } else if (userRoleString === 'ROLE_USER') {
            userRole = ERole.ROLE_USER;
          }
          // else: it defaults to ROLE_USER if role string is unexpected or missing

          const userForApp: User = {
            id: response.id,
            username: response.username,
            email: response.email,
            role: userRole,
          };

          // Store user details along with token in localStorage under USER_KEY
          const userForStorage = {
            ...userForApp,
            accessToken: response.token 
          };
          localStorage.setItem(this.USER_KEY, JSON.stringify(userForStorage));
          localStorage.setItem(this.TOKEN_KEY, response.token); // Store raw token under TOKEN_KEY

          this.currentUserSubject.next(userForApp);
          return userForApp;
        })
      );
  }

  register(registrationData: RegisterRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(API_URL + 'register', registrationData, HTTP_OPTIONS);
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
} 