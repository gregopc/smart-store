import { Component, EventEmitter, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-header',
  imports: [MatIconModule, RouterModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {

  @Output() searchEvent = new EventEmitter<string>();

  search(query: string) {
    this.searchEvent.emit(query.trim());
  }

  onInput(value: string) {
    if (!value.trim()) {
      this.searchEvent.emit('');
    }
  }
}
