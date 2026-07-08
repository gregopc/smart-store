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
import { ProductFilter } from '../../shared/interfaces/product-filter';


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


  searchQuery = signal<string | null>(null);

  filters = signal<ProductFilter>({});


  page = 0;
  size = 12;

  loading = false;
  hasMore = true;


  private observer?: IntersectionObserver;


  @ViewChild('sentinel', { static: true })
  sentinel!: ElementRef<HTMLDivElement>;



  private searchEffect = effect(() => {

    const query = this.searchState.getQuery()();

    if (query === null) {
      return;
    }


    this.searchQuery.set(query || null);

    this.resetAndLoad();

  });



  async ngOnInit() {

    await this.loadProducts();

    this.setupObserver();

  }



  ngOnDestroy() {

    this.observer?.disconnect();

  }



  private resetAndLoad() {

    this.page = 0;

    this.products.set([]);

    this.hasMore = true;


    this.observer?.disconnect();


    this.loadProducts()
      .then(() => this.setupObserver());

  }



  applyFilters(filters: ProductFilter) {

    this.filters.set(filters);

    this.resetAndLoad();

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


    this.observer.observe(
      this.sentinel.nativeElement
    );

  }



  async loadProducts() {

    if (this.loading || !this.hasMore) {
      return;
    }


    this.loading = true;


    try {

      let response;


      const query = this.searchQuery();


      if (query) {

        response = await this.productService.search(
          query,
          this.page,
          this.size
        );

      } else {

        response = await this.productService.getProducts(
          this.page,
          this.size,
          this.filters()
        );

      }



      const content = response.content;


      this.products.update(current => [

        ...current,

        ...content

      ]);



      if (response.last || content.length === 0) {

        this.hasMore = false;

      }



      this.page++;


    } catch (error) {

      console.error(
        'Erro ao carregar produtos:',
        error
      );


    } finally {

      this.loading = false;

    }

  }

}