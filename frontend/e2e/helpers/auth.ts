import { APIRequestContext, Page } from '@playwright/test';

const API_URL = process.env.API_URL ?? 'http://localhost:8080';

export type E2EUser = {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
};

export function uniqueUser(prefix = 'e2e'): E2EUser {
  const id = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  const password = 'Test123!';

  return {
    username: `${prefix}-${id}`,
    email: `${prefix}-${id}@example.com`,
    password,
    confirmPassword: password
  };
}

export async function registerAndLoginByApi(
  page: Page,
  request: APIRequestContext,
  user: E2EUser = uniqueUser()
) {
  const response = await request.post(`${API_URL}/auth/register`, {
    data: user
  });

  if (!response.ok()) {
    throw new Error(`Could not create E2E user: ${response.status()} ${await response.text()}`);
  }

  const body = await response.json() as {
    token: string;
    username: string;
    role: string;
  };

  await page.addInitScript(({ token, username, role }) => {
    localStorage.setItem('token', token);
    localStorage.setItem('username', username);
    localStorage.setItem('role', role);
  }, body);

  return user;
}
