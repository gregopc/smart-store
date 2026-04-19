import { Input } from '@angular/core';
import { Component } from '@angular/core';

@Component({
  selector: 'app-card',
  imports: [],
  templateUrl: './card.html',
  styleUrl: './card.css',
})
export class Card {
  @Input() imageUrl: string = 'https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fimg.freepik.com%2Fpsd-premium%2Farroz-bruto-em-saco-isolado-representando-graos-alimentares-e-ingredientes-na-agricultura-png-transparencia-com-sombra_185216-985.jpg%3Fw%3D996&f=1&nofb=1&ipt=06759728456e00f5b0e156897466fd1f85cdcc17655814b6cb0d028d3eef6b77';
  @Input() alt: string = '';
  @Input() title: string = '';
  @Input() description: string = '';
  @Input() price: string = '';
}
