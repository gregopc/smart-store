import { Injectable, inject } from "@angular/core"
import { firstValueFrom } from "rxjs"
import { HttpClient, HttpParams } from "@angular/common/http"

import { environment } from "../../shared/environments/environments"
import { Product } from "../models/product";
import { PaginatedApiResponse } from "../../shared/interfaces/api";
import { ProductFilter } from "../../shared/interfaces/product-filter";


@Injectable({
  providedIn: 'root',
})
export class ProductService {

  private readonly API_URL = `${environment.apiUrl}/products`

  private http = inject(HttpClient);


  async getProducts(
    page = 0,
    size = 12,
    filters?: ProductFilter
  ) {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size);


    if (filters?.category) {
      params = params.set(
        'category',
        filters.category
      );
    }


    if (filters?.minPrice !== undefined) {
      params = params.set(
        'minPrice',
        filters.minPrice
      );
    }


    if (filters?.maxPrice !== undefined) {
      params = params.set(
        'maxPrice',
        filters.maxPrice
      );
    }


    if (filters?.inStock !== undefined) {
      params = params.set(
        'inStock',
        filters.inStock
      );
    }


    return firstValueFrom(
      this.http.get<PaginatedApiResponse<Product[]>>(
        this.API_URL,
        { params }
      )
    );
  }


  async getProduct(id: string): Promise<Product> {
    return firstValueFrom(
      this.http.get<Product>(
        `${this.API_URL}/${id}`
      )
    );
  }


  async search(
    query: string,
    page = 0,
    size = 12
  ) {

    return firstValueFrom(
      this.http.get<PaginatedApiResponse<Product[]>>(
        `${this.API_URL}/search`,
        {
          params: {
            query,
            page,
            size
          }
        }
      )
    );
  }
}