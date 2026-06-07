export type UserRole = 'STUDENT' | 'COMPANY' | 'ADMIN';

export interface AuthResponse {
  token: string;
  userId: number;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  profileImageUrl?: string;
  cvUrl?: string;
  bio?: string;
  skills?: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
  bio: string;
  skills: string;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}
