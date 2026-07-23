/**
 * Dashboard.js
 *
 * Main dashboard page showing portfolio value, key stats, performance charts, and inventory.
 * Fetches portfolio history, inventory items, the latest snapshot, and 7-day S&P 500 data on mount.
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
 * @brief Fetches all portfolio data and renders the hero card, header stats, chart tabs, and inventory table.
 *
 * @returns {JSX.Element} The dashboard page
 */
function Dashboard() {
  const [snapshot, setSnapshot] = useState(null);
  const [items, setItems] = useState([]);
  const [history, setHistory] = useState([]);
  const [sp500History, setSp500History] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [chartView, setChartView] = useState('performance');

  useEffect(() => {
    const sevenDaysAgo = new Date();
    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 7);
    const sevenDaysAgoStr = sevenDaysAgo.toISOString().split('T')[0];

    Promise.all([
      fetch(`${API_BASE}/api/portfolio/latest`).then(r => r.ok ? r.json() : null),
      fetch(`${API_BASE}/api/inventory/items`).then(r => r.ok ? r.json() : []),
      fetch(`${API_BASE}/api/portfolio/history`).then(r => r.ok ? r.json() : []),
      fetch(`${API_BASE}/api/market/sp500?from=${sevenDaysAgoStr}`).then(r => r.ok ? r.json() : []),
    ])
      .then(([snapshotData, itemsData, historyData, sp500Data]) => {
        setSnapshot(snapshotData);
        setItems(itemsData);
        setHistory(historyData || []);
        setSp500History(sp500Data || []);
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
  const costBasis = items.reduce((sum, item) => sum + item.costBasisPerUnit * item.quantity, 0);

  const latestValue = history.length >= 1 ? history[history.length - 1].totalValueEur : null;
  const prevValue = history.length >= 2 ? history[history.length - 2].totalValueEur : null;
  const change = latestValue != null && prevValue != null ? latestValue - prevValue : null;
  const changePct = change != null && prevValue > 0 ? (change / prevValue) * 100 : null;
  const positive = change != null && change >= 0;

  const sevenDaysAgoStr = (() => { const d = new Date(); d.setDate(d.getDate() - 7); return d.toISOString().split('T')[0]; })();
  const sevenDayEntry = history.length > 0 ? [...history].reverse().find(h => h.date <= sevenDaysAgoStr) : null;
  const sevenDayChange = latestValue != null && sevenDayEntry ? latestValue - sevenDayEntry.totalValueEur : null;
  const sevenDayPct = sevenDayChange != null && sevenDayEntry.totalValueEur > 0 ? (sevenDayChange / sevenDayEntry.totalValueEur) * 100 : null;

  const sp500First = sp500History.length > 0 ? sp500History[0].close : null;
  const sp500Last = sp500History.length > 0 ? sp500History[sp500History.length - 1].close : null;
  const sp500Pct = sp500First && sp500Last && sp500First > 0 ? ((sp500Last - sp500First) / sp500First) * 100 : null;

  return (
    <div className="dashboard">
      <header className="page-header">
        <p className="eyebrow">Counter-Strike 2 / Personal inventory</p>
        <h1>Portfolio</h1>
        <p className="page-intro">Market value and performance of a single tracked inventory.</p>
      </header>

      <section className="portfolio-header" aria-label="Portfolio summary">
        <div className="portfolio-header-left">
          <div className="portfolio-title">
            <span className="portfolio-title-label">Current market value</span>
          </div>
          <div className="portfolio-value-row">
            <span className="portfolio-value">€{latestValue != null ? latestValue.toFixed(2) : '—'}</span>
            {change != null && (
              <>
                <span className={`portfolio-change-eur ${positive ? 'positive' : 'negative'}`}>
                  {positive ? '+' : ''}€{change.toFixed(2)}
                </span>
                <span className={`portfolio-change-pct ${positive ? 'positive' : 'negative'}`}>
                  {positive ? '+' : ''}{changePct.toFixed(2)}%
                </span>
              </>
            )}
          </div>
          <div className="portfolio-meta">
            <span><strong>{items.length}</strong> unique items</span>
            <span className="meta-divider">/</span>
            <span><strong>{totalUnits}</strong> total units</span>
          </div>
        </div>

        <div className="portfolio-header-right">
          <div className="portfolio-stat">
            <span className="portfolio-stat-label">7-day P&L</span>
            <span className={`portfolio-stat-value ${sevenDayPct == null ? '' : sevenDayPct >= 0 ? 'positive' : 'negative'}`}>
              {sevenDayPct != null ? `${sevenDayPct >= 0 ? '+' : ''}${sevenDayPct.toFixed(2)}%` : '—'}
            </span>
          </div>
          <div className="portfolio-stat">
            <span className="portfolio-stat-label">vs S&P 500</span>
            <span className={`portfolio-stat-value ${sp500Pct == null ? '' : sp500Pct >= 0 ? 'positive' : 'negative'}`}>
              {sp500Pct != null ? `${sp500Pct >= 0 ? '+' : ''}${sp500Pct.toFixed(2)}%` : '—'}
            </span>
          </div>
          <div className="portfolio-stat">
            <span className="portfolio-stat-label">Cost basis</span>
            <span className="portfolio-stat-value">€{costBasis.toFixed(1)}</span>
          </div>
        </div>
      </section>

      {snapshot && (
        <>
          <div className="chart-tabs" aria-label="Chart view">
            <button
              className={`chart-tab ${chartView === 'performance' ? 'active' : ''}`}
              onClick={() => setChartView('performance')}
            >
              Performance
            </button>
            <button
              className={`chart-tab ${chartView === 'value' ? 'active' : ''}`}
              onClick={() => setChartView('value')}
            >
              Market value
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
