/**
 * Dashboard.js
 *
 * Main dashboard page displaying the latest portfolio value snapshot and the portfolio chart.
 * Fetches the most recent snapshot from the backend on mount and passes it to child components.
 *
 * @author Ville Laaksoaho
 * Dependencies: PortfolioChart.js, Dashboard.css
 */
import { useEffect, useState } from 'react';
import PortfolioChart from '../components/PortfolioChart';
import './Dashboard.css';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080';

/**
 * @brief Fetches and displays the latest portfolio snapshot including total value, item count, and chart.
 *
 * @returns {JSX.Element} The dashboard page with stat cards and portfolio chart
 */
function Dashboard() {
  const [snapshot, setSnapshot] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch(`${API_BASE}/api/portfolio/latest`)
      .then(res => {
        if (!res.ok) throw new Error('Failed to fetch portfolio data');
        return res.json();
      })
      .then(data => {
        setSnapshot(data);
        setLoading(false);
      })
      .catch(err => {
        setError(err.message);
        setLoading(false);
      });
  }, []);

  if (loading) return <p className="status-text">Loading...</p>;
  if (error) return <p className="status-text error">{error}</p>;
  if (!snapshot) return <p className="status-text">No data yet. The nightly job has not run yet.</p>;

  return (
    <div className="dashboard">
      <h1 className="page-title">CS2 Portfolio</h1>
      <p className="snapshot-date">Last updated: {snapshot.date.split('-').reverse().join('.')}</p>

      <div className="stat-cards">
        <div className="stat-card">
          <span className="stat-label">Total Value</span>
          <span className="stat-value">€{snapshot.totalValueEur.toFixed(2)}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Items Tracked</span>
          <span className="stat-value">{snapshot.itemCount}</span>
        </div>
      </div>

      <PortfolioChart />
    </div>
  );
}

export default Dashboard;
