import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },

  {
    path: 'home',
    loadComponent: () =>
      import('./home/home').then(m => m.Home)
  },

  {
    path: 'login',
    loadComponent: () =>
      import('./auth/pages/login/login').then(m => m.Login)
  },

  {
    path: 'register',
    loadComponent: () =>
      import('./auth/pages/register/register').then(m => m.Register)
  },

  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./auth/pages/profile/profile').then(m => m.Profile)
  },

  {
    path: 'dice',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./dice/pages/dice/dice').then(m => m.Dice)
  },

  {
    path: 'session',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./session/pages/active-session/active-session').then(m => m.ActiveSession)
  },

  {
    path: 'matches',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./session/pages/match-history/match-history').then(m => m.MatchHistory)
  },

  {
    path: 'matches/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./session/pages/match-detail/match-detail').then(m => m.MatchDetail)
  },

  {
    path: 'matches/:id/edit',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./session/pages/match-edit/match-edit').then(m => m.MatchEdit)
  },

  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    loadComponent: () =>
      import('./auth/pages/admin/admin').then(m => m.Admin)
  }
];
