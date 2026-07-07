import { Component, computed, inject, resource } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { CurrencyPipe } from '@angular/common';

import { ProductService } from '../../core/services/product.service';

@Component({
  selector: 'app-product-page',
  imports: [CurrencyPipe],
  templateUrl: './product-page.html',
  styleUrl: './product-page.css',
})
export class ProductPageComponent {
  private readonly productService: ProductService = inject(ProductService);
  private readonly route: ActivatedRoute = inject(ActivatedRoute);

  private readonly paramMap = toSignal(this.route.paramMap);

  private readonly productId = computed(() => this.paramMap()?.get('id'));

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

  readonly product = computed(() => this.productResource.hasValue()
    ? this.productResource.value()
    : undefined
  );

  private goBack() {
    history.back();
  }

  formatDescription(text: string): string {
    if (!text) {
      return '';
    }

    let result = text.charAt(0).toUpperCase() + text.slice(1);

    if (!/[.!?]$/.test(result.trim())) {
      result += '.';
    }

    return result;
  }
}
