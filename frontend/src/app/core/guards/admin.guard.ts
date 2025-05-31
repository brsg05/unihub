import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { User } from '../../models/user.model';
import { ERole } from '../../models/erole.model';

@Injectable({
  providedIn: 'root'
})
export class AdminGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    return this.authService.currentUser.pipe(
      take(1),
      map((user: User | null) => {
        if (user && user.role === ERole.ROLE_ADMIN) {
          return true;
        }
        // Redirect to login or unauthorized page
        return this.router.createUrlTree(['/login']); // Or a dedicated 'unauthorized' page
      })
    );
  }
} 