import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator'; // No PageEvent if not paginated server-side
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';

import { UserService } from '../../../core/services/user.service';
import { User } from '../../../models/user.model';
import { ERole } from '../../../models/erole.model';
// import { AdminUserRoleDialogComponent, AdminUserRoleDialogData } from './admin-user-role-dialog/admin-user-role-dialog.component'; // Dialog for role update

import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSelectModule } from '@angular/material/select'; // For role selection

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatSelectModule,
    // AdminUserRoleDialogComponent, // If using a dialog for role change
  ],
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.scss']
})
export class AdminUsersComponent implements OnInit, OnDestroy {
  displayedColumns: string[] = ['id', 'username', 'email', 'role', 'actions'];
  dataSource: MatTableDataSource<User> = new MatTableDataSource();
  isLoading = true;
  roles = Object.values(ERole); // For role selection dropdown

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private usersSub: Subscription | undefined;

  constructor(
    private userService: UserService,
    public dialog: MatDialog, // For dialog-based role update if preferred
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  ngOnDestroy(): void {
    this.usersSub?.unsubscribe();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.usersSub = this.userService.getAllUsers().subscribe({
      next: (data: User[]) => {
        this.dataSource.data = data;
        this.isLoading = false;
        // Setup paginator and sort after data is loaded for client-side operations
        this.dataSource.paginator = this.paginator; 
        this.dataSource.sort = this.sort;
      },
      error: (err) => {
        this.snackBar.open('Erro ao carregar usuários.', 'Fechar', { duration: 3000 });
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  // Inline role update for simplicity, or implement openEditRoleDialog
  updateUserRole(user: User, newRole: ERole): void {
    if (user.role === newRole) {
        this.snackBar.open('O usuário já possui este papel.', 'Fechar', { duration: 3000 });
        return;
    }

    this.userService.updateUserRole(user.id, { role: newRole }).subscribe({
      next: (updatedUser) => {
        this.snackBar.open(`Papel do usuário ${updatedUser.username} atualizado para ${updatedUser.role}.`, 'Fechar', { duration: 3000 });
        this.loadUsers(); // Refresh list
      },
      error: (err) => {
        this.snackBar.open('Erro ao atualizar papel do usuário.', 'Fechar', { duration: 5000 });
        console.error(err);
        // Optionally, revert the role in the dropdown if the update fails, though loadUsers() will refresh anyway
        this.loadUsers(); // Or find a way to revert the specific user's role in the UI
      }
    });
  }

  // Optional: Implement dialog-based role update if more complex logic or confirmation is needed
  /*
  openEditRoleDialog(user: User): void {
    const dialogRef = this.dialog.open(AdminUserRoleDialogComponent, {
      width: '400px',
      data: { userId: user.id, currentRole: user.role } // Pass necessary data to dialog
    });

    dialogRef.afterClosed().subscribe(newRole => {
      if (newRole) {
        this.updateUserRole(user, newRole);
      }
    });
  }
  */
} 