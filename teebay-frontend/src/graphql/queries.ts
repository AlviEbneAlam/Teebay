import { gql } from '@apollo/client';

export const GET_ALL_PRODUCTS_PAGINATED = gql`
  query GetAllProductsPaginated($page: Int!, $size: Int!) {
    allProductsPaginated(page: $page, size: $size) {
      totalPages
      totalElements
      currentPage
      products {
        id
        title
        description
        categories
        sellingPrice
        rent
        typeOfRent
        availabilityStatus
        createdAt
        rentStartTime
        rentEndTime
      }
    }
  }
`;

export const PRODUCT_BY_ID = gql`
  query ProductById($productId: Int!) {
    productById(productId: $productId) {
      id
      title
      description
      categories
      sellingPrice
      rent
      typeOfRent
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