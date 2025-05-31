import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service'; // Adjust path as needed
import { User } from '../../models/user.model'; // Import User model

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.authService.currentUser.pipe(
      take(1),
      map((user: User | null) => { // Typed user parameter
        if (user) {
          // User is logged in
          return true;
        }
        // User is not logged in, redirect to login page
        return this.router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
      })
    );
  }
} 