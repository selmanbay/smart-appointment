/* Synthetic fixtures for UI review only. No measured results or production rules. */
window.CNG491_DEMO = {
  date: '2026-10-27',
  organization: 'Northbridge University',
  staff: [
    { name: 'Maya Chen', initials: 'MC', title: 'Academic advisor', color: '', wait: 5, rescheduleWait: 3 },
    { name: 'Daniel Brooks', initials: 'DB', title: 'Senior advisor', color: 'peach', wait: 15, rescheduleWait: 10 },
    { name: 'Elena Ortiz', initials: 'EO', title: 'Student services', color: 'lilac', wait: 4 },
    { name: 'Oliver Reed', initials: 'OR', title: 'Registration officer', color: '', wait: 6 }
  ],
  services: ['Academic advising', 'Document verification', 'Course registration'],
  organizations: [
    { name: 'Northbridge University', type: 'University', owner: 'Sarah Mitchell', status: 'Active' },
    { name: 'Westfield Dental', type: 'Dental practice', owner: 'Marcus Taylor', status: 'Active' },
    { name: 'Oakwell Health Clinic', type: 'Clinic', owner: 'Emma Richardson', status: 'Active' },
    { name: 'Riverside University', type: 'University', owner: 'David Park', status: 'Active' },
    { name: 'Cedar Dental Studio', type: 'Dental practice', owner: 'Isabella Ross', status: 'Inactive' },
    { name: 'Meridian Care', type: 'Clinic', owner: 'Noah Sullivan', status: 'Active' }
  ],
  // [staff index, weekday (Mon=0), start hour, duration in hours, customer, service index, status]
  calendar: [
    [0, 0, 9, .5, 'Sofia Martin', 0, 'Completed'],
    [1, 0, 11, 1, 'William Davis', 0, 'Completed'],
    [2, 0, 14, 1, 'Ava Brown', 1, 'Completed'],
    [0, 1, 9, .5, 'Sofia Martin', 0, 'Completed'],
    [0, 1, 9.5, .5, 'James Wilson', 0, 'Completed'],
    [0, 1, 10, .5, 'Alex Morgan', 0, 'Confirmed'],
    [0, 1, 11.5, .5, 'Olivia Bennett', 0, 'Confirmed'],
    [0, 1, 13, 1, 'Ethan Parker', 0, 'Confirmed'],
    [0, 1, 15, 1, 'Lily Anderson', 0, 'Confirmed'],
    [1, 1, 9.5, 1, 'Noah Clarke', 0, 'In service'],
    [1, 1, 11, 1, 'Emily Foster', 0, 'Confirmed'],
    [1, 1, 13.5, 1, 'Henry Adams', 0, 'Confirmed'],
    [1, 1, 15.5, .5, 'Grace Lee', 0, 'Confirmed'],
    [2, 1, 9, 1, 'Amelia Scott', 1, 'Completed'],
    [2, 1, 10.5, .5, 'Lucas Evans', 1, 'Confirmed'],
    [2, 1, 13, 1.5, 'Chloe Williams', 1, 'Confirmed'],
    [2, 1, 15, 1, 'Jack Thompson', 1, 'Confirmed'],
    [3, 1, 9.5, 1.5, 'Isabella Harris', 2, 'In service'],
    [3, 1, 13.5, 1, 'Benjamin Young', 2, 'Confirmed'],
    [3, 1, 15.5, 1, 'Ella Robinson', 2, 'Confirmed'],
    [0, 2, 9, .5, 'Mia Walker', 0, 'Confirmed'],
    [3, 2, 11, 1, 'Oliver Wright', 2, 'Confirmed'],
    [2, 2, 14, 1.5, 'Sophia King', 1, 'Confirmed'],
    [1, 3, 10, 1.5, 'Jacob White', 0, 'Confirmed'],
    [2, 3, 13, 1, 'Charlotte Green', 1, 'Confirmed'],
    [3, 3, 15, 1, 'Leo Baker', 2, 'Confirmed'],
    [0, 4, 9.5, .5, 'Harper Hill', 0, 'Confirmed'],
    [3, 4, 11, 1.5, 'Mason Carter', 2, 'Confirmed'],
    [1, 4, 14, 1, 'Evelyn Turner', 0, 'Confirmed']
  ]
};
