import { Text } from '@mantine/core';
import dayjs from 'dayjs';
import { Columns } from '../../components/layout/Columns';
import { Page } from '../../components/layout/Page';
import { Section } from '../../components/layout/Section';
import { useSession } from '../../components/session/SessionContext';
import { AgendaList } from './AgendaList';
import { AgendaSummary } from './AgendaSummary';
import { AppointmentDetail } from './AppointmentDetail';
import { useAgenda } from './useAgenda';

export function StaffAgendaPage(): React.JSX.Element {
  const { account } = useSession();
  const agenda = useAgenda();
  const subtitle = `${dayjs(agenda.day).format('dddd D MMMM')} · ${account?.fullName ?? ''}`;

  return (
    <Page title="Daily agenda" subtitle={subtitle}>
      <AgendaSummary total={agenda.appointments.length} remaining={agenda.remaining} />
      <Columns>
        <Section title="Today" aside={`${String(agenda.appointments.length)} appointments`}>
          <AgendaList
            appointments={agenda.appointments}
            selectedId={agenda.selected?.appointmentId ?? null}
            onSelect={agenda.setSelectedId}
          />
        </Section>
        <Section title="Appointment">
          {agenda.selected === undefined ? (
            <Text c="dimmed">Select an appointment to record its progress.</Text>
          ) : (
            <AppointmentDetail
              appointment={agenda.selected}
              isSaving={agenda.isSaving}
              onChangeStatus={agenda.changeStatus}
            />
          )}
        </Section>
      </Columns>
    </Page>
  );
}
