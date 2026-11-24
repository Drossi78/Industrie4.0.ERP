import { RoleName } from './user.model';

export interface RegisterUserRequest {
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  role: RoleName;
}
