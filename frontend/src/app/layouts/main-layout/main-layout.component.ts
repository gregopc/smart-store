import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ChatWidgetComponent } from '../../shared/components/chat-widget/chat-widget.component';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { FooterComponent } from '../../shared/components/footer/footer.component';
import { SearchStateService } from '../../core/services/search-state.service';

@Component({
  selector: 'app-main-layout',
  imports: [
    RouterOutlet,
    ChatWidgetComponent,
    HeaderComponent,
    FooterComponent
  ],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css',
})
export class MainLayoutComponent {
  private searchState = inject(SearchStateService);

  onSearch(query: string) {
    this.searchState.setQuery(query);
  }
}
