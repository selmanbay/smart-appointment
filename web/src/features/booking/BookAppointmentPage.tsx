import { Stack } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { useNavigate } from 'react-router';
import { Columns } from '../../components/layout/Columns';
import { Page } from '../../components/layout/Page';
import { Section } from '../../components/layout/Section';
import { BookingChoices } from './BookingChoices';
import { BookingSummary } from './BookingSummary';
import { SlotGrid } from './SlotGrid';
import { useBookingForm, type BookingForm } from './useBookingForm';

/** UC-3 Book Appointment, with the staff recommendation (UC-18) and waiting estimate (UC-19). */
export function BookAppointmentPage(): React.JSX.Element {
  const form = useBookingForm();
  const navigate = useNavigate();
  const service = form.services.data?.find((option) => option.serviceId === form.serviceId);
  const staff = form.staffOptions.data?.find((option) => option.staffId === form.chosenStaffId);

  const confirm = (): void => {
    confirmBooking(form, () => {
      void navigate('/appointments');
    });
  };

  return (
    <Page title="Book appointment" subtitle="Choose a service, a staff member and a free time.">
      <Columns>
        <BookingChoices form={form} />
        <Stack gap="xl">
          <Section
            title="Time"
            aside={
              service === undefined
                ? undefined
                : `${String(service.durationMinutes)}-minute sessions`
            }
          >
            <SlotGrid
              slots={form.slots.data ?? []}
              selectedStart={form.slotStart}
              onSelect={form.setSlotStart}
            />
          </Section>
          <BookingSummary
            slotStart={form.slotStart}
            serviceName={service?.name}
            staffName={staff?.fullName}
            isSaving={form.booking.isPending}
            onConfirm={confirm}
          />
        </Stack>
      </Columns>
    </Page>
  );
}

function confirmBooking(form: BookingForm, onBooked: () => void): void {
  if (form.serviceId === null || form.chosenStaffId === null || form.slotStart === null) {
    return;
  }
  const request = { serviceId: form.serviceId, staffId: form.chosenStaffId, start: form.slotStart };
  form.booking.mutate(request, {
    onSuccess: () => {
      notifications.show({
        title: 'Appointment booked',
        message: 'A confirmation e-mail is on its way.',
      });
      onBooked();
    },
  });
}
