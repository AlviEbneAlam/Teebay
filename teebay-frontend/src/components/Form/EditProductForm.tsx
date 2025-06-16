import React, { useState } from 'react';
import {
  Container,
  Title,
  TextInput,
  Textarea,
  NumberInput,
  Select,
  MultiSelect,
  Button,
  Paper,
  Group,
  Loader,
  Center,
  Text,
} from '@mantine/core';
import { useMutation, useQuery } from '@apollo/client';
import { useNavigate, useParams } from 'react-router-dom';
import { EDIT_PRODUCT } from '../../graphql/mutations';
import { PRODUCT_BY_ID } from '../../graphql/queries';
import { showNotification } from '@mantine/notifications';
import { useAuth } from '../../auth/useAuth';

export const EditProductForm: React.FC = () => {
  
  const navigate = useNavigate();
  const { id } = useParams(); 
  const { token } = useAuth();
  //console.log('productId from useParams:', productId);

  type RentType = 'PER_HOUR' | 'PER_DAY' | '';

  interface FormState {
    title: string;
    description: string;
    categoriesList: string[];
    sellingPrice: number;
    rent: number;
    typeOfRent: RentType;
  }

  const [formState, setFormState] = useState<FormState>({
    title: '',
    description: '',
    categoriesList: [],
    sellingPrice: 0,
    rent: 0,
    typeOfRent: '',
  });

  const {  loading: queryLoading, error: queryError } = useQuery(PRODUCT_BY_ID, {
  
    variables: { productId: Number(id) },
    context: {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
    fetchPolicy: 'network-only',
    onCompleted: (data) => {
      const product = data.productById;
      setFormState({
        title: product.title || '',
        description: product.description || '',
        categoriesList: product.categories || [],
        sellingPrice: product.sellingPrice || 0,
        rent: product.rent || 0,
        typeOfRent: product.typeOfRent || '',
      });
    },
  });

  const [editProduct, { loading }] = useMutation(EDIT_PRODUCT, {
    onCompleted: (data) => {
      showNotification({
        title: 'Success',
        message: data.editProduct.statusMessage,
        color: 'green',
      });
      navigate('/myProducts');
    },
    onError: (error) => {
      showNotification({
        title: 'Error',
        message: error.message,
        color: 'red',
      });
    },
  });

  const handleSubmit = () => {

    
    editProduct({
      variables: {
        productId: Number(id),
        editRequest: { ...formState },
      },
      context: {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      },
    })
      .then((response) => console.log('Edit response:', response))
      .catch((error) => console.error('Edit error:', error));
  };

  // Show loading state while fetching product
  if (queryLoading) {
    return (
      <Center mt="lg">
        <Loader />
      </Center>
    );
  }

  if (queryError) {
    return (
      <Center mt="lg">
        <Text color="red">Failed to load product. {queryError.message}</Text>
      </Center>
    );
  }

  return (
      
    <Container size="sm">
      <Paper withBorder shadow="md" p="xl" radius="md">
        <Title order={3} mb="lg" ta="center">
          Edit Product
        </Title>

        <TextInput
          label="Title"
          placeholder="Enter product title"
          value={formState.title}
          onChange={(e) => setFormState({ ...formState, title: e.target.value })}
          mb="md"
        />

        <Textarea
          label="Description"
          placeholder="Enter product description"
          value={formState.description}
          onChange={(e) => setFormState({ ...formState, description: e.target.value })}
          mb="md"
        />

        <MultiSelect
          label="Categories"
          placeholder="Select categories"
          data={["Electronics", "Books", "Toys", "Furniture", "Clothing"]}
          value={formState.categoriesList}
          onChange={(value) => setFormState({ ...formState, categoriesList: value })}
          mb="md"
        />

        <NumberInput
          label="Selling Price"
          value={formState.sellingPrice}
          onChange={(value) =>
            setFormState({ ...formState, sellingPrice: typeof value === 'number' ? value : 0 })
          }
          min={0}
          mb="md"
        />

        <NumberInput
          label="Rent"
          value={formState.rent}
          onChange={(value) =>
            setFormState({ ...formState, rent: typeof value === 'number' ? value : 0 })
          }
          min={0}
          mb="md"
        />

        <Select
          label="Type of Rent"
          placeholder="Select rent type"
          data={[
            { value: 'PER_HOUR', label: 'Per Hour' },
            { value: 'PER_DAY', label: 'Per Day' },
          ]}
          value={formState.typeOfRent}
          onChange={(value) => setFormState({ ...formState, typeOfRent: value as RentType })}
          mb="lg"
        />

        <Group justify="center">
          <Button onClick={handleSubmit} loading={loading}>
            Save Changes
          </Button>
        </Group>
      </Paper>
    </Container>
  );
};
