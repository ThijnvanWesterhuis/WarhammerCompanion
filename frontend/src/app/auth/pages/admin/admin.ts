import { Component } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [],
  templateUrl: './admin.html',
  styleUrl: './admin.css'
})
export class Admin {
  message = '';

  constructor(private api: ApiService) {}

  testAdminEndpoint() {
    this.api.get<{ message: string }>('/admin/test').subscribe({
      next: response => {
        this.message = response.message;
      },
      error: () => {
        this.message = 'Admin request failed';
      }
    });
  }
}
