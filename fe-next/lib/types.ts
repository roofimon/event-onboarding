export interface Profile {
  name: string;
  email: string;
  phone: string;
  salary: number;
  yearsOfExperience: number;
}

export interface OnboardingState {
  applicationId: string | null;
  email: string;
  name: string;
  phone: string;
  salary: number | null;
  yearsOfExperience: number | null;
  score: number | null;
  approved: boolean | null;
  profile: Profile | null;
}

export interface FulfillmentPayload {
  name: string;
  email: string;
  phone: string;
  salary: number;
  yearsOfExperience: number;
}
