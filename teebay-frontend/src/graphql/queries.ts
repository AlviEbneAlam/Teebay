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