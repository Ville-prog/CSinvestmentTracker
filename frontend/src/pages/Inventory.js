/**
 * Inventory.js
 *
 * Inventory page displaying all tracked CS2 items with their latest price, total value, and P&L.
 * Fetches data from the backend database; does not call the Steam API directly.
 *
 * @author Ville Laaksoaho
 * Dependencies: InventoryTable.js, Dashboard.css
 */
import { useEffect, useState } from 'react';
import InventoryTable from '../components/InventoryTable';
import './Dashboard.css';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080';

/**
 * @brief Fetches and displays the full tracked inventory with per-item price and P&L data.
 *
 * @returns {JSX.Element} The inventory page with a sortable item table
 */
function Inventory() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch(`${API_BASE}/api/inventory/items`)
      .then(res => {
        if (!res.ok) throw new Error('Failed to fetch inventory');
        return res.json();
      })
      .then(data => {
        setItems(data);
        setLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setLoading(false);
      });
  }, []);

  if (loading) return <p className="status-text">Loading...</p>;
  if (error) return <p className="status-text error">{error}</p>;
  if (items.length === 0) return <p className="status-text">No inventory data yet. The nightly job has not run yet.</p>;

  const totalValue = items.reduce((sum, item) => sum + item.totalValueEur, 0);

  return (
    <div className="dashboard">
      <h1 className="page-title">Inventory</h1>

      <div className="stat-cards">
        <div className="stat-card">
          <span className="stat-label">Total Value</span>
          <span className="stat-value">€{totalValue.toFixed(2)}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Unique Items</span>
          <span className="stat-value">{items.length}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Total Units</span>
          <span className="stat-value">{items.reduce((sum, item) => sum + item.quantity, 0)}</span>
        </div>
      </div>

      <InventoryTable items={items} />
    </div>
  );
}

export default Inventory;
