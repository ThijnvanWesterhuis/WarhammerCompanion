import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AccountService } from '../../services/account.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  username = '';
  password = '';
  errorMessage = signal('');

  constructor(
    private accountService: AccountService,
    private router: Router
  ) {}

  onSubmit() {
    this.errorMessage.set('');

    this.accountService.login({
      username: this.username,
      password: this.password
    }).subscribe({
      next: () => {
        this.router.navigate(['/profile']);
      },
      error: () => {
        this.errorMessage.set('Invalid username or password');
      }
    });
  }
}
