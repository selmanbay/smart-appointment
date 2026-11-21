import dayjs from 'dayjs';

// One place for date and time formats (docs/design/UI_GUIDELINES.md section 4).

export function formatTime(isoDateTime: string): string {
  return dayjs(isoDateTime).format('HH:mm');
}

export function formatDay(isoDateTime: string): string {
  return dayjs(isoDateTime).format('ddd D MMM');
}

export function formatTimeRange(start: string, end: string): string {
  return `${formatTime(start)}–${formatTime(end)}`;
}

export function formatMinutes(minutes: number): string {
  return `${String(minutes)} min`;
}
