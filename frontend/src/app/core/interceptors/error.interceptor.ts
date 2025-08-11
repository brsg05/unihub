import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(
    private snackBar: MatSnackBar,
    private authService: AuthService,
    private router: Router
    ) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'An unknown error occurred!';
        if (error.error instanceof ErrorEvent) {
          // Client-side errors
          errorMessage = `Error: ${error.error.message}`;
        } else {
          // Server-side errors
          errorMessage = `Error: ${error.status} - ${error.error?.message || error.message}`;
          if (error.status === 401 && !request.url.includes('/users/login')) {
            // Auto logout if 401 response returned from api and not from login attempt
            this.snackBar.open('Your session has expired or you are unauthorized. Please login again.', 'Close', { duration: 7000 });
            this.authService.logout(); // This will navigate to login
          }
        }
        // Avoid showing snackbar for login failures if handled in component
        if (!request.url.includes('/users/login') || error.status !== 401) {
             this.snackBar.open(errorMessage, 'Close', {
                duration: 5000,
                panelClass: ['error-snackbar'] 
             });
        }
        return throwError(() => error);
      })
    );
  }
}