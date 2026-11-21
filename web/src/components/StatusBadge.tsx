import { Badge } from '@mantine/core';
import type { AppointmentStatus } from '../api/types';

const STATUS_STYLE: Record<AppointmentStatus, { label: string; color: string }> = {
  BOOKED: { label: 'Booked', color: 'gray' },
  CHECKED_IN: { label: 'Checked in', color: 'yellow' },
  IN_SERVICE: { label: 'In service', color: 'accent' },
  COMPLETED: { label: 'Completed', color: 'gray' },
  CANCELLED: { label: 'Cancelled', color: 'red' },
  NO_SHOW: { label: 'No-show', color: 'red' },
};

export function StatusBadge({
  status,
}: Readonly<{ status: AppointmentStatus }>): React.JSX.Element {
  const style = STATUS_STYLE[status];
  return <Badge color={style.color}>{style.label}</Badge>;
}
