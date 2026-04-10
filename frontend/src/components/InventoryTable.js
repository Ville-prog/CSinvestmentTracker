/**
 * InventoryTable.js
 *
 * Sortable table displaying tracked CS2 inventory items with price, total value, and P&L.
 * Clicking a column header sorts the table by that column.
 *
 * @author Ville Laaksoaho
 * Dependencies: InventoryTable.css
 */
import { useState } from 'react';
import './InventoryTable.css';

/**
 * @brief Formats a number as a signed percentage string (e.g. "+4.21%" or "-1.50%").
 *
 * @param {number} pct The percentage value to format
 * @returns {string} Formatted percentage string with sign
 */
function formatPct(pct) {
  const sign = pct > 0 ? '+' : '';
  return `${sign}${pct.toFixed(2)}%`;
}

/**
 * @brief Sortable table of CS2 inventory items showing icon, name, quantity, price, total value, and P&L.
 *        Clicking a column header toggles sort direction for that column.
 *
 * @param {{ items: Array }} props
 * @param {Array} props.items List of InventoryItemView objects from the backend
 * @returns {JSX.Element} A sortable inventory table
 */
function InventoryTable({ items }) {
  const [sortKey, setSortKey] = useState('totalValueEur');
  const [sortAsc, setSortAsc] = useState(false);

  /**
   * @brief Handles a column header click — sets sort key or toggles direction if already active.
   *
   * @param {string} key The data key to sort by
   */
  function handleSort(key) {
    if (sortKey === key) {
      setSortAsc(v => !v);
    } else {
      setSortKey(key);
      setSortAsc(false);
    }
  }

  const sorted = [...items].sort((a, b) => {
    const av = a[sortKey];
    const bv = b[sortKey];
    if (typeof av === 'string') return sortAsc ? av.localeCompare(bv) : bv.localeCompare(av);
    return sortAsc ? av - bv : bv - av;
  });

  /**
   * @brief Renders a column header with a sort indicator arrow.
   *
   * @param {string} label Display label for the column
   * @param {string} col Data key this column sorts by
   * @returns {JSX.Element} A th element with sort indicator
   */
  function Th({ label, col }) {
    const active = sortKey === col;
    const arrow = active ? (sortAsc ? ' ↑' : ' ↓') : '';
    return (
      <th className={`sortable ${active ? 'active' : ''}`} onClick={() => handleSort(col)}>
        {label}{arrow}
      </th>
    );
  }

  return (
    <div className="table-wrapper">
      <table className="inventory-table">
        <thead>
          <tr>
            <th></th>
            <Th label="Name" col="name" />
            <Th label="Qty" col="quantity" />
            <Th label="Price" col="priceEur" />
            <Th label="Total Value" col="totalValueEur" />
            <Th label="P&L %" col="pnlPct" />
          </tr>
        </thead>
        <tbody>
          {sorted.map(item => (
            <tr key={item.marketHashName}>
              <td className="icon-cell">
                {item.iconUrl && (
                  <img
                    src={`https://community.akamai.steamstatic.com/economy/image/${item.iconUrl}/62fx62f`}
                    alt={item.name}
                    className="item-icon"
                  />
                )}
              </td>
              <td className="name-cell">{item.name}</td>
              <td>{item.quantity}</td>
              <td>€{item.priceEur.toFixed(2)}</td>
              <td>€{item.totalValueEur.toFixed(2)}</td>
              <td className={item.pnlPct > 0 ? 'positive' : item.pnlPct < 0 ? 'negative' : ''}>
                {formatPct(item.pnlPct)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default InventoryTable;
