export interface NotificationI {
  id: number;
  type: string;
  datetime?: Date;
  subject?: string;
  from?: string;
  isUrgent?: boolean;
  note?: string;
}
