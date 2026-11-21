import type { ReactNode } from 'react';
import { Navigate } from 'react-router';
import type { Role } from '../../api/types';
import { useSession } from './SessionContext';

export function RequireRole({
  role,
  children,
}: Readonly<{
  role: Role;
  children: ReactNode;
}>): React.JSX.Element {
  const { account } = useSession();
  if (account?.role !== role) {
    return <Navigate to="/sign-in" replace />;
  }
  return <>{children}</>;
}
