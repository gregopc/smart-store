import { Component, EventEmitter, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-header',
  imports: [MatIconModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {

  @Output() searchEvent = new EventEmitter<string>();

  search(query: string) {
    if (!query.trim()) return;

    this.searchEvent.emit(query);
  }
}
