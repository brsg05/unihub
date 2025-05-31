import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { first } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service'; // Adjusted path
import { MatSnackBar } from '@angular/material/snack-bar';
import { RegisterRequest } from '../../../models/dto/auth.dto';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  loading = false;
  submitted = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {
     // Redirect to home if already logged in
     if (this.authService.currentUserValue) {
      this.router.navigate(['/']);
    }
  }

  ngOnInit(): void {
    this.registerForm = this.formBuilder.group({
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  get f() { return this.registerForm.controls; }

  onSubmit() {
    this.submitted = true;

    if (this.registerForm.invalid) {
      return;
    }

    this.loading = true;
    const registerRequest: RegisterRequest = {
      username: this.f['username'].value,
      email: this.f['email'].value,
      password: this.f['password'].value
    };

    this.authService.register(registerRequest)
      .pipe(first())
      .subscribe({
        next: (response) => {
          this.snackBar.open(response.message || 'Registration successful! Please login.', 'Close', {
            duration: 5000
          });
          this.router.navigate(['/login']);
        },
        error: error => {
          this.snackBar.open(`Registration failed: ${error.error?.message || error.message || 'An error occurred'}`, 'Close', {
            duration: 5000,
            panelClass: ['error-snackbar']
          });
          this.loading = false;
        }
      });
  }
} 