// StepperForm.tsx
import { useState } from 'react';
import {
  Stepper,
  Button,
  Group,
  TextInput,
  Box, Stack,
  rem, Select, Textarea, Flex, Paper, Text, MultiSelect
} from '@mantine/core';
import { useMutation } from '@apollo/client';
import { ADD_PRODUCT } from '../../graphql/mutations';
import { useAuth } from '../../auth/useAuth';


export function StepperForm() {
  const [active, setActive] = useState(0);
  const [addProduct] = useMutation(ADD_PRODUCT);
  const { token } = useAuth();

  const [formValues, setFormValues] = useState({
    title: '',
    category: [] as string[],
    description: '',
    price: '',
    rent: '',
    rentType: '',
  });

  const handleChange = (field: string, value: string | string[]) => {
    setFormValues((prev) => ({ ...prev, [field]: value }));
  };

  const submitProduct = async () => {
    try {
      const input = {
        title: formValues.title,
        categoriesList: formValues.category,
        description: formValues.description,
        sellingPrice: parseFloat(formValues.price),
        rent: parseFloat(formValues.rent),
        typeOfRent: formValues.rentType.replace(' ', '_').toUpperCase(),
      };

      const { data } = await addProduct({
        variables: { addProductRequest: input },
        context: {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        },
      });

      alert(`Product added! Status: ${data.addProduct.statusMessage}`);
    } catch (err) {
      console.error('Submission error', err);
      alert('Failed to add product');
    }
  };

  const isStepValid = () => {
    if (active === 0) return formValues.title.trim().length > 0;
    if (active === 1) return formValues.category.length > 0;
    if (active === 2) return formValues.description.trim().length > 0;
    if (active === 3) {
      const {  price, rent, rentType } = formValues;
      const hasPrice = price.trim().length > 0;
      const hasRent = rent.trim().length > 0;
      const hasRentType = rentType.trim().length > 0;

      return hasPrice && hasRent && hasRentType;
    }
    return true;
  };

  const nextStep = () => setActive((current) => (current < 4 ? current + 1 : current));
  const prevStep = () => setActive((current) => (current > 0 ? current - 1 : current));

  return (
    <Box maw={600} mx="auto">
      <Stepper active={active} onStepClick={setActive} styles={{ steps: { display: 'none' } }}>
        <Stepper.Step label="Step 1" description="Title" mt="md">
          <Stack gap="sm">
            <TextInput
              ta="center"
              mt="md"
              size="md"
              label="Select a title for your product"
              placeholder="Title"
              required
              value={formValues.title}
              onChange={(e) => handleChange('title', e.currentTarget.value)}
              styles={{ label: { fontSize: rem(18) } }}
            />
          </Stack>
        </Stepper.Step>

        <Stepper.Step label="Step 2" description="Categories">
          <Stack gap="sm">
            <MultiSelect
              mt="md"
              size="md"
              label="Select Categories"
              placeholder="Select one or more categories"
              data={['Electronics', 'Furniture', 'Home Appliances', 'Sporting Goods', 'Outdoor', 'Toys']}
              required
              value={formValues.category}
              onChange={(value) => handleChange('category', value)}
              styles={{ label: { fontSize: '1.125rem', textAlign: 'center' } }}
            />
          </Stack>
        </Stepper.Step>

        <Stepper.Step label="Step 3" description="Description">
          <Stack gap="sm">
            <Textarea
              ta="center"
              mt="md"
              size="md"
              label="Select description"
              placeholder="Enter a detailed description"
              required
              minRows={6}
              autosize
              value={formValues.description}
              onChange={(e) => handleChange('description', e.currentTarget.value)}
              styles={{ label: { fontSize: rem(18), textAlign: 'center' } }}
            />
          </Stack>
        </Stepper.Step>

        <Stepper.Step label="Step 4" description="Price">
          <Flex justify="center" align="flex-start" gap="md" wrap="wrap">
            <TextInput
              ta="center"
              mt="md"
              size="sm"
              label="Select Price"
              placeholder="Purchase price"
              required
              value={formValues.price}
              onChange={(e) => handleChange('price', e.currentTarget.value)}
              styles={{ label: { fontSize: rem(18) }, input: { width: rem(200), margin: '0 auto' } }}
            />
            <TextInput
              ta="center"
              mt="md"
              size="sm"
              label="Rent"
              placeholder="Rent"
              required
              value={formValues.rent}
              onChange={(e) => handleChange('rent', e.currentTarget.value)}
              styles={{ label: { fontSize: rem(18) }, input: { width: rem(150), margin: '0 auto' } }}
            />
           
            <Select
              mt="md"
              size="sm"
              w={rem(180)}
              label="Type of Rent"
              placeholder="Choose rent type"
              data={['Per Hour', 'Per Day']}
              value={formValues.rentType}
              onChange={(value) => handleChange('rentType', value || '')}
              styles={{ label: { fontSize: rem(18), textAlign: 'center' } }}
            />
          </Flex>
        </Stepper.Step>

        <Stepper.Step label="Step 5" description="Confirm">
          <Paper withBorder p="md" radius="md">
            <Stack gap="sm">
              <Text fw={600} size="lg" ta="center">Summary</Text>
              <Text>Title: {formValues.title}</Text>
              <Text>Categories: {formValues.category.join(', ')}</Text>
              <Text>Description: {formValues.description}</Text>
              <Text>Price: ${formValues.price || '-'}</Text>
              <Text>
               Rent: ${formValues.rent || '-'} {formValues.rentType}
              </Text>
            </Stack>
          </Paper>
        </Stepper.Step>
      </Stepper>

      <Group justify="space-between" mt="xl">
        <Button variant="default" onClick={prevStep} disabled={active === 0}>Back</Button>
        {active < 4 ? (
          <Button onClick={nextStep} disabled={!isStepValid()}>Next</Button>
        ) : (
          <Button color="green" onClick={submitProduct}>Submit</Button>
        )}
      </Group>
    </Box>
  );
}
