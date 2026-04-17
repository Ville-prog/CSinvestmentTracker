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
  AreaChart, Area, Line, XAxis, YAxis, Tooltip,
  ResponsiveContainer, ReferenceLine, Legend, CartesianGrid
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
  if (base === 0) return [];
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
  const [range, setRange] = useState('1W');
  const [showSp500, setShowSp500] = useState(false);

  useEffect(() => {
    const selectedRange = RANGES.find(r => r.label === range);
    const rangeFrom = fromDate(selectedRange);

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

            const allDates = history
              .filter(h => h.date >= sp500From)
              .map(h => h.date)
              .sort();

            let lastSp500Pct = null;
            const merged = allDates.map(date => {
              if (sp500Map[date] != null) lastSp500Pct = sp500Map[date];
              return {
                date,
                sp500: lastSp500Pct,
                portfolio: portfolioMap[date] ?? null
              };
            });

            setData(merged);
            setLoading(false);
          });
      })
      .catch(() => setLoading(false));
  }, [range]);

  return (
    <div className="chart-wrapper">
      <div className="chart-controls">
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
        <button
          className={`range-btn sp500-btn ${showSp500 ? 'active' : ''}`}
          onClick={() => setShowSp500(v => !v)}
        >
          S&P 500
        </button>
      </div>

      {!loading && data.length > 0 && (() => {
        const last = data[data.length - 1];
        const portfolioVal = last?.portfolio;
        const lastSp500 = [...data].reverse().find(p => p.sp500 != null);
        const sp500Val = lastSp500?.sp500;
        const fmt = v => v != null ? `${v > 0 ? '+' : ''}${v.toFixed(2)}%` : null;
        return (
          <div className="chart-summary">
            {portfolioVal != null && (
              <span className={`chart-summary-stat ${portfolioVal > 0 ? 'positive' : portfolioVal < 0 ? 'negative' : ''}`}>
                CS2 {fmt(portfolioVal)}
              </span>
            )}
            {showSp500 && sp500Val != null && (
              <span className={`chart-summary-stat ${sp500Val > 0 ? 'positive' : sp500Val < 0 ? 'negative' : ''}`}>
                S&P 500 {fmt(sp500Val)}
              </span>
            )}
          </div>
        );
      })()}

      {loading ? (
        <p className="status-text">Loading chart...</p>
      ) : data.length === 0 ? (
        <p className="status-text">No market data available.</p>
      ) : (
        <ResponsiveContainer width="100%" height={300}>
          <AreaChart data={data} margin={{ top: 8, right: 16, bottom: 0, left: 0 }}>
            <defs>
              <linearGradient id="portfolioGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#4f9eff" stopOpacity={0.25} />
                <stop offset="95%" stopColor="#4f9eff" stopOpacity={0} />
              </linearGradient>
            </defs>
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
            <CartesianGrid stroke="#222" strokeDasharray="3 3" vertical={false} />
            <ReferenceLine y={0} stroke="#333" strokeDasharray="3 3" />
            {showSp500 && <Line type="monotone" dataKey="sp500" stroke="#f0c040" strokeWidth={2} dot={false} activeDot={{ r: 4 }} connectNulls />}
            <Area type="monotone" dataKey="portfolio" stroke="#4f9eff" strokeWidth={2} fill="url(#portfolioGradient)" dot={false} activeDot={{ r: 4 }} connectNulls />
          </AreaChart>
        </ResponsiveContainer>
      )}
    </div>
  );
}

export default PortfolioChart;
