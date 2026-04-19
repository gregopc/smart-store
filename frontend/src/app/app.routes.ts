import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home-page').then(m => m.HomePageComponent)
  },
  {
    path: 'cart',
    loadComponent: () => import('./pages/cart/cart-page').then(m => m.CartPageComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./pages/product/product-page').then(m => m.ProductPageComponent)
  },
  // TODO: create PageNotFoundComponent
  // {
  //   path: '**',
  //   loadComponent: () => import('./pages/page-not-found/page-not-found').then(m => m.PageNotFoundComponent)
  // }
];
