import { Component, WritableSignal, signal } from '@angular/core';
import { ProductService } from '../../core/services/product.service';
import { Product } from '../../core/models/product';
import { Card } from '../../shared/components/card/card';
import { ChatService } from '../../core/services/chat.service';


@Component({
  selector: 'app-home',
  imports: [
    Card
  ],
  templateUrl: './home-page.html',
  styleUrl: './home-page.css',
})
export class HomePageComponent {
  products: WritableSignal<Product[]> = signal([]);

  constructor(private readonly productService: ProductService) {}

  async ngOnInit() {
    const products = (await this.productService.getProducts()).content;
    this.products.set(products);
  }
}
