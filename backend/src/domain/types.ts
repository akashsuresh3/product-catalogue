export interface Rating {
  rate: number;
  count: number;
}

export interface Product {
  id: number;
  title: string;
  description: string;
  price: number;
  category: string;
  imageUrl: string;
  rating: Rating;
}

export interface Enquiry {
  productId: number;
  name: string;
  email: string;
  phone?: string;
  message: string;
}
