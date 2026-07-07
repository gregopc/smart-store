import { Component, Input, inject } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

import { CartItem } from '../../../core/models/cart-item';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-cart-item',
  imports: [
    CurrencyPipe,
    MatIconModule,
  ],
  templateUrl: './cart-item.component.html',
  styleUrl: './cart-item.component.css',
})
export class CartItemComponent {

  @Input({ required: true })
  item!: CartItem;

  private readonly cartService = inject(CartService);

  increase(): void {
    this.cartService.increase(this.item.product.id);
  }

  decrease(): void {
    this.cartService.decrease(this.item.product.id);
  }

  remove(): void {
    this.cartService.remove(this.item.product.id);
  }

  get subtotal(): number {
    return this.item.product.price * this.item.quantity;
  }

}