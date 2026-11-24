export interface InventoryItem {
  id: string;
  name: string;
  quantity: number;
  unit: string;
  location?: string;
  updatedAt: Date;
  createdAt: Date;
}

export interface CreateInventoryItem {
  name: string;
  quantity: number;
  unit: string;
  location?: string;
}

export interface UpdateInventoryItem extends Partial<CreateInventoryItem> {}