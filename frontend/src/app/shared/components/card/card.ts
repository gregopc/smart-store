import { Component, inject, Input, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Router } from '@angular/router';

import { Product } from '../../../core/models/product';
import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-card',
  imports: [CurrencyPipe],
  templateUrl: './card.html',
  styleUrl: './card.css',
})
export class Card {

  @Input({ required: true })
  product!: Product;

  private readonly router = inject(Router);
  private readonly cartService = inject(CartService);

  readonly added = signal(false);

  openProduct(): void {
    this.router.navigate(['/product', this.product.id]);
  }

  addToCart(event: MouseEvent): void {
    event.stopPropagation();

    this.cartService.add(this.product);

    this.added.set(true);

    setTimeout(() => this.added.set(false), 2000);
  }

}