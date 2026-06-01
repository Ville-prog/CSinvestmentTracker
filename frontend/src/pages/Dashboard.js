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

  const totalUnits = items.reduce((sum, item) => sum + item.quantity, 0);

  const latestValue = history.length >= 1 ? history[history.length - 1].totalValueEur : null;
  const prevValue = history.length >= 2 ? history[history.length - 2].totalValueEur : null;
  const change = latestValue != null && prevValue != null ? latestValue - prevValue : null;
  const changePct = change != null && prevValue > 0 ? (change / prevValue) * 100 : null;
  const positive = change != null && change >= 0;

  return (
    <div className="dashboard">
      <div className="app-header">
        <div className="app-title-row">
          <img src="/favicon.ico" className="app-favicon" alt="" />
          <span className="app-name">CS2 Portfolio</span>
        </div>
        {latestValue != null && (
          <div className="app-subtitle-row">
            <span className="app-value">€{latestValue.toFixed(2)}</span>
            {change != null && (
              <span className={`app-change ${positive ? 'positive' : 'negative'}`}>
                {positive ? '+' : ''}€{change.toFixed(2)} ({positive ? '+' : ''}{changePct.toFixed(2)}%)
              </span>
            )}
          </div>
        )}
      </div>

      <div className="hero-stats-meta">
        <span><strong>{items.length}</strong> unique items</span>
        <span><strong>{totalUnits}</strong> total units</span>
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
