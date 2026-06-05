import { Component, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AccountService } from '../../services/account.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {
  username = '';
  email = '';
  password = '';
  confirmPassword = '';

  submitted = false;

  errorMessage = signal('');
  serverErrors = signal<Record<string, string>>({});

  constructor(
    private accountService: AccountService,
    private router: Router
  ) {}

  get trimmedUsername() {
    return this.username.trim();
  }

  get trimmedEmail() {
    return this.email.trim();
  }

  get usernameWarning() {
    if (!this.submitted && !this.username) {
      return '';
    }

    if (!this.trimmedUsername) {
      return 'Username is required';
    }

    if (this.trimmedUsername.length < 3) {
      return 'Username must be at least 3 characters';
    }

    if (this.trimmedUsername.length > 50) {
      return 'Username may not be longer than 50 characters';
    }

    return '';
  }

  get emailWarning() {
    if (!this.submitted && !this.email) {
      return '';
    }

    if (!this.trimmedEmail) {
      return 'Email is required';
    }

    if (!this.isValidEmail(this.trimmedEmail)) {
      return 'Enter a valid email address';
    }

    return '';
  }

  get passwordWarnings() {
    const warnings: string[] = [];

    if (!this.submitted && !this.password) {
      return warnings;
    }

    if (!this.password) {
      warnings.push('Password is required');
    }

    if (this.password.length < 8) {
      warnings.push('Password must be at least 8 characters');
    }

    if (!/[0-9]/.test(this.password)) {
      warnings.push('Password must contain at least one number');
    }

    if (!/[^A-Za-z0-9]/.test(this.password)) {
      warnings.push('Password must contain at least one special character');
    }

    return warnings;
  }

  get confirmPasswordWarning() {
    if (!this.submitted && !this.confirmPassword) {
      return '';
    }

    if (!this.confirmPassword) {
      return 'Confirm password is required';
    }

    if (this.password !== this.confirmPassword) {
      return 'Passwords do not match';
    }

    return '';
  }

  get isFormValid() {
    return !this.usernameWarning
      && !this.emailWarning
      && this.passwordWarnings.length === 0
      && !this.confirmPasswordWarning;
  }

  getFieldServerError(field: string) {
    return this.serverErrors()[field] ?? '';
  }

  onSubmit() {
    this.submitted = true;
    this.errorMessage.set('');
    this.serverErrors.set({});

    if (!this.isFormValid) {
      this.errorMessage.set('Check the warnings below before creating your account.');
      return;
    }

    this.accountService.register({
      username: this.trimmedUsername,
      email: this.trimmedEmail,
      password: this.password,
      confirmPassword: this.confirmPassword
    }).subscribe({
      next: () => {
        this.router.navigate(['/profile']);
      },
      error: (error: HttpErrorResponse) => {
        this.handleRegisterError(error);
      }
    });
  }

  private isValidEmail(email: string) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  private handleRegisterError(error: HttpErrorResponse) {
    const response = error.error as Record<string, string> | string | null;

    if (response && typeof response === 'object') {
      this.serverErrors.set(response);

      if (response['username']) {
        this.errorMessage.set(response['username']);
        return;
      }

      if (response['email']) {
        this.errorMessage.set(response['email']);
        return;
      }

      if (response['password']) {
        this.errorMessage.set(response['password']);
        return;
      }

      if (response['confirmPassword']) {
        this.errorMessage.set(response['confirmPassword']);
        return;
      }

      this.errorMessage.set(response['error'] ?? 'Registration failed');
      return;
    }

    if (typeof response === 'string') {
      this.errorMessage.set(response);
      return;
    }

    this.errorMessage.set('Registration failed');
  }
}
