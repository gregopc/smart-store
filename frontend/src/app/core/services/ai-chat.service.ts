import { Injectable, inject } from "@angular/core"
import { firstValueFrom } from "rxjs"
import { HttpClient } from "@angular/common/http"
import { environment } from "../../shared/environments/environments"
import { PaginatedApiResponse } from "../../shared/interfaces/api";

@Injectable({
  providedIn: 'root',
})
export class AiChatService {
  private readonly API_URL =  `${environment.apiUrl}/...`

  private http = inject(HttpClient);

}
