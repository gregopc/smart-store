import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SearchStateService {
  private query = signal<string | null>(null);

  setQuery(value: string | null) {
    this.query.set(value);
  }

  getQuery() {
    return this.query;
  }
}