import {
  Component,
  computed,
  inject,
  resource,
  signal,
} from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';

import { ProductService } from '../../core/services/product.service';
import { CartService } from '../../core/services/cart.service';

@Component({
  selector: 'app-product-page',
  imports: [CurrencyPipe],
  templateUrl: './product-page.html',
  styleUrl: './product-page.css',
})
export class ProductPageComponent {

  private readonly productService = inject(ProductService);
  private readonly cartService = inject(CartService);
  private readonly route = inject(ActivatedRoute);

  readonly added = signal(false);

  private feedbackTimeout?: ReturnType<typeof setTimeout>;

  private readonly paramMap = toSignal(this.route.paramMap);

  private readonly productId = computed(() =>
    this.paramMap()?.get('id')
  );

  private readonly productResource = resource({
    params: () => this.productId(),

    loader: async ({ params: id }) => {

      if (!id) {
        this.goBack();
        return null;
      }

      return await this.productService.getProduct(id);

    },
  });

  readonly product = computed(() =>
    this.productResource.hasValue()
      ? this.productResource.value()
      : undefined
  );

  addToCart(): void {

    const product = this.product();

    if (!product) {
      return;
    }

    this.cartService.add(product);

    this.showAddedFeedback();

  }

  private showAddedFeedback(): void {

    this.added.set(true);

    clearTimeout(this.feedbackTimeout);

    this.feedbackTimeout = setTimeout(() => {
      this.added.set(false);
    }, 2000);

  }

  private goBack(): void {
    history.back();
  }

  formatDescription(text: string): string {

    if (!text) {
      return '';
    }

    let result =
      text.charAt(0).toUpperCase() +
      text.slice(1);

    if (!/[.!?]$/.test(result.trim())) {
      result += '.';
    }

    return result;

  }

}