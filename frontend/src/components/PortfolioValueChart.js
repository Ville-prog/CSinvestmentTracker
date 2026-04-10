/**
 * PortfolioValueChart.js
 *
 * Line chart showing raw CS2 portfolio value in EUR over time.
 * Unlike the comparison chart, this shows absolute value including the effect of adding new items.
 * Supports time range selection (Max, 1Y, 6M, 3M, 1M, 1W).
 *
 * @author Ville Laaksoaho
 * Dependencies: recharts, PortfolioChart.css
 */
import { useEffect, useState } from 'react';
import {
  LineChart, Line, XAxis, YAxis, Tooltip,
  ResponsiveContainer, CartesianGrid
} from 'recharts';
import './PortfolioChart.css';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const RANGES = [
  { label: 'Max', months: null },
  { label: '1Y', months: 12 },
  { label: '6M', months: 6 },
  { label: '3M', months: 3 },
  { label: '1M', months: 1 },
  { label: '1W', weeks: 1 },
];

/**
 * @brief Returns an ISO date string for the start of the given range.
 *        Supports month-based and week-based ranges. Returns the CS:GO skin market launch date for Max.
 *
 * @param {{ months: number|null, weeks?: number }} range Range object from the RANGES array
 * @returns {string} ISO date string (YYYY-MM-DD)
 */
function fromDate(range) {
  if (range.months === null) return '2013-08-13';
  const d = new Date();
  if (range.weeks) {
    d.setDate(d.getDate() - range.weeks * 7);
  } else {
    d.setMonth(d.getMonth() - range.months);
  }
  return d.toISOString().split('T')[0];
}

/**
 * @brief Formats an ISO date string into DD.MM.YYYY format.
 *
 * @param {string} dateStr ISO date string (YYYY-MM-DD)
 * @returns {string} Formatted date label (e.g. "09.04.2026")
 */
function formatDate(dateStr) {
  const [year, month, day] = dateStr.split('-');
  return `${day}.${month}.${year}`;
}

/**
 * @brief Chart component that fetches portfolio history and renders total EUR value over time.
 *        Includes time range selector buttons. Does not normalize data — shows absolute values.
 *
 * @returns {JSX.Element} The portfolio total value chart with range buttons
 */
function PortfolioValueChart() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [range, setRange] = useState('1W');

  useEffect(() => {
    const selectedRange = RANGES.find(r => r.label === range);
    const from = fromDate(selectedRange);

    setLoading(true);

    fetch(`${API_BASE}/api/portfolio/history`)
      .then(r => r.json())
      .then(history => {
        const filtered = history
          .filter(h => h.date >= from)
          .map(h => ({ date: h.date, value: h.totalValueEur }));
        setData(filtered);
        setLoading(false);
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
        <p className="status-text">No portfolio data available.</p>
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
              tickFormatter={v => `€${v.toLocaleString('fi-FI', { maximumFractionDigits: 0 })}`}
              tick={{ fill: '#888', fontSize: 12 }}
              axisLine={false}
              tickLine={false}
              width={72}
            />
            <Tooltip
              formatter={value => [`€${value.toFixed(2)}`, 'Portfolio Value']}
              labelFormatter={formatDate}
              contentStyle={{ backgroundColor: '#1a1a1a', border: '1px solid #2a2a2a', borderRadius: 6 }}
              labelStyle={{ color: '#888' }}
            />
            <CartesianGrid stroke="#222" strokeDasharray="3 3" vertical={false} />
            <Line type="monotone" dataKey="value" stroke="#4f9eff" strokeWidth={2} dot={false} activeDot={{ r: 4 }} />
          </LineChart>
        </ResponsiveContainer>
      )}
    </div>
  );
}

export default PortfolioValueChart;
