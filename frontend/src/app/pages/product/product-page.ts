import { Component, WritableSignal, signal } from '@angular/core';
import { ProductService } from '../../core/services/product.service';
import { Product } from '../../core/models/product';

@Component({
  selector: 'app-product-page',
  imports: [],
  templateUrl: './product-page.html',
  styleUrl: './product-page.css',
})
export class ProductPageComponent {
  product: WritableSignal<Product | null> = signal(null);

  constructor(private readonly productService: ProductService) {}

  async ngOnInit() {
    const products = (await this.productService.getProducts()).content;
    //this.products.set(products);
  }
}
