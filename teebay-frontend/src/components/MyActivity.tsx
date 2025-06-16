import {
  Container,
  Title,
  Tabs,
  Text,
  Loader,
  Pagination,
  Stack,
  Group,
  Paper,
  Badge,
  Button,
} from '@mantine/core';
import React, { useState } from 'react';
import { useQuery } from '@apollo/client';
import type { DocumentNode } from '@apollo/client';
import { useAuth } from '../auth/useAuth';
import { useNavigate } from 'react-router-dom';
import {
  BOUGHT_PRODUCTS_BY_USER,
  BORROWED_PRODUCTS_BY_USER,
  PRODUCTS_BY_USER_AND_STATUS,
} from '../graphql/queries';

// ------------------------
// Types
// ------------------------

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

interface ProductPage {
  products: Product[];
  totalPages: number;
}

interface ProductCardProps {
  product: Product;
  toggleShowMore: (id: number) => void;
  showMore: boolean;
}

interface ProductListProps {
  query: DocumentNode;
  variables: Record<string, any>;
}

// ------------------------
// Components
// ------------------------

const ProductCard: React.FC<ProductCardProps> = ({
  product,
  toggleShowMore,
  showMore,
}) => {
  const navigate = useNavigate();
  const isLong = product.description.length > 150;

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
          ? `${product.description.slice(0, 150)}...`
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
};

const ProductList: React.FC<ProductListProps> = ({ query, variables }) => {
  const [page, setPage] = useState(1);
  const [showMoreMap, setShowMoreMap] = useState<Record<number, boolean>>({});
  const { token } = useAuth();

  const { data, loading, error } = useQuery(query, {
    variables: { ...variables, page: page - 1 },
    context: {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
    fetchPolicy: 'cache-and-network',
  });

  const toggleShowMore = (productId: number) => {
    setShowMoreMap((prev) => ({
      ...prev,
      [productId]: !prev[productId],
    }));
  };

  if (loading) return <Loader mt="md" />;
  if (error) return <Text color="red">Error: {error.message}</Text>;

  const productPage: ProductPage =
    data?.boughtProductsByUser ??
    data?.getBorrowedProductsByUser ??
    data?.productsByUserAndStatus ??
    { products: [], totalPages: 1 };

  const { products, totalPages } = productPage;

  return (
    <>
      <Stack gap="md" mt="md">
        {products.map((product) => (
          <ProductCard
            key={product.id}
            product={product}
            toggleShowMore={toggleShowMore}
            showMore={showMoreMap[product.id] || false}
          />
        ))}
      </Stack>
      <Group mt="lg" justify="center">
        <Pagination total={totalPages} value={page} onChange={setPage} />
      </Group>
    </>
  );
};

export const MyActivity: React.FC = () => {
  const size = 10;

  return (
    <Container size="md" mt="lg">
      <Title order={2} mb="lg" ta="center">
        My Activity
      </Title>

      <Tabs defaultValue="bought" keepMounted={false}>
        <Tabs.List>
          <Tabs.Tab value="bought">Bought</Tabs.Tab>
          <Tabs.Tab value="sold">Sold</Tabs.Tab>
          <Tabs.Tab value="borrowed">Borrowed</Tabs.Tab>
          <Tabs.Tab value="lent">Lent</Tabs.Tab>
        </Tabs.List>

        <Tabs.Panel value="bought" pt="md">
          <ProductList query={BOUGHT_PRODUCTS_BY_USER} variables={{ size }} />
        </Tabs.Panel>

        <Tabs.Panel value="sold" pt="md">
          <ProductList
            query={PRODUCTS_BY_USER_AND_STATUS}
            variables={{ size, status: 'SOLD' }}
          />
        </Tabs.Panel>

        <Tabs.Panel value="borrowed" pt="md">
          <ProductList query={BORROWED_PRODUCTS_BY_USER} variables={{ size }} />
        </Tabs.Panel>

        <Tabs.Panel value="lent" pt="md">
          <ProductList
            query={PRODUCTS_BY_USER_AND_STATUS}
            variables={{ size, status: 'RENTED' }}
          />
        </Tabs.Panel>
      </Tabs>
    </Container>
  );
};
