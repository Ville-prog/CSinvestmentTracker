import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import InventoryTable from './InventoryTable';

const sampleItems = [
  {
    name: 'AK-47 | Redline (Field-Tested)',
    marketHashName: 'AK-47 | Redline (Field-Tested)',
    iconUrl: null,
    quantity: 2,
    priceEur: 12.0,
    totalValueEur: 24.0,
    pnlPct: 20.0,
  },
  {
    name: 'USP-S | Kill Confirmed (Field-Tested)',
    marketHashName: 'USP-S | Kill Confirmed (Field-Tested)',
    iconUrl: null,
    quantity: 1,
    priceEur: 8.0,
    totalValueEur: 8.0,
    pnlPct: -10.0,
  },
  {
    name: 'Glock-18 | Water Elemental (Field-Tested)',
    marketHashName: 'Glock-18 | Water Elemental (Field-Tested)',
    iconUrl: null,
    quantity: 3,
    priceEur: 5.0,
    totalValueEur: 15.0,
    pnlPct: 0.0,
  },
];

test('renders all item names', () => {
  render(<InventoryTable items={sampleItems} />);
  expect(screen.getByText('AK-47 | Redline (Field-Tested)')).toBeInTheDocument();
  expect(screen.getByText('USP-S | Kill Confirmed (Field-Tested)')).toBeInTheDocument();
  expect(screen.getByText('Glock-18 | Water Elemental (Field-Tested)')).toBeInTheDocument();
});

test('applies positive class to positive P&L', () => {
  render(<InventoryTable items={sampleItems} />);
  const cell = screen.getByText('+20.00%');
  expect(cell).toHaveClass('positive');
});

test('applies negative class to negative P&L', () => {
  render(<InventoryTable items={sampleItems} />);
  const cell = screen.getByText('-10.00%');
  expect(cell).toHaveClass('negative');
});

test('applies no color class to zero P&L', () => {
  render(<InventoryTable items={sampleItems} />);
  const cell = screen.getByText('0.00%');
  expect(cell).not.toHaveClass('positive');
  expect(cell).not.toHaveClass('negative');
});

test('defaults to sorting by total value descending', () => {
  render(<InventoryTable items={sampleItems} />);
  const rows = screen.getAllByRole('row').slice(1); // skip header
  expect(rows[0]).toHaveTextContent('AK-47 | Redline');
  expect(rows[1]).toHaveTextContent('Glock-18 | Water Elemental');
  expect(rows[2]).toHaveTextContent('USP-S | Kill Confirmed');
});

test('sorts by name ascending on header click', () => {
  render(<InventoryTable items={sampleItems} />);
  userEvent.click(screen.getByText('Name'));
  const rows = screen.getAllByRole('row').slice(1);
  expect(rows[0]).toHaveTextContent('AK-47 | Redline');
  expect(rows[1]).toHaveTextContent('Glock-18 | Water Elemental');
  expect(rows[2]).toHaveTextContent('USP-S | Kill Confirmed');
});

test('toggles sort direction on second header click', () => {
  render(<InventoryTable items={sampleItems} />);
  userEvent.click(screen.getByText('Name'));
  userEvent.click(screen.getByText(/Name/));
  const rows = screen.getAllByRole('row').slice(1);
  expect(rows[0]).toHaveTextContent('USP-S | Kill Confirmed');
});
