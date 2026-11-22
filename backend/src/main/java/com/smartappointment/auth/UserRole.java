package com.smartappointment.auth;

/** Roles an {@link AppUser} can hold (SRS Table 9, ck_app_user_role). */
public enum UserRole {
  SYSTEM_ADMIN,
  ORGANIZATION_OWNER,
  STAFF,
  CUSTOMER
}
