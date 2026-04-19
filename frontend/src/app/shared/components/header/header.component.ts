import { Component, inject } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { ProductService } from '../../../core/services/product.service';

@Component({
  selector: 'app-header',
  imports: [MatIconModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  private productService = inject(ProductService);

  search() {
    console.log('Search');
  }

}
