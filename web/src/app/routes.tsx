import { createBrowserRouter, Navigate } from 'react-router';
import { AppFrame } from '../components/layout/AppFrame';
import { RequireRole } from '../components/session/RequireRole';
import { SignInPage } from '../features/auth/SignInPage';
import { BookAppointmentPage } from '../features/booking/BookAppointmentPage';
import { MyAppointmentsPage } from '../features/my-appointments/MyAppointmentsPage';
import { StaffAgendaPage } from '../features/staff-agenda/StaffAgendaPage';

export const router = createBrowserRouter([
  { path: '/sign-in', element: <SignInPage /> },
  {
    element: <AppFrame />,
    children: [
      { index: true, element: <Navigate to="/sign-in" replace /> },
      {
        path: 'book',
        element: (
          <RequireRole role="CUSTOMER">
            <BookAppointmentPage />
          </RequireRole>
        ),
      },
      {
        path: 'appointments',
        element: (
          <RequireRole role="CUSTOMER">
            <MyAppointmentsPage />
          </RequireRole>
        ),
      },
      {
        path: 'agenda',
        element: (
          <RequireRole role="STAFF">
            <StaffAgendaPage />
          </RequireRole>
        ),
      },
    ],
  },
  { path: '*', element: <Navigate to="/sign-in" replace /> },
]);
