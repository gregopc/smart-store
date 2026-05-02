import {
  Component,
  WritableSignal,
  signal,
  OnInit,
  OnDestroy,
  ElementRef,
  ViewChild,
} from '@angular/core';

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
export class HomePageComponent implements OnInit, OnDestroy {

  products: WritableSignal<Product[]> = signal([]);

  page = 0;
  size = 12;

  loading = false;
  hasMore = true;

  private observer?: IntersectionObserver;

  @ViewChild('sentinel', { static: true })
  sentinel!: ElementRef<HTMLDivElement>;

  constructor(private readonly productService: ProductService) {}

  async ngOnInit() {
    await this.loadProducts();
    this.setupObserver();
  }

  ngOnDestroy() {
    this.observer?.disconnect();
  }

  private setupObserver() {
    this.observer = new IntersectionObserver(
      (entries) => {
        const entry = entries[0];

        if (entry.isIntersecting) {
          this.loadProducts();
        }
      },
      {
        root: null,           // viewport
        rootMargin: '600px',  // começa antes de chegar no fim
        threshold: 0,
      }
    );

    this.observer.observe(this.sentinel.nativeElement);
  }

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
