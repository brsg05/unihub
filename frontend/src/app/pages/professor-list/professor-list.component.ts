import { Component, OnInit, OnDestroy, ViewChild, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProfessorService } from '../../core/services/professor.service';
import { ProfessorDto, Page } from '../../models/professor.model';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { FormBuilder, FormGroup } from '@angular/forms';

interface FilterOption {
  value: string;
  viewValue: string;
}

@Component({
  selector: 'app-professor-list',
  templateUrl: './professor-list.component.html',
  styleUrls: ['./professor-list.component.scss']
})
export class ProfessorListComponent implements OnInit, AfterViewInit, OnDestroy {
  displayedColumns: string[] = ['photo', 'nomeCompleto', 'notaGeral', 'actions'];
  dataSource = new MatTableDataSource<ProfessorDto>();
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  totalElements: number = 0;
  pageSize: number = 10;
  currentPage: number = 0;
  isLoading: boolean = false;
  
  filterForm!: FormGroup;
  currentSearchTerm: string | null = null;
  currentFilter: string | null = null; // 'top', 'worst', 'recent', 'periodo'
  currentPeriodo: string | null = null;

  filterOptions: FilterOption[] = [
    { value: 'all', viewValue: 'All' },
    { value: 'top', viewValue: 'Top Rated' },
    { value: 'worst', viewValue: 'Worst Rated' },
    // { value: 'recent', viewValue: 'Recently Added' }, // Backend needs to support this
    { value: 'periodo', viewValue: 'By Academic Period' }
  ];

  private destroy$ = new Subject<void>();

  constructor(
    private professorService: ProfessorService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.filterForm = this.fb.group({
      searchTerm: [''],
      filterType: ['all'],
      academicPeriod: ['']
    });

    this.route.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        this.currentSearchTerm = params['search'] || null;
        this.filterForm.patchValue({ searchTerm: this.currentSearchTerm }, { emitEvent: false });
        this.currentPage = 0; // Reset to first page on new search
        if (this.paginator) {
            this.paginator.pageIndex = 0;
        }
        this.loadProfessors();
      });

    this.filterForm.get('searchTerm')?.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(term => {
        // Navigate to update URL and trigger queryParams subscription
        this.router.navigate([], {
          relativeTo: this.route,
          queryParams: { search: term || null },
          queryParamsHandling: 'merge'
        });
      });
    
    this.filterForm.get('filterType')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(filter => {
        this.currentFilter = filter === 'all' ? null : filter;
        this.currentPage = 0;
        if (this.paginator) {
            this.paginator.pageIndex = 0;
        }
        this.loadProfessors();
      });

    this.filterForm.get('academicPeriod')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(periodo => {
        this.currentPeriodo = periodo;
        if(this.currentFilter === 'periodo'){
            this.currentPage = 0;
            if (this.paginator) {
                this.paginator.pageIndex = 0;
            }
            this.loadProfessors();
        }
      });
  }

  ngAfterViewInit() {
    if (this.paginator) {
        this.dataSource.paginator = this.paginator; // For client-side pagination if ever needed, but we use server-side
        this.paginator.page
        .pipe(takeUntil(this.destroy$))
        .subscribe((event: PageEvent) => {
            this.currentPage = event.pageIndex;
            this.pageSize = event.pageSize;
            this.loadProfessors();
        });
    }
  }

  loadProfessors(): void {
    this.isLoading = true;
    const searchTermToUse = this.filterForm.get('searchTerm')?.value;
    const filterTypeToUse = this.filterForm.get('filterType')?.value === 'all' ? null : this.filterForm.get('filterType')?.value;
    const periodoToUse = this.filterForm.get('academicPeriod')?.value || null;

    if (searchTermToUse) {
      this.professorService.searchProfessores(searchTermToUse, this.currentPage, this.pageSize)
        .pipe(takeUntil(this.destroy$))
        .subscribe(this.handleProfessorResponse.bind(this), this.handleError.bind(this));
    } else {
      this.professorService.getAllProfessores(this.currentPage, this.pageSize, filterTypeToUse, periodoToUse)
        .pipe(takeUntil(this.destroy$))
        .subscribe(this.handleProfessorResponse.bind(this), this.handleError.bind(this));
    }
  }

  private handleProfessorResponse(page: Page<ProfessorDto>) {
    this.dataSource.data = page.content;
    this.totalElements = page.totalElements;
    this.isLoading = false;
  }

  private handleError(error: any) {
    this.isLoading = false;
    this.snackBar.open('Failed to load professors. Please try again.', 'Close', { duration: 3000 });
    console.error(error);
  }

  viewProfessor(professorId: number): void {
    this.router.navigate(['/professor', professorId]);
  }

  clearSearch(): void {
    this.filterForm.patchValue({ searchTerm: ''}); // This will trigger valueChanges and reload
  }
  
  isPeriodoFilterActive(): boolean {
    return this.filterForm.get('filterType')?.value === 'periodo';
  }

  getAverageRating(): number {
    if (this.dataSource.data.length === 0) return 0;
    
    const ratedProfessors = this.dataSource.data.filter(prof => prof.notaGeral !== null && prof.notaGeral !== undefined);
    if (ratedProfessors.length === 0) return 0;
    
    const sum = ratedProfessors.reduce((acc, prof) => acc + (prof.notaGeral || 0), 0);
    return sum / ratedProfessors.length;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
} 