import { Injectable, inject } from "@angular/core"
import { firstValueFrom } from "rxjs"
import { HttpClient } from "@angular/common/http"
import { environment } from "../../shared/environments/environments"
import { Product } from "../models/product";
import { PaginatedApiResponse } from "../../shared/interfaces/api";

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private readonly API_URL =  `${environment.apiUrl}/products`

  private http = inject(HttpClient);

  async getProducts(page = 0, size = 12) {
    return firstValueFrom(
      this.http.get<PaginatedApiResponse<Product>>(
        `${this.API_URL}`,
        {
          params: {
            page,
            size
          }
        }
      )
    );
  }

  async getProduct(id: string): Promise<Product> {
    return firstValueFrom(this.http.get<Product>(`${this.API_URL}/${id}`));
  }

  async search(query: string, page = 0, size = 12) {
    return firstValueFrom(
      this.http.get<PaginatedApiResponse<Product>>(
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
