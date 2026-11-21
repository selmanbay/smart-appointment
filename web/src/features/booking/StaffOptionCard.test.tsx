import { Radio } from '@mantine/core';
import { screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import type { StaffOption } from '../../api/types';
import { renderWithTheme } from '../../test/renderWithTheme';
import { StaffOptionCard } from './StaffOptionCard';

const STAFF: StaffOption = {
  staffId: 1,
  fullName: 'Maya Chen',
  title: 'Academic advisor',
  nextFreeTime: '2026-11-23T09:00:00.000Z',
  estimatedWaitMinutes: 5,
  bookedSlots: 6,
  totalSlots: 14,
  meanRating: 4.7,
  isRecommended: true,
};

function renderCard(staff: StaffOption): void {
  renderWithTheme(
    <Radio.Group>
      <StaffOptionCard staff={staff} />
    </Radio.Group>,
  );
}

describe('StaffOptionCard', () => {
  it('shows the facts behind the recommendation', () => {
    renderCard(STAFF);

    expect(screen.getByText('5 min')).toBeInTheDocument();
    expect(screen.getByText('6 of 14 slots booked')).toBeInTheDocument();
    expect(screen.getByText('Recommended: shortest estimated wait')).toBeInTheDocument();
  });

  it('does not show the recommendation line for other staff', () => {
    renderCard({ ...STAFF, isRecommended: false });

    expect(screen.queryByText(/Recommended/)).not.toBeInTheDocument();
  });
});
