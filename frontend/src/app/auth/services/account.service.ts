import { Injectable, signal } from '@angular/core';
import { tap } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  Role,
  UserResponse
} from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly tokenKey = 'token';
  private readonly usernameKey = 'username';
  private readonly roleKey = 'role';

  private loggedInSignal = signal<boolean>(!!localStorage.getItem(this.tokenKey));
  private usernameSignal = signal<string | null>(localStorage.getItem(this.usernameKey));
  private roleSignal = signal<Role | null>(localStorage.getItem(this.roleKey) as Role | null);

  constructor(private api: ApiService) {}

  register(request: RegisterRequest) {
    return this.api.post<AuthResponse>('/auth/register', request).pipe(
      tap(response => this.saveLogin(response))
    );
  }

  login(request: LoginRequest) {
    return this.api.post<AuthResponse>('/auth/login', request).pipe(
      tap(response => this.saveLogin(response))
    );
  }

  getProfile() {
    return this.api.get<UserResponse>('/users/me');
  }

  logout() {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.usernameKey);
    localStorage.removeItem(this.roleKey);

    this.loggedInSignal.set(false);
    this.usernameSignal.set(null);
    this.roleSignal.set(null);
  }

  isLoggedIn() {
    return this.loggedInSignal();
  }

  isAdmin() {
    return this.roleSignal() === 'ADMIN';
  }

  getUsername() {
    return this.usernameSignal();
  }

  private saveLogin(response: AuthResponse) {
    localStorage.setItem(this.tokenKey, response.token);
    localStorage.setItem(this.usernameKey, response.username);
    localStorage.setItem(this.roleKey, response.role);

    this.loggedInSignal.set(true);
    this.usernameSignal.set(response.username);
    this.roleSignal.set(response.role);
  }
}
