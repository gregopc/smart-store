import { Component, EventEmitter, Output, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';

import { CartService } from '../../../core/services/cart.service';

@Component({
  selector: 'app-header',
  imports: [MatIconModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {

  readonly cartService = inject(CartService);

  @Output() searchEvent = new EventEmitter<string>();

  search(query: string): void {
    this.searchEvent.emit(query.trim());
  }

  onInput(value: string): void {
    if (!value.trim()) {
      this.searchEvent.emit('');
    }
  }

}