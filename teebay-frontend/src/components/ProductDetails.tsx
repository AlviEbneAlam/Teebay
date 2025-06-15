import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  Container,
  Title,
  Text,
  Paper,
  Group,
  Badge,
  Button,
  Divider,
} from '@mantine/core';

export const ProductDetails: React.FC = () => {
  const { state } = useLocation();
  const navigate = useNavigate();
  const product = state?.product;

  if (!product) {
    return <Text color="red">No product data found.</Text>;
  }

  return (
    <Container size="sm" mt="md">
      <Paper withBorder p="md" shadow="sm">
        <Group justify="space-between" mb="sm">
          <Title order={2}>{product.title}</Title>
          <Text size="sm" color="dimmed">
            {product.createdAt}
          </Text>
        </Group>

        <Group mb="sm">
          {product.categories.map((cat: string) => (
            <Badge key={cat}>{cat}</Badge>
          ))}
        </Group>

        <Text mb="xs" c="dimmed">
          Status: <strong>{product.availabilityStatus}</strong>
        </Text>

        <Text mb="xs">
          Price: {product.sellingPrice} |{' '}
          {product.rent != null
            ? `${product.rent} ${product.typeOfRent}`
            : 'Not for Rent'}
        </Text>

        {product.rentStartTime && product.rentEndTime && (
          <Text mb="xs" c="dimmed">
            Rented from <strong>{product.rentStartTime}</strong> to{' '}
            <strong>{product.rentEndTime}</strong>
          </Text>
        )}

        <Divider my="sm" />

        <Text size="sm" style={{ whiteSpace: 'pre-line' }}>
          {product.description}
        </Text>

        <Group mt="lg" justify="flex-end">
          <Button color="green">Buy</Button>
          <Button color="blue">Rent</Button>
        </Group>
      </Paper>

      <Group mt="md" justify="center">
        <Button variant="subtle" onClick={() => navigate(-1)}>
          Go Back
        </Button>
      </Group>
    </Container>
  );
};
