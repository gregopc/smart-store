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
import { ChatService } from '../../core/services/chat.service';
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

  products: WritableSignal<Product[]> = signal([]);

  private searchEffect = effect(() => {
    const query = this.searchState.getQuery()();

    if (query === null) return;

    this.searchQuery.set(query || null);
    this.resetAndLoad();
  });

  private resetAndLoad() {
    this.page = 0;
    this.products.set([]);
    this.hasMore = true;
    this.loading = false; // garante que não fica travado

    // reconecta o observer para garantir que o sentinel redispare
    this.observer?.disconnect();

    this.loadProducts().then(() => {
      this.setupObserver(); // reobserva após carregar primeira página
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

    try {
      let response;

      const query = this.searchQuery();

      if (query) {
        response = await this.productService.search(query, this.page, this.size);
      } else {
        response = await this.productService.getProducts(this.page, this.size);
      }

      let content: Product[] = [];
      let isLast = false;

      content = response.content;
      isLast = response.last;

      this.products.update(current => [
        ...current,
        ...content
      ]);

      if (isLast || content.length === 0) {
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
