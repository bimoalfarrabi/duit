import { defineMiddleware } from 'astro:middleware';

const PUBLIC_ROUTES = ['/login'];

export const onRequest = defineMiddleware(async ({ url, cookies, locals, redirect }, next) => {
  const token = cookies.get('auth_token')?.value;
  if (!PUBLIC_ROUTES.includes(url.pathname) && !token) return redirect('/login');
  locals.token = token ?? '';
  return next();
});
