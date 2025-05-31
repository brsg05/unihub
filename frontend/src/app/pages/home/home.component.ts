import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ProfessorService } from '../../core/services/professor.service';
import { ProfessorDto } from '../../models/professor.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  topProfessores: ProfessorDto[] = [];
  isLoadingTopProfessores = false;
  searchForm!: FormGroup;

  constructor(
    private professorService: ProfessorService,
    private router: Router,
    private snackBar: MatSnackBar,
    private fb: FormBuilder
  ) { }

  ngOnInit(): void {
    this.searchForm = this.fb.group({
      searchTerm: ['']
    });
    this.fetchTopProfessores();
  }

  fetchTopProfessores(limit: number = 5): void {
    this.isLoadingTopProfessores = true;
    this.professorService.getTopProfessores(limit).subscribe({
      next: (data) => {
        this.topProfessores = data;
        this.isLoadingTopProfessores = false;
      },
      error: (err) => {
        this.snackBar.open('Failed to load top professors.', 'Close', { duration: 3000 });
        console.error(err);
        this.isLoadingTopProfessores = false;
      }
    });
  }

  onSearchSubmit(): void {
    const searchTerm = this.searchForm.value.searchTerm;
    if (searchTerm && searchTerm.trim() !== '') {
      this.router.navigate(['/professores'], { queryParams: { search: searchTerm.trim() } });
    } else {
      // Optionally, navigate to the general professor list or show a message
      this.router.navigate(['/professores']);
    }
  }
} 