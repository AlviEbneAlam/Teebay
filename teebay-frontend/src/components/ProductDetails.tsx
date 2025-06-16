import React, { useState } from 'react';
import {
  Container,
  Title,
  Text,
  Paper,
  Group,
  Badge,
  Button,
  Divider,
  Modal,
  NumberInput,
} from '@mantine/core';
import { useLocation, useNavigate } from 'react-router-dom';
import { useMutation } from '@apollo/client';
import {
  BOOK_FOR_RENT_MUTATION,
  BUY_PRODUCT_MUTATION,
} from '../graphql/mutations';
import { useAuth } from '../auth/useAuth';

export const ProductDetails: React.FC = () => {
  const { state } = useLocation();
  const navigate = useNavigate();
  const { token } = useAuth();
  const product = state?.product;

  const [showBuyConfirm, setShowBuyConfirm] = useState(false);
  const [showRentModal, setShowRentModal] = useState(false);
  const [showRentConfirmModal, setShowRentConfirmModal] = useState(false);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [rentHours, setRentHours] = useState<number>(1);
  const [rentStartFormatted, setRentStartFormatted] = useState('');
  const [rentEndFormatted, setRentEndFormatted] = useState('');

  const today = new Date();
  const todayYMD = today.toISOString().split('T')[0];

  const isValidRange =
    startDate &&
    (product?.typeOfRent === 'PER_DAY' ? endDate && startDate <= endDate : true);

  const [buyProduct] = useMutation(BUY_PRODUCT_MUTATION, {
    variables: { productId: product?.id, status: 'SOLD' },
    context: { headers: { Authorization: `Bearer ${token}` } },
    onCompleted: (data) => {
      alert(data?.buyProduct?.statusMessage || 'Product successfully bought');
      navigate('/myProducts');
    },
    onError: (err) => alert(err.message),
  });

  const [bookForRent] = useMutation(BOOK_FOR_RENT_MUTATION, {
    context: { headers: { Authorization: `Bearer ${token}` } },
    onCompleted: (data) => {
      alert(data?.bookForRent?.statusMessage || 'Product successfully rented');
      navigate('/myProducts');
    },
    onError: (err) => alert(err.message),
  });

  const pad = (n: number) => n.toString().padStart(2, '0');

  const formatToAMPM = (datetimeStr: string): string => {
    const [datePart, timePart] = datetimeStr.split('T');
    const [year, month, day] = datePart.split('-').map(Number);
    const [hour, minute] = timePart.split(':').map(Number);
    const date = new Date(year, month - 1, day, hour, minute);

    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
      hour12: true,
    });
  };

  const calculateEndTime = (startStr: string, hours: number): string => {
    const [datePart, timePart] = startStr.split('T');
    const [year, month, day] = datePart.split('-').map(Number);
    const [hour, minute] = timePart.split(':').map(Number);
    const startDate = new Date(year, month - 1, day, hour, minute);
    const endDate = new Date(startDate.getTime() + hours * 60 * 60 * 1000);

    return endDate.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
      hour12: true,
    });
  };

 const handleRentConfirm = () => {
  if (!startDate) {
    alert('Please select a start date/time.');
    return;
  }

  if (product.typeOfRent === 'PER_DAY') {
    if (!endDate || startDate > endDate) {
      alert('Please select a valid end date.');
      return;
    }

    const rentStart = `${startDate} 00:00:00`;
    const rentEnd = `${endDate} 23:59:59`;

    bookForRent({
      variables: {
        productId: product.id,
        rentStart,
        rentEnd,
        noOfHours: 0,
      },
    });

    setShowRentModal(false);
    return;
  }

  // PER_HOUR: Show confirmation modal
  const startAMPM = formatToAMPM(startDate);
  const endAMPM = calculateEndTime(startDate, rentHours);

  setRentStartFormatted(startAMPM);
  setRentEndFormatted(endAMPM);
  setShowRentConfirmModal(true);
};


  const confirmHourlyBooking = () => {
  const [datePart, timePart] = startDate.split('T');
  const [hour, minute] = timePart.split(':').map(Number);
  const startDateObj = new Date(
    Number(datePart.slice(0, 4)),
    Number(datePart.slice(5, 7)) - 1,
    Number(datePart.slice(8, 10)),
    hour,
    minute
  );

  const endDateObj = new Date(startDateObj.getTime() + rentHours * 60 * 60 * 1000);

  const rentStart = `${datePart} ${pad(hour)}:${pad(minute)}:00`;
  const rentEnd = `${endDateObj.getFullYear()}-${pad(endDateObj.getMonth() + 1)}-${pad(
    endDateObj.getDate()
  )} ${pad(endDateObj.getHours())}:${pad(endDateObj.getMinutes())}:00`;

  bookForRent({
    variables: {
      productId: product.id,
      rentStart,
      rentEnd,
      noOfHours: rentHours,
    },
  });

  setShowRentConfirmModal(false);
  setShowRentModal(false);
};

  if (!product) return <Text color="red">No product data found.</Text>;

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
          Price: ${product.sellingPrice} |{' '} $
          {product.rent != null
            ? `${product.rent} ${product.typeOfRent.toLowerCase().replace('_', ' ')}`
            : 'Not for Rent'}
        </Text>

        {product.rentStartTime && product.rentEndTime && (
            <Text mb="xs" c="dimmed">
              Rented from <strong>{formatToAMPM(product.rentStartTime)}</strong> to{' '}
              <strong>{formatToAMPM(product.rentEndTime)}</strong>
            </Text>
          )}

        <Divider my="sm" />
        <Text size="sm" style={{ whiteSpace: 'pre-line' }}>
          {product.description}
        </Text>

        <Group mt="lg" justify="flex-end">
          <Button color="green" onClick={() => setShowBuyConfirm(true)}>
            Buy
          </Button>
          <Button color="blue" onClick={() => setShowRentModal(true)}>
            Rent
          </Button>
        </Group>
      </Paper>

      <Group mt="md" justify="center">
        <Button variant="subtle" onClick={() => navigate(-1)}>
          Go Back
        </Button>
      </Group>

      {/* Buy Confirmation Modal */}
      <Modal
        opened={showBuyConfirm}
        onClose={() => setShowBuyConfirm(false)}
        title="Confirm Purchase"
        centered
      >
        <Text mb="md">Are you sure you want to buy this product?</Text>
        <Group justify="flex-end">
          <Button variant="default" onClick={() => setShowBuyConfirm(false)}>
            No
          </Button>
          <Button color="green" onClick={() => buyProduct()}>
            Yes, Buy
          </Button>
        </Group>
      </Modal>

      {/* Rent Modal */}
      <Modal
        opened={showRentModal}
        onClose={() => setShowRentModal(false)}
        title="Select Rental Period"
        centered
        size="sm"
      >
        <div style={{ paddingTop: '1rem' }}>
          {product.typeOfRent === 'PER_DAY' ? (
            <>
              <label style={{ display: 'block', marginBottom: 10 }}>
                Start Date:
                <input
                  type="date"
                  value={startDate}
                  min={todayYMD}
                  onChange={(e) => setStartDate(e.target.value)}
                  style={{ width: '100%', padding: '8px', marginTop: 4 }}
                />
              </label>

              <label style={{ display: 'block', marginBottom: 10 }}>
                End Date:
                <input
                  type="date"
                  value={endDate}
                  min={startDate || todayYMD}
                  onChange={(e) => setEndDate(e.target.value)}
                  style={{ width: '100%', padding: '8px', marginTop: 4 }}
                />
              </label>
            </>
          ) : (
            <>
              <label style={{ display: 'block', marginBottom: 10 }}>
                Start Date & Time:
                <input
                  type="datetime-local"
                  value={startDate}
                  min={new Date().toISOString().slice(0, 16)}
                  onChange={(e) => setStartDate(e.target.value)}
                  style={{ width: '100%', padding: '8px', marginTop: 4 }}
                />
              </label>

              <NumberInput
                label="Number of Hours"
                value={rentHours}
                min={1}
                step={1}
                allowDecimal={false}
                onChange={(val) => {
                  if (typeof val === 'number') setRentHours(val);
                }}
                style={{ marginTop: 10 }}
              />
            </>
          )}

          <Group justify="flex-end" mt="md">
            <Button variant="default" onClick={() => setShowRentModal(false)}>
              Cancel
            </Button>
            <Button color="blue" onClick={handleRentConfirm} disabled={!isValidRange}>
              Confirm Rent
            </Button>
          </Group>
        </div>
      </Modal>

      {/* Confirm Rent Modal */}
      <Modal
        opened={showRentConfirmModal}
        onClose={() => setShowRentConfirmModal(false)}
        title="Confirm Rental Period"
        centered
      >
        <Text mb="md">
          Do you want to confirm booking from <strong>{rentStartFormatted}</strong> to{' '}
          <strong>{rentEndFormatted}</strong>?
        </Text>
        <Group justify="flex-end">
          <Button variant="default" onClick={() => setShowRentConfirmModal(false)}>
            Cancel
          </Button>
          <Button color="blue" onClick={confirmHourlyBooking}>
            Confirm Booking
          </Button>
        </Group>
      </Modal>
    </Container>
  );
};
