import { Stack } from '@mantine/core';
import { Section } from '../../components/layout/Section';
import { DayPicker } from './DayPicker';
import { ServicePicker } from './ServicePicker';
import { StaffPicker } from './StaffPicker';
import type { BookingForm } from './useBookingForm';

/** Left column of the booking screen: service, date and staff (UC-3 steps 1–3). */
export function BookingChoices({ form }: Readonly<{ form: BookingForm }>): React.JSX.Element {
  return (
    <Stack gap="xl">
      <Section title="Service">
        <ServicePicker
          services={form.services.data ?? []}
          value={form.serviceId}
          onChange={form.setServiceId}
        />
      </Section>
      <Section title="Date">
        <DayPicker value={form.day} onChange={form.chooseDay} />
      </Section>
      <Section title="Staff" aside="Sorted by estimated wait">
        <StaffPicker
          staffOptions={form.staffOptions.data ?? []}
          value={form.chosenStaffId}
          onChange={form.chooseStaff}
        />
      </Section>
    </Stack>
  );
}
