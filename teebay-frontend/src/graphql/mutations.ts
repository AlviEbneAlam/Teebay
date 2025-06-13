import { gql } from '@apollo/client';

export const LOGIN_USER = gql`
  mutation Login($email: String!, $password: String!) {
    login(jwtRequest: { email: $email, password: $password }) {
      jwtToken
      message
    }
  }
`;

export const REGISTER_USER = gql`
  mutation RegisterUser($input: RegisterInput!) {
    register(userInfo: $input) {
      id
      email
      firstName
    }
  }
`;