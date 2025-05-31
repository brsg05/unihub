import { Criterio } from './criterio.model';

export interface NotaCriterio {
  id?: number; // May not have ID if it's part of a request DTO before creation
  criterioId: number;
  nota: number;
  // criterio?: Criterio; // Could be populated in responses
}

// If you have a specific DTO for NotaCriterio responses, define it here
// export interface NotaCriterioDto extends NotaCriterio {
//   criterio: Criterio;
// } 