import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, UpdateUserRoleRequest } from '../../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = environment.apiUrl + '/users';

  constructor(private http: HttpClient) { }

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  updateUserRole(id: number, roleRequest: UpdateUserRoleRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}/role`, roleRequest);
  }
  
  // deleteUser(id: number): Observable<void> { // If a delete user endpoint exists in the future
  //   return this.http.delete<void>(`${this.apiUrl}/${id}`);
  // }
} 