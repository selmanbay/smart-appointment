import dayjs from 'dayjs';

const DAYS_SHOWN = 7;
const WEEKEND_DAYS = [0, 6];

/** The next working days, as YYYY-MM-DD. Prototype only: replaced by the organization's working hours from the API. */
export function upcomingWorkingDays(): string[] {
  const days: string[] = [];
  let candidate = dayjs().startOf('day');
  while (days.length < DAYS_SHOWN) {
    if (!WEEKEND_DAYS.includes(candidate.day())) {
      days.push(candidate.format('YYYY-MM-DD'));
    }
    candidate = candidate.add(1, 'day');
  }
  return days;
}

/** The day the prototype treats as "today": today on weekdays, otherwise the next weekday. */
export function firstWorkingDay(): string {
  return upcomingWorkingDays()[0] ?? dayjs().format('YYYY-MM-DD');
}
