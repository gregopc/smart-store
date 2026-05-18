import {
  Component,
  WritableSignal,
  signal,
  OnInit,
  OnDestroy,
  ElementRef,
  ViewChild,
  inject,
  effect,
} from '@angular/core';

import { ProductService } from '../../core/services/product.service';
import { Product } from '../../core/models/product';
import { Card } from '../../shared/components/card/card';
import { SearchStateService } from '../../core/services/search-state.service';


@Component({
  selector: 'app-home',
  imports: [
    Card
  ],
  templateUrl: './home-page.html',
  styleUrl: './home-page.css',
})
export class HomePageComponent implements OnInit, OnDestroy {

  private productService = inject(ProductService);
  private searchState = inject(SearchStateService);
  private requestId = 0;

  products: WritableSignal<Product[]> = signal([]);

  constructor() {
    effect(() => {
      const query = this.searchState.getQuery()();

      this.searchQuery.set(query || null);

      this.page = 0;
      this.products.set([]);
      this.hasMore = true;

      this.requestId++;
      this.loading = false;

      this.loadProducts();
    });
  }

  searchQuery = signal<string | null>(null);

  page = 0;
  size = 12;

  loading = false;
  hasMore = true;

  private observer?: IntersectionObserver;

  @ViewChild('sentinel', { static: true })
  sentinel!: ElementRef<HTMLDivElement>;

  async ngOnInit() {
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
        root: null,
        rootMargin: '600px',
        threshold: 0,
      }
    );

    this.observer.observe(this.sentinel.nativeElement);
  }

  async loadProducts() {
    if (this.loading || !this.hasMore) return;

    this.loading = true;

    const currentRequest = this.requestId;

    try {
      const query = this.searchQuery();

      const response = query
        ? await this.productService.search(
            query,
            this.page,
            this.size
          )
        : await this.productService.getProducts(
            this.page,
            this.size
          );

      if (currentRequest !== this.requestId) return;

      this.products.update(current => [
        ...current,
        ...response.content
      ]);

      this.hasMore =
        !response.last &&
        response.content.length > 0;

      this.page++;

    } catch (error) {
      console.error(
        'Erro ao carregar produtos:',
        error
      );
    } finally {
      if (currentRequest === this.requestId) {
        this.loading = false;
      }
    }
  }
}
