import React, { useState } from 'react';
import {
  Container,
  Title,
  Text,
  Paper,
  Pagination,
  Stack,
  Group,
  Button,
  Badge,
} from '@mantine/core';
import { useQuery } from '@apollo/client';
import { useNavigate } from 'react-router-dom';
import { GET_ALL_PRODUCTS_PAGINATED } from '../graphql/queries';
import { useAuth } from '../auth/useAuth';

export interface Product {
  id: number;
  title: string;
  categories: string[];
  sellingPrice: number;
  rent: number;
  typeOfRent?: string | null;
  description: string;
  availabilityStatus: string;
  createdAt: string;
  rentStartTime?: string | null;
  rentEndTime?: string | null;
}

export const AllProducts: React.FC = () => {
  const [page, setPage] = useState(1);
  const [showMoreMap, setShowMoreMap] = useState<Record<number, boolean>>({});
  const size = 10;
  const { token } = useAuth();
  const navigate = useNavigate();

  const { data, loading, error } = useQuery(GET_ALL_PRODUCTS_PAGINATED, {
    variables: { page: page - 1, size },
    context: {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
    fetchPolicy: 'cache-and-network',
  });

  if (loading) return <Text>Loading...</Text>;
  if (error) return <Text color="red">Error: {error.message}</Text>;

  const { products, totalPages } = data.allProductsPaginated;

  const toggleShowMore = (productId: number) => {
    setShowMoreMap((prev) => ({
      ...prev,
      [productId]: !prev[productId],
    }));
  };

  return (
    <Container size="md" mt="md">
      <Group justify="space-between" mb="lg">
        <Title order={2}>All Products</Title>
      </Group>

      <Stack gap="md">
        {products.map((product: Product) => {
          const isLong = product.description.length > 150;
          const showMore = showMoreMap[product.id] || false;

          return (
            <Paper
              withBorder
              p="md"
              shadow="xs"
              key={product.id}
              style={{ cursor: 'pointer' }}
              onClick={() => navigate(`/product/${product.id}`, { state: { product } })}
            >
              <Group justify="space-between" mb={6}>
                <Text fw={600}>{product.title}</Text>
                <Text size="sm" c="gray">
                  {product.createdAt}
                </Text>
              </Group>

              <Group gap="xs" mb={4}>
                {product.categories.map((cat) => (
                  <Badge key={cat}>{cat}</Badge>
                ))}
              </Group>

              <Text size="sm" mb={4}>
                Price: ${product.sellingPrice} | Rent:{' '}
                {product.rent != null && product.typeOfRent
                    ? `$${product.rent} per ${product.typeOfRent === 'PER_DAY' ? 'day' : 'hour'}`
                    : 'N/A'}
                </Text>

              <Text size="sm" mb={4}>
                Availability: <b>{product.availabilityStatus}</b>
              </Text>

              <Text size="sm" onClick={(e) => e.stopPropagation()}>
                {isLong && !showMore
                  ? `${product.description.slice(0, 150)}... `
                  : product.description}{' '}
                {isLong && (
                  <Button
                    variant="subtle"
                    size="compact-sm"
                    onClick={() => toggleShowMore(product.id)}
                    ml="xs"
                    px={0}
                  >
                    {showMore ? 'Show Less' : 'More Details'}
                  </Button>
                )}
              </Text>
            </Paper>
          );
        })}
      </Stack>

      <Group mt="lg" justify="center">
        <Pagination total={totalPages} value={page} onChange={setPage} />
      </Group>
    </Container>
  );
};
