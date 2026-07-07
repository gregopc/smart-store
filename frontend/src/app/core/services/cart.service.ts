import { Injectable, computed, signal } from '@angular/core';

import { Product } from '../models/product';
import { CartItem } from '../models/cart-item';

@Injectable({
  providedIn: 'root',
})
export class CartService {

  private readonly cartItems = signal<CartItem[]>([]);

  readonly items = this.cartItems.asReadonly();

  readonly isEmpty = computed(() =>
    this.cartItems().length === 0
  );

  readonly totalItems = computed(() =>
    this.cartItems().reduce(
      (total, item) => total + item.quantity,
      0
    )
  );

  readonly subtotal = computed(() =>
    this.cartItems().reduce(
      (total, item) => total + item.product.price * item.quantity,
      0
    )
  );

  add(product: Product): void {

    this.cartItems.update(items => {

      const index = items.findIndex(
        item => item.product.id === product.id
      );

      if (index >= 0) {

        return items.map((item, i) =>
          i === index
            ? {
                ...item,
                quantity: item.quantity + 1,
              }
            : item
        );

      }

      return [
        ...items,
        {
          product,
          quantity: 1,
        },
      ];
    });

  }

  increase(productId: string): void {

    this.cartItems.update(items =>
      items.map(item =>
        item.product.id === productId
          ? {
              ...item,
              quantity: item.quantity + 1,
            }
          : item
      )
    );

  }

  decrease(productId: string): void {

    this.cartItems.update(items =>
      items
        .map(item =>
          item.product.id === productId
            ? {
                ...item,
                quantity: item.quantity - 1,
              }
            : item
        )
        .filter(item => item.quantity > 0)
    );

  }

  remove(productId: string): void {

    this.cartItems.update(items =>
      items.filter(item => item.product.id !== productId)
    );

  }

  clear(): void {
    this.cartItems.set([]);
  }

}