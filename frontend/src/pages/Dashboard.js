import { useEffect, useState } from 'react';
import PortfolioChart from '../components/PortfolioChart';
import './Dashboard.css';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080';

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
      <p className="snapshot-date">Last updated: {snapshot.date}</p>

      <div className="stat-cards">
        <div className="stat-card">
          <span className="stat-label">Total Value</span>
          <span className="stat-value">${snapshot.totalValueUsd.toFixed(2)}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Items Tracked</span>
          <span className="stat-value">{snapshot.itemCount}</span>
        </div>
      </div>

      <PortfolioChart fromDate={snapshot.date} />
    </div>
  );
}

export default Dashboard;
