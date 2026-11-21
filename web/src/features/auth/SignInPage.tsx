import { Button, Center, Paper, Stack, Text, Title } from '@mantine/core';
import { useNavigate } from 'react-router';
import { DEMO_ACCOUNTS } from '../../api/mockStore';
import type { Account } from '../../api/types';
import { useSession } from '../../components/session/SessionContext';

const HOME_BY_ROLE: Record<Account['role'], string> = {
  CUSTOMER: '/book',
  STAFF: '/agenda',
  ORGANIZATION_OWNER: '/sign-in',
  SYSTEM_ADMIN: '/sign-in',
};

/** Prototype sign-in with demo accounts. Replaced by e-mail and password once the auth API exists (F1). */
export function SignInPage(): React.JSX.Element {
  const { signIn } = useSession();
  const navigate = useNavigate();

  const handleSignIn = (account: Account): void => {
    signIn(account);
    void navigate(HOME_BY_ROLE[account.role]);
  };

  return (
    <Center mih="100vh" bg="gray.0" p="lg">
      <Paper withBorder p="xl" w={400} maw="100%">
        <Stack gap="lg">
          <Stack gap={4}>
            <Title order={1}>Sign in</Title>
            <Text c="dimmed">Prototype: choose a demo account.</Text>
          </Stack>
          <Stack gap="sm">
            {DEMO_ACCOUNTS.map((account) => (
              <Button
                key={account.userId}
                variant="default"
                size="md"
                justify="space-between"
                onClick={() => {
                  handleSignIn(account);
                }}
                rightSection={
                  <Text size="sm" c="dimmed">
                    {account.role === 'CUSTOMER' ? 'Customer' : 'Staff'}
                  </Text>
                }
              >
                {account.fullName}
              </Button>
            ))}
          </Stack>
        </Stack>
      </Paper>
    </Center>
  );
}
