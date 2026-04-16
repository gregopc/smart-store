import { Injectable, inject } from "@angular/core"
import { firstValueFrom } from "rxjs"
import { HttpClient } from "@angular/common/http"
import { environment } from "../../shared/enviroments/environments"
import { Product } from "../models/product";
import { ApiResponse } from "../../shared/interfaces/api";

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private readonly API_URL =  `${environment.apiUrl}/products`

  private http = inject(HttpClient);

  async getProducts(): Promise<ApiResponse<Product[]>> {
    return firstValueFrom(this.http.get<ApiResponse<Product[]>>(this.API_URL));
  }

  async getProduct(id: string): Promise<ApiResponse<Product>> {
    return firstValueFrom(this.http.get<ApiResponse<Product>>(`${this.API_URL}/${id}`));
  }
}
