import { Component, inject } from '@angular/core';
import { HealthService } from './health.service';

@Component({
  selector: 'app-root',
  imports: [],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  private healthService = inject(HealthService);

  title = 'frontend';
  backendStatus = 'Not tested yet';

  testBackend() {
    this.healthService.getHealth().subscribe({
      next: (response) => {
        this.backendStatus = response.status;
      },
      error: () => {
        this.backendStatus = 'Connection failed';
      }
    });
  }
}
