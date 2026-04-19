/**
 * Dashboard.js
 *
 * Main dashboard page displaying a hero portfolio value stat, 24h change, charts, and the full inventory table.
 * Fetches the latest portfolio snapshot, inventory items, and portfolio history on mount.
 *
 * @author Ville Laaksoaho
 * Dependencies: PortfolioChart.js, PortfolioValueChart.js, InventoryTable.js, Dashboard.css
 */
import { useEffect, useState } from 'react';
import PortfolioChart from '../components/PortfolioChart';
import PortfolioValueChart from '../components/PortfolioValueChart';
import InventoryTable from '../components/InventoryTable';
import './Dashboard.css';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080';

/**
 * @brief Renders the dashboard with a hero value stat, 24h change badge, chart tabs, and inventory table.
 *
 * @returns {JSX.Element} The full dashboard page
 */
function Dashboard() {
  const [snapshot, setSnapshot] = useState(null);
  const [items, setItems] = useState([]);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [chartView, setChartView] = useState('performance');

  useEffect(() => {
    Promise.all([
      fetch(`${API_BASE}/api/portfolio/latest`).then(r => r.ok ? r.json() : null),
      fetch(`${API_BASE}/api/inventory/items`).then(r => r.ok ? r.json() : []),
      fetch(`${API_BASE}/api/portfolio/history`).then(r => r.ok ? r.json() : [])
    ])
      .then(([snapshotData, itemsData, historyData]) => {
        setSnapshot(snapshotData);
        setItems(itemsData);
        setHistory(historyData || []);
        setLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setLoading(false);
      });
  }, []);

  if (loading) return <p className="status-text">Loading...</p>;
  if (error) return <p className="status-text error">{error}</p>;

  const totalValue = snapshot?.totalValueEur ?? items.reduce((sum, item) => sum + item.totalValueEur, 0);
  const totalUnits = items.reduce((sum, item) => sum + item.quantity, 0);

  const dailyChange = history.length >= 2
    ? history[history.length - 1].totalValueEur - history[history.length - 2].totalValueEur
    : null;
  const dailyChangePct = dailyChange != null && history[history.length - 2].totalValueEur > 0
    ? (dailyChange / history[history.length - 2].totalValueEur * 100)
    : null;

  return (
    <div className="dashboard">
      <div className="hero-stats">
        <div className="hero-stats-value">
          €{totalValue.toFixed(2)}
          {dailyChange != null && (
            <span className={`hero-stats-change ${dailyChange < 0 ? 'negative' : ''}`}>
              {dailyChange >= 0 ? '▲' : '▼'} {dailyChange >= 0 ? '+' : ''}€{Math.abs(dailyChange).toFixed(2)}
              {dailyChangePct != null && `(${dailyChangePct >= 0 ? '+' : ''}${dailyChangePct.toFixed(2)}%)`}
              <span className={`hero-stats-change-tag ${dailyChange < 0 ? 'negative' : ''}`}>24H</span>
            </span>
          )}
        </div>
        <div className="hero-stats-meta">
          <span><strong>{items.length}</strong> unique items</span>
          <span><strong>{totalUnits}</strong> total units</span>
        </div>
      </div>

      {snapshot && (
        <>
          <div className="chart-tabs">
            <button
              className={`chart-tab ${chartView === 'performance' ? 'active' : ''}`}
              onClick={() => setChartView('performance')}
            >
              P&L %
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
    </div>
  );
}

export default Dashboard;
