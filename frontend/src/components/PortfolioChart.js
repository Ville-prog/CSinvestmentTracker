/**
 * PortfolioChart.js
 *
 * Line chart component comparing CS2 portfolio value against the S&P 500 index over time.
 * Both series are normalized to percentage change from the first data point for comparison.
 * Supports time range selection (Max, 1Y, 6M, 3M, 1M).
 *
 * @author Ville Laaksoaho
 * Dependencies: recharts, PortfolioChart.css
 */
import { useEffect, useState } from 'react';
import {
  LineChart, Line, XAxis, YAxis, Tooltip,
  ResponsiveContainer, ReferenceLine, Legend
} from 'recharts';
import './PortfolioChart.css';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const RANGES = [
  { label: 'Max', months: null },
  { label: '1Y', months: 12 },
  { label: '6M', months: 6 },
  { label: '3M', months: 3 },
  { label: '1M', months: 1 },
];

/**
 * @brief Returns an ISO date string for the start of a range given a number of months back.
 *        Returns the CS:GO skin market launch date when months is null (Max range).
 *
 * @param {number|null} months Number of months to go back, or null for Max
 * @returns {string} ISO date string (YYYY-MM-DD)
 */
function fromDate(months) {
  if (months === null) return '2013-08-13';
  const d = new Date();
  d.setMonth(d.getMonth() - months);
  return d.toISOString().split('T')[0];
}

/**
 * @brief Normalizes a list of data points to percentage change from the first value.
 *
 * @param {{ date: string, [valueKey]: number }[]} dataPoints Array of data point objects
 * @param {string} valueKey The key to normalize on
 * @returns {{ date: string, pct: number }[]} Normalized data points with percentage change
 */
function normalize(dataPoints, valueKey) {
  const valid = dataPoints.filter(p => p[valueKey] != null);
  if (valid.length === 0) return [];
  const base = valid[0][valueKey];
  return valid.map(p => ({
    date: p.date,
    pct: parseFloat((((p[valueKey] - base) / base) * 100).toFixed(2))
  }));
}

/**
 * @brief Formats an ISO date string into a short human-readable label.
 *
 * @param {string} dateStr ISO date string (YYYY-MM-DD)
 * @returns {string} Formatted date label (e.g. "9 Apr '26")
 */
function formatDate(dateStr) {
  const [year, month, day] = dateStr.split('-');
  return `${day}.${month}.${year}`;
}

/**
 * @brief Chart component that fetches portfolio history and S&P 500 data, then renders a
 *        normalized percentage change line chart with time range selector buttons.
 *
 * @returns {JSX.Element} The portfolio comparison chart with range buttons
 */
function PortfolioChart() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [range, setRange] = useState('1Y');

  useEffect(() => {
    const selectedRange = RANGES.find(r => r.label === range);
    const rangeFrom = fromDate(selectedRange.months);

    setLoading(true);

    fetch(`${API_BASE}/api/portfolio/history`)
      .then(r => r.json())
      .then(history => {
        if (!history || history.length === 0) {
          setData([]);
          setLoading(false);
          return;
        }

        const firstSnapshotDate = history[0].date;
        const sp500From = firstSnapshotDate > rangeFrom ? firstSnapshotDate : rangeFrom;

        fetch(`${API_BASE}/api/market/sp500?from=${firstSnapshotDate}`)
          .then(r => r.json())
          .then(sp500 => {
            const normSp500 = normalize(sp500.map(p => ({ date: p.date, value: p.close })), 'value');
            const sp500Map = Object.fromEntries(normSp500.map(p => [p.date, p.pct]));

            const portfolioMap = Object.fromEntries(
              history.map(h => {
                const pnl = h.totalCostBasisEur > 0
                  ? parseFloat((((h.totalValueEur - h.totalCostBasisEur) / h.totalCostBasisEur) * 100).toFixed(2))
                  : 0;
                return [h.date, pnl];
              })
            );

            const merged = normSp500
              .filter(p => p.date >= sp500From)
              .map(p => ({
                date: p.date,
                sp500: sp500Map[p.date] ?? null,
                portfolio: portfolioMap[p.date] ?? null
              }));

            setData(merged);
            setLoading(false);
          });
      })
      .catch(() => setLoading(false));
  }, [range]);

  return (
    <div className="chart-wrapper">
      <div className="chart-range-buttons">
        {RANGES.map(r => (
          <button
            key={r.label}
            className={`range-btn ${range === r.label ? 'active' : ''}`}
            onClick={() => setRange(r.label)}
          >
            {r.label}
          </button>
        ))}
      </div>

      {loading ? (
        <p className="status-text">Loading chart...</p>
      ) : data.length === 0 ? (
        <p className="status-text">No market data available.</p>
      ) : (
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={data} margin={{ top: 8, right: 16, bottom: 0, left: 0 }}>
            <XAxis
              dataKey="date"
              tickFormatter={formatDate}
              tick={{ fill: '#888', fontSize: 12 }}
              axisLine={false}
              tickLine={false}
              minTickGap={60}
            />
            <YAxis
              tickFormatter={v => `${v > 0 ? '+' : ''}${v}%`}
              tick={{ fill: '#888', fontSize: 12 }}
              axisLine={false}
              tickLine={false}
              width={56}
            />
            <Tooltip
              formatter={(value, name) => [
                value != null ? `${value > 0 ? '+' : ''}${value}%` : 'N/A',
                name === 'portfolio' ? 'CS2 Portfolio P&L' : 'S&P 500'
              ]}
              labelFormatter={formatDate}
              contentStyle={{ backgroundColor: '#1a1a1a', border: '1px solid #2a2a2a', borderRadius: 6 }}
              labelStyle={{ color: '#888' }}
            />
            <Legend
              formatter={name => name === 'portfolio' ? 'CS2 Portfolio P&L' : 'S&P 500'}
              wrapperStyle={{ fontSize: 13, color: '#888' }}
            />
            <ReferenceLine y={0} stroke="#333" strokeDasharray="3 3" />
            <Line type="monotone" dataKey="sp500" stroke="#f0c040" strokeWidth={2} dot={false} activeDot={{ r: 4 }} connectNulls />
            <Line type="monotone" dataKey="portfolio" stroke="#4f9eff" strokeWidth={2} dot={false} activeDot={{ r: 4 }} connectNulls />
          </LineChart>
        </ResponsiveContainer>
      )}
    </div>
  );
}

export default PortfolioChart;
