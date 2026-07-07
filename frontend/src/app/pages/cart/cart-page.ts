import { Component, inject } from '@angular/core';
import { CurrencyPipe } from '@angular/common';

import { CartService } from '../../core/services/cart.service';
import { CartItemComponent } from '../../shared/components/cart-item/cart-item.component';

@Component({
  selector: 'app-cart',
  imports: [
    CurrencyPipe,
    CartItemComponent,
  ],
  templateUrl: './cart-page.html',
  styleUrl: './cart-page.css',
})
export class CartPageComponent {

  readonly cartService = inject(CartService);

  clear(): void {
    this.cartService.clear();
  }

}