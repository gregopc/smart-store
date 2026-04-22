export interface ChatSuggestionResponse {
  message: string;
  suggestion: {
    reply: string;
    suggestedProducts: ProductDTO[];
  };
}

export interface ProductDTO {
  id: string;
  name: string;
  description: string;
  price: number;
  category: string;
  stock: number;
  imageUrl: string;
}
