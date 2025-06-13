// StepperForm.tsx
import { useState } from 'react';
import {
  Stepper,
  Button,
  Group,
  TextInput,
  Box, Stack,
  rem, Select, Textarea, Flex, Paper, Text
} from '@mantine/core';

export function StepperForm() {
  const [active, setActive] = useState(0);

  const [formValues, setFormValues] = useState({
    title: '',
    category: '',
    description: '',
    price: '',
    rent: '',
    buyerOption: '',
  });

  const handleChange = (field: string, value: string) => {
    setFormValues(prev => ({ ...prev, [field]: value }));
  };

   const isStepValid = () => {
    if (active === 0) return formValues.title.trim().length > 0;
    if (active === 1) return formValues.category.trim().length > 0;
    if (active === 2) return formValues.description.trim().length > 0;
    if (active === 3) {
    const buyerOption = formValues.buyerOption;
    const hasPrice = formValues.price.trim().length > 0;
    const hasRent = formValues.rent.trim().length > 0;

    if (buyerOption === 'Purchase') return hasPrice;
    if (buyerOption === 'Rent') return hasRent;
    if (buyerOption === 'Both') return hasPrice && hasRent;

    return false; 
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
              styles={{
                label: {
                  fontSize: rem(18),
                },
              }}
            />
          </Stack>
        </Stepper.Step>

        <Stepper.Step label="Step 2" description="Categories">
          <Stack gap="sm">
            <Select
              ta="center"
              mt="md"
              size="md"
              label="Select Categories"
              placeholder="Select a Category"
              data={['Electronics', 'Furniture', 'Home Appliances', 'Sporting Goods', 'Outdoor', 'Toys']}
              required
              value={formValues.category}
              onChange={(value) => handleChange('category', value || '')}
              styles={{
                label: {
                  fontSize: '1.125rem',
                  textAlign: 'center',
                },
              }}
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
              styles={{
                label: {
                  fontSize: rem(18),
                  textAlign: 'center',
                },
              }}
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
              styles={{
                label: {
                  fontSize: rem(18),
                },
                input: {
                  width: rem(200),
                  margin: '0 auto',
                },
              }}
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
              styles={{
                label: {
                  fontSize: rem(18),
                },
                input: {
                  width: rem(150),
                  margin: '0 auto',
                },
              }}
            />
            <Select
              ta="center"
              mt="md"
              size="sm"
              w={rem(180)}
              label="Buyer Options"
              placeholder="Select an Option"
              data={['Purchase', 'Rent', 'Both']}
              required
              value={formValues.buyerOption}
              onChange={(value) => handleChange('buyerOption', value || '')}
              styles={{
                label: {
                  fontSize: rem(18),
                  textAlign: 'center',
                },
              }}
            />
          </Flex>
        </Stepper.Step>

        <Stepper.Step label="Step 5" description="Confirm">
        <Paper withBorder p="md" radius="md">
            <Stack gap="sm">
            <Text fw={600} size="lg" ta="center">Summary</Text>
            <Text>Title: {formValues.title}</Text>
            <Text>Categories: {formValues.category}</Text>
            <Text>Description: {formValues.description}</Text>
            <Text>
                Price: { '$' }{formValues.price || '-'}{' '}
            </Text>
            <Text>
                Rent: { '$' }{formValues.rent || '-'}{' '}
                {formValues.rent && formValues.buyerOption === 'Rent' || formValues.buyerOption === 'Both' ? ' per day' : ''}
            </Text>
            </Stack>
        </Paper>
        </Stepper.Step>
      </Stepper>

      <Group justify="space-between" mt="xl">
        <Button variant="default" onClick={prevStep} disabled={active === 0}>
          Back
        </Button>
        {active < 4 ? (
          <Button onClick={nextStep} disabled={!isStepValid()}>
            Next
          </Button>
        ) : (
          <Button color="green" onClick={() => alert('Submitted!')}>
            Submit
          </Button>
        )}
      </Group>
    </Box>
  );
}
