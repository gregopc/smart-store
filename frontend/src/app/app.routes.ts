import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      {
        path: 'home',
        loadComponent: () => import('./pages/home/home-page').then(m => m.HomePageComponent),
      },
      {
        path: 'home/:id',
        loadComponent: () => import('./pages/product/product-page').then(m => m.ProductPageComponent)
      },
      {
        path: 'cart',
        loadComponent: () => import('./pages/cart/cart-page').then(m => m.CartPageComponent)
      },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },

  // TODO: create CheckoutPageComponent
  // {
  //   path: '/checkout',
  //   loadComponent: () => import('./pages/checkout/checkout-page').then(m => m.CheckoutPageComponent)
  // },
  // TODO: create LoginPageComponent
  // {
  //   path: '/login',
  //   loadComponent: () => import('./pages/login/login-page').then(m => m.LoginPageComponent)
  // },
  // TODO: create RegisterPageComponent
  // {
  //   path: '/register',
  //   loadComponent: () => import('./pages/register/register-page').then(m => m.RegisterPageComponent)
  // },
  // TODO: create PageNotFoundComponent
  // {
  //   path: '**',
  //   loadComponent: () => import('./pages/page-not-found/page-not-found').then(m => m.PageNotFoundComponent)
  // }
];
