import { Component, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { UserResponse } from '../../models/auth.models';
import { AccountService } from '../../services/account.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './profile.html',
  styleUrl: './profile.css'
})
export class Profile implements OnInit {
  user = signal<UserResponse | null>(null);
  errorMessage = signal('');

  constructor(
    private accountService: AccountService,
    private router: Router
  ) {}

  ngOnInit() {
    this.accountService.getProfile().subscribe({
      next: user => {
        this.user.set(user);
      },
      error: () => {
        this.errorMessage.set('Could not load profile');
      }
    });
  }

  logout() {
    this.accountService.logout();
    this.router.navigate(['/home']);
  }
}
