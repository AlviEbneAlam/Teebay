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

export const ADD_PRODUCT = gql`
  mutation AddProduct($addProductRequest: AddProductInput!) {
    addProduct(addProductRequest: $addProductRequest) {
      statusCode
      statusMessage
    }
  }
`;

export const PRODUCTS_BY_USER_PAGINATED = gql`
  query ProductsByUserPaginated($page: Int!, $size: Int!) {
    productsByUserPaginated(page: $page, size: $size) {
      products {
        id
        title
        categories
        sellingPrice
        rent
        typeOfRent
        description
        createdAt
      }
      totalPages
      totalElements
      currentPage
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
