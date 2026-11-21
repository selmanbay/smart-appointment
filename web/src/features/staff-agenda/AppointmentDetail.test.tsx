import { screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import type { Appointment } from '../../api/types';
import { renderWithTheme } from '../../test/renderWithTheme';
import { AppointmentDetail } from './AppointmentDetail';

const BOOKED: Appointment = {
  appointmentId: 103,
  serviceName: 'Academic advising',
  staffName: 'Maya Chen',
  customerName: 'Alex Morgan',
  start: '2026-11-23T10:00:00.000Z',
  end: '2026-11-23T10:30:00.000Z',
  status: 'BOOKED',
  estimatedWaitMinutes: 5,
};

describe('AppointmentDetail (UC-8)', () => {
  it('enables only the actions allowed from the current status', () => {
    renderWithTheme(
      <AppointmentDetail appointment={BOOKED} isSaving={false} onChangeStatus={vi.fn()} />,
    );

    expect(screen.getByRole('button', { name: 'Check in' })).toBeEnabled();
    expect(screen.getByRole('button', { name: 'Mark no-show' })).toBeEnabled();
    expect(screen.getByRole('button', { name: 'Start service' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Complete' })).toBeDisabled();
  });

  it('reports the chosen status', () => {
    const onChangeStatus = vi.fn();
    renderWithTheme(
      <AppointmentDetail appointment={BOOKED} isSaving={false} onChangeStatus={onChangeStatus} />,
    );

    screen.getByRole('button', { name: 'Check in' }).click();

    expect(onChangeStatus).toHaveBeenCalledWith('CHECKED_IN');
  });
});
