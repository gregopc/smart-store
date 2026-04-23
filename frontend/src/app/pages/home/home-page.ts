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

  page = 0;
  size = 12;

  loading = false;
  hasMore = true;

  constructor(private readonly productService: ProductService) {}

  async ngOnInit() {
    await this.loadProducts();

    window.addEventListener('scroll', this.onScroll);
  }

  ngOnDestroy() {
    window.removeEventListener('scroll', this.onScroll);
  }

  onScroll = () => {
    const threshold = 200;

    const position = window.innerHeight + window.scrollY;
    const height = document.body.offsetHeight;

    if (position >= height - threshold) {
      this.loadProducts();
    }
  };

  async loadProducts() {
    if (this.loading || !this.hasMore) return;

    this.loading = true;

    try {
      const response = await this.productService.getProducts(this.page, this.size);

      this.products.update(current => [
        ...current,
        ...response.content
      ]);

      if (response.last || response.content.length === 0) {
        this.hasMore = false;
      }

      this.page++;
    } catch (error) {
      console.error('Erro ao carregar produtos:', error);
    } finally {
      this.loading = false;
    }
  }
}
