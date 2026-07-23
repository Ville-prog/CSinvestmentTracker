import { useEffect, useState } from 'react';
import PortfolioChart from '../components/PortfolioChart';
import PortfolioValueChart from '../components/PortfolioValueChart';
import InventoryTable from '../components/InventoryTable';
import './Dashboard.css';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080';

function Dashboard() {
  const [snapshot, setSnapshot] = useState(null);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [chartView, setChartView] = useState('performance');

  useEffect(() => {
    Promise.all([
      fetch(`${API_BASE}/api/portfolio/latest`).then(r => r.ok ? r.json() : null),
      fetch(`${API_BASE}/api/inventory/items`).then(r => r.ok ? r.json() : []),
    ])
      .then(([snapshotData, itemsData]) => {
        setSnapshot(snapshotData);
        setItems(itemsData);
        setLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setLoading(false);
      });
  }, []);

  if (loading) return <p className="status-text">Loading...</p>;
  if (error) return <p className="status-text error">{error}</p>;

  const totalValue = items.reduce((sum, item) => sum + item.totalValueEur, 0);
  const totalUnits = items.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <main className="dashboard">
      <h1 className="page-title">CS2 Portfolio</h1>
      {snapshot && (
        <p className="snapshot-date">
          Last updated: {snapshot.date.split('-').reverse().join('.')}
        </p>
      )}

      <section className="stat-cards" aria-label="Portfolio summary">
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
          <span className="stat-value">{totalUnits}</span>
        </div>
      </section>

      {snapshot && (
        <>
          <div className="chart-tabs" aria-label="Chart view">
            <button
              className={`chart-tab ${chartView === 'performance' ? 'active' : ''}`}
              onClick={() => setChartView('performance')}
            >
              P&amp;L %
            </button>
            <button
              className={`chart-tab ${chartView === 'value' ? 'active' : ''}`}
              onClick={() => setChartView('value')}
            >
              Total Value
            </button>
          </div>
          {chartView === 'performance' ? <PortfolioChart /> : <PortfolioValueChart />}
        </>
      )}

      {items.length > 0 && (
        <>
          <h2 className="section-title">Inventory</h2>
          <InventoryTable items={items} />
        </>
      )}
    </main>
  );
}

export default Dashboard;
