export interface PasswordResetRequestRequest {
  email: string;
}

export interface PasswordResetRequest {
  token: string;
  newPassword: string;
}
