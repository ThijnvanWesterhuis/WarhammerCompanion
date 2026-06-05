import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AccountService } from '../auth/services/account.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home {
  constructor(public accountService: AccountService) {}
}
