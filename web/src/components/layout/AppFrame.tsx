import {
  AppShell,
  Burger,
  Button,
  Group,
  NavLink,
  Stack,
  Text,
  useMantineTheme,
} from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { IconCalendarPlus, IconCalendarTime, IconListCheck } from '@tabler/icons-react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router';
import type { Role } from '../../api/types';
import { useSession } from '../session/SessionContext';

const NAVIGATION: Record<Role, { label: string; path: string; icon: React.JSX.Element }[]> = {
  CUSTOMER: [
    { label: 'Book appointment', path: '/book', icon: <IconCalendarPlus size={18} /> },
    { label: 'My appointments', path: '/appointments', icon: <IconListCheck size={18} /> },
  ],
  STAFF: [{ label: 'Daily agenda', path: '/agenda', icon: <IconCalendarTime size={18} /> }],
  ORGANIZATION_OWNER: [],
  SYSTEM_ADMIN: [],
};

const ROLE_LABEL: Record<Role, string> = {
  CUSTOMER: 'Customer',
  STAFF: 'Staff',
  ORGANIZATION_OWNER: 'Organization owner',
  SYSTEM_ADMIN: 'System admin',
};

export function AppFrame(): React.JSX.Element {
  const theme = useMantineTheme();
  const { account } = useSession();
  const { pathname } = useLocation();
  const links = account === null ? [] : NAVIGATION[account.role];
  const [isNavigationOpen, navigation] = useDisclosure(false);

  return (
    <AppShell
      header={{ height: 56 }}
      navbar={{ width: 240, breakpoint: 'sm', collapsed: { mobile: !isNavigationOpen } }}
      padding="lg"
    >
      <AppShell.Header px="lg">
        <FrameHeader isNavigationOpen={isNavigationOpen} onToggleNavigation={navigation.toggle} />
      </AppShell.Header>
      <AppShell.Navbar p="sm">
        <Stack gap="xs">
          {account !== null && (
            <Text size="xs" c="dimmed" px="sm" pb="xs">
              {account.organizationName}
            </Text>
          )}
          {links.map((link) => (
            <NavLink
              key={link.path}
              component={Link}
              to={link.path}
              label={link.label}
              leftSection={link.icon}
              active={pathname === link.path}
              onClick={navigation.close}
            />
          ))}
        </Stack>
      </AppShell.Navbar>
      <AppShell.Main bg={theme.other.canvas as string}>
        <Outlet />
      </AppShell.Main>
    </AppShell>
  );
}

function FrameHeader({
  isNavigationOpen,
  onToggleNavigation,
}: Readonly<{ isNavigationOpen: boolean; onToggleNavigation: () => void }>): React.JSX.Element {
  const { account, signOut } = useSession();
  const navigate = useNavigate();
  const handleSignOut = (): void => {
    signOut();
    void navigate('/sign-in');
  };
  return (
    <Group h="100%" justify="space-between">
      <Group gap="sm">
        <Burger
          opened={isNavigationOpen}
          onClick={onToggleNavigation}
          hiddenFrom="sm"
          size="sm"
          aria-label="Toggle navigation"
        />
        <Text fw={600}>Smart Appointment</Text>
      </Group>
      {account !== null && (
        <Group gap="sm">
          <Text size="sm" visibleFrom="sm">
            {account.fullName}
          </Text>
          <Text size="sm" c="dimmed" visibleFrom="sm">
            {ROLE_LABEL[account.role]}
          </Text>
          <Button variant="subtle" size="xs" onClick={handleSignOut}>
            Sign out
          </Button>
        </Group>
      )}
    </Group>
  );
}
