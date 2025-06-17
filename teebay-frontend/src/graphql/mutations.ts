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
      statusCode
      message
      email
      firstName
    }
  }
`;

export const ADD_PRODUCT = gql`
  mutation AddProduct($addProductRequest: AddProductInput!) {
    addProduct(addProductRequest: $addProductRequest) {
      statusCode
      statusMessage
    }
  }
`;



export const EDIT_PRODUCT = gql`
  mutation EditProduct($productId: Int!, $editRequest: AddProductInput!) {
    editProduct(productId: $productId, editRequest: $editRequest) {
      statusCode
      statusMessage
    }
  }
`;

export const DELETE_PRODUCT = gql`
  mutation DeleteProduct($productId: Int!) {
    deleteProduct(productId: $productId) {
      statusCode
      statusMessage
    }
  }
`;

export const BUY_PRODUCT_MUTATION = gql`
  mutation BuyProduct($productId: Int!, $status: String!) {
    buyProduct(productId: $productId, status: $status) {
      statusCode
      statusMessage
    }
  }
`;

export const BOOK_FOR_RENT_MUTATION = gql`
  mutation BookForRent($productId: Int!, $rentStart: String!, $rentEnd: String!, $noOfHours: Int!) {
    bookForRent(productId: $productId, rentStart: $rentStart, rentEnd: $rentEnd, noOfHours: $noOfHours) {
      statusCode
      statusMessage
    }
  }
`;
