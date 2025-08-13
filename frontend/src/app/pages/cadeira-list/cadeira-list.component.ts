import { Component, OnInit, OnDestroy, ViewChild, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatTableDataSource } from '@angular/material/table';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { FormBuilder, FormGroup } from '@angular/forms';
import { Cadeira } from '../../models/cadeira.model';

interface FilterOption {
  value: string;
  viewValue: string;
}

@Component({
  selector: 'app-cadeira-list',
  templateUrl: './cadeira-list.component.html',
  styleUrls: ['./cadeira-list.component.scss']
})
export class CadeiraListComponent implements OnInit, AfterViewInit, OnDestroy {
  displayedColumns: string[] = ['nome', 'curso', 'professores', 'notaGeral', 'actions'];
  dataSource = new MatTableDataSource<Cadeira>();
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  totalElements: number = 0;
  pageSize: number = 10;
  currentPage: number = 0;
  isLoading: boolean = false;
  
  filterForm!: FormGroup;
  currentSearchTerm: string | null = null;
  currentFilter: string | null = null;

  filterOptions: FilterOption[] = [
    { value: 'all', viewValue: 'Todas' },
    { value: 'top', viewValue: 'Melhor Avaliadas' },
    { value: 'worst', viewValue: 'Pior Avaliadas' },
    { value: 'curso', viewValue: 'Por Curso' }
  ];

  // Mock data for demonstration
  mockCadeiras: Cadeira[] = [
    {
      id: 1,
      nome: 'Algoritmos e Estruturas de Dados',
      codigo: 'AED001',
      cargaHoraria: 60,
      isEletiva: false,
      cursoId: 1,
      cursoNome: 'Ciência da Computação',
      descricao: 'Fundamentos de algoritmos e estruturas de dados',
      professorCount: 3,
      notaGeral: 4.2
    },
    {
      id: 2,
      nome: 'Cálculo I',
      codigo: 'MAT101',
      cargaHoraria: 80,
      isEletiva: false,
      cursoId: 1,
      cursoNome: 'Engenharia',
      descricao: 'Introdução ao cálculo diferencial e integral',
      professorCount: 5,
      notaGeral: 3.8
    },
    {
      id: 3,
      nome: 'Programação Orientada a Objetos',
      codigo: 'POO001',
      cargaHoraria: 60,
      isEletiva: false,
      cursoId: 1,
      cursoNome: 'Ciência da Computação',
      descricao: 'Conceitos e práticas de POO',
      professorCount: 2,
      notaGeral: 4.5
    },
    {
      id: 4,
      nome: 'Física I',
      codigo: 'FIS101',
      cargaHoraria: 80,
      isEletiva: false,
      cursoId: 2,
      cursoNome: 'Engenharia',
      descricao: 'Mecânica clássica e termodinâmica',
      professorCount: 4,
      notaGeral: 3.5
    },
    {
      id: 5,
      nome: 'Banco de Dados',
      codigo: 'BD001',
      cargaHoraria: 60,
      isEletiva: true,
      cursoId: 1,
      cursoNome: 'Ciência da Computação',
      descricao: 'Modelagem e implementação de bancos de dados',
      professorCount: 2,
      notaGeral: 4.1
    }
  ];

  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar,
    private fb: FormBuilder
  ) {
    this.initializeForm();
  }

  ngOnInit(): void {
    this.loadData();
    this.setupSearchFilter();
  }

  ngAfterViewInit(): void {
    if (this.paginator) {
      this.dataSource.paginator = this.paginator;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private initializeForm(): void {
    this.filterForm = this.fb.group({
      searchTerm: [''],
      filter: ['all']
    });
  }

  private setupSearchFilter(): void {
    this.filterForm.get('searchTerm')?.valueChanges
      .pipe(
        takeUntil(this.destroy$),
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(searchTerm => {
        this.currentSearchTerm = searchTerm;
        this.applyFilters();
      });

    this.filterForm.get('filter')?.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(filter => {
        this.currentFilter = filter;
        this.applyFilters();
      });
  }

  private loadData(): void {
    this.isLoading = true;
    
    // Simulate API call with mock data
    setTimeout(() => {
      this.dataSource.data = this.mockCadeiras;
      this.totalElements = this.mockCadeiras.length;
      this.isLoading = false;
      this.applyFilters();
    }, 500);
  }

  private applyFilters(): void {
    let filteredData = [...this.mockCadeiras];

    // Apply search term filter
    if (this.currentSearchTerm) {
      const searchLower = this.currentSearchTerm.toLowerCase();
      filteredData = filteredData.filter(cadeira =>
        cadeira.nome.toLowerCase().includes(searchLower) ||
        (cadeira.codigo && cadeira.codigo.toLowerCase().includes(searchLower)) ||
        (cadeira.cursoNome && cadeira.cursoNome.toLowerCase().includes(searchLower))
      );
    }

    // Apply rating filter
    if (this.currentFilter && this.currentFilter !== 'all') {
      switch (this.currentFilter) {
        case 'top':
          filteredData = filteredData.filter(cadeira => (cadeira.notaGeral ?? 0) >= 4.0);
          break;
        case 'worst':
          filteredData = filteredData.filter(cadeira => (cadeira.notaGeral ?? 0) < 3.5);
          break;
        case 'curso':
          // Could implement course-specific filtering here
          break;
      }
    }

    this.dataSource.data = filteredData;
    this.totalElements = filteredData.length;
  }

  onPageChange(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.currentPage = event.pageIndex;
    this.loadData();
  }

  clearSearch(): void {
    this.filterForm.get('searchTerm')?.setValue('');
  }

  viewCadeira(cadeira: Cadeira): void {
    // Navigate to cadeira detail page (to be implemented)
    this.router.navigate(['/cadeira', cadeira.id]);
  }

  getStarsArray(rating: number): number[] {
    return Array(Math.floor(rating)).fill(0);
  }

  getEmptyStarsArray(rating: number): number[] {
    return Array(5 - Math.floor(rating)).fill(0);
  }

  trackByCadeiraId(index: number, cadeira: Cadeira): number {
    return cadeira.id;
  }

  getActiveCourses(): number {
    const uniqueCourses = new Set(this.dataSource.data.map(cadeira => cadeira.cursoId));
    return uniqueCourses.size;
  }
}
