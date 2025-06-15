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
} from '@mantine/core';
import { useMutation } from '@apollo/client';
import { useNavigate, useLocation } from 'react-router-dom';
import { EDIT_PRODUCT } from '../../graphql/mutations';
import { showNotification } from '@mantine/notifications';
import { useAuth } from '../../auth/useAuth';

export const EditProductForm: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const product = location.state?.product;
  const { token } = useAuth();

  const [formState, setFormState] = useState({
    title: product.title || '',
    description: product.description || '',
    categoriesList: product.categories || [],
    sellingPrice: product.sellingPrice || 0,
    rent: product.rent || 0,
    typeOfRent: product.typeOfRent || '',
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
        productId: product.id,
        editRequest: {
          ...formState,
        },
      },
      context: {
        headers: {
            Authorization: `Bearer ${token}`,
        },
    },
        }).then((response) => {
        console.log('Edit response:', response);
    }).catch((error) => {
        console.error('Edit error:', error);
    });
  };

  return (
    <Container size="sm" >
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
          onChange={(value) => setFormState({ ...formState, sellingPrice: value || 0 })}
          min={0}
          mb="md"
        />

        <NumberInput
          label="Rent"
          value={formState.rent}
          onChange={(value) => setFormState({ ...formState, rent: value || 0 })}
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
          onChange={(value) => setFormState({ ...formState, typeOfRent: value || '' })}
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
