import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AccountService } from '../../auth/services/account.service';

export const adminGuard: CanActivateFn = () => {
  const accountService = inject(AccountService);
  const router = inject(Router);

  if (accountService.isLoggedIn() && accountService.isAdmin()) {
    return true;
  }

  return router.createUrlTree(['/profile']);
};
