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
  Modal,
} from '@mantine/core';
import { showNotification } from '@mantine/notifications';
import { useQuery, useMutation } from '@apollo/client';
import { useNavigate, Link } from 'react-router-dom';
import { PRODUCTS_BY_USER_PAGINATED, DELETE_PRODUCT } from '../graphql/mutations';
import { useAuth } from '../auth/useAuth';

export interface Product {
  id: number;
  title: string;
  categories: string[];
  sellingPrice: number;
  rentPrice?: number | null;
  typeOfRent?: string | null;
  description: string;
  availabilityStatus: string;
  createdAt: string;
}

export const MyProducts: React.FC = () => {
  const [page, setPage] = useState(1);
  const [showMoreMap, setShowMoreMap] = useState<Record<number, boolean>>({});
  const [productToDelete, setProductToDelete] = useState<Product | null>(null);
  const size = 10;
  const { token } = useAuth();
  const navigate = useNavigate();

  const { data, loading, error, refetch } = useQuery(PRODUCTS_BY_USER_PAGINATED, {
    variables: { page: page - 1, size },
    context: {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
    fetchPolicy: 'network-only',
  });

  const [deleteProduct] = useMutation(DELETE_PRODUCT, {
    context: {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
    onCompleted: (data) => {
      showNotification({
        title: 'Product Deleted',
        message: data.deleteProduct.statusMessage,
        color: 'green',
      });
      setProductToDelete(null);
      refetch(); // refresh list
    },
    onError: (err) => {
      showNotification({
        title: 'Error',
        message: err.message,
        color: 'red',
      });
      setProductToDelete(null);
      refetch();
    },
  });

  if (loading) return <Text>Loading...</Text>;
  if (error) return <Text color="red">Error: {error.message}</Text>;

  const { products, totalPages } = data.productsByUserPaginated;

  const toggleShowMore = (productId: number) => {
    setShowMoreMap((prev) => ({
      ...prev,
      [productId]: !prev[productId],
    }));
  };

  const handleDelete = () => {
    if (productToDelete) {
      deleteProduct({ variables: { productId: productToDelete.id } });
    }
  };

  return (
    <Container size="md" mt="md">
      <Group justify="space-between" mb="lg">
        <Title order={2}>My Products</Title>
        <Button component={Link} to="/StepperForm">
          Add Product
        </Button>
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
              onClick={() =>
                navigate(`/edit-product/${product.id}`, { state: { product } })
              }
            >
              <Group justify="space-between" mb={6}>
                <Text fw={600}>{product.title}</Text>
                <Text size="sm" c="gray">
                  {product.createdAt}
                </Text>
              </Group>

              <Text size="sm" c="dimmed" mb={4}>
                {product.categories.join(', ')}
              </Text>

              <Text size="sm" mb={4}>
                {product.sellingPrice} |{' '}
                {product.rentPrice != null
                  ? `${product.rentPrice} ${product.typeOfRent}`
                  : 'N/A'}
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

              <Group justify="flex-end" mt="sm" onClick={(e) => e.stopPropagation()}>
                <Button
                  color="red"
                  size="xs"
                  onClick={() => setProductToDelete(product)}
                >
                  Delete
                </Button>
              </Group>
            </Paper>
          );
        })}
      </Stack>

      <Group mt="lg" justify="center">
        <Pagination total={totalPages} value={page} onChange={setPage} />
      </Group>

      <Modal
        opened={!!productToDelete}
        onClose={() => setProductToDelete(null)}
        title="Confirm Deletion"
        centered
      >
        <Text mb="md">Are you sure you want to delete this product?</Text>
        <Group justify="flex-end">
          <Button variant="default" onClick={() => setProductToDelete(null)}>
            No
          </Button>
          <Button color="red" onClick={handleDelete}>
            Yes, Delete
          </Button>
        </Group>
      </Modal>
    </Container>
  );
};
