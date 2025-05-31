import { ERole } from './erole.model';

export interface User {
  id: number;
  username: string;
  email: string;
  role: ERole;
}

export interface UpdateUserRoleRequest {
  role: ERole;
} 