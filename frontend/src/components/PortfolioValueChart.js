/**
 * PortfolioValueChart.js
 *
 * Line+area chart showing raw CS2 portfolio value in EUR over a selected time range.
 * Unlike the P&L chart, this shows absolute value including the effect of adding new items.
 *
 * Shares the banner palette with PortfolioChart: warm-ink line, CS-gold area wash + accents.
 *
 * @author Ville Laaksoaho
 * Dependencies: recharts, PortfolioChart.css
 */
import { useEffect, useState } from 'react';
import {
  AreaChart, Area, XAxis, YAxis, Tooltip,
  ResponsiveContainer, CartesianGrid
} from 'recharts';
import './PortfolioChart.css';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const COLOR_LINE = '#231f1c';   // --cs-ink
const COLOR_GOLD = '#d8c715';   // --cs-gold
const COLOR_GRID = '#f1f3f6';   // --chart-grid
const COLOR_AXIS = '#9aa0ab';   // --chart-axis

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
 * @returns {string} Formatted date label (e.g. "12.04.2026")
 */
function formatDate(dateStr) {
  const [year, month, day] = dateStr.split('-');
  return `${day}.${month}.${year}`;
}

/**
 * @brief Flat custom tooltip — square corners, gold dot on the EUR value.
 *
 * @param {boolean} active Whether the tooltip is currently visible
 * @param {object[]} payload Recharts payload array containing the hovered data point
 * @param {string} label The date string for the hovered x-axis position
 * @returns {JSX.Element|null}
 */
function ValueTooltip({ active, payload, label }) {
  if (!active || !payload || payload.length === 0) return null;
  const v = payload[0]?.value;
  return (
    <div className="chart-tip">
      <div className="chart-tip-date">{formatDate(label)}</div>
      <div className="chart-tip-row">
        <span className="chart-tip-cs2">
          <span className="dot" />€{v != null ? v.toFixed(2) : 'N/A'}
        </span>
      </div>
    </div>
  );
}

/**
 * @brief Chart fetching portfolio history and rendering total EUR value over the selected range.
 *        The chart summary shows the absolute EUR change from the first to the last point in range.
 *
 * @returns {JSX.Element} The portfolio total value chart with range selector buttons
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

      {!loading && data.length > 1 && (() => {
        const first = data[0].value;
        const last = data[data.length - 1].value;
        const change = last - first;
        const formatted = `${change > 0 ? '+' : ''}€${change.toFixed(2)}`;
        return (
          <div className="chart-summary">
            <span className={`chart-summary-stat ${change > 0 ? 'positive' : change < 0 ? 'negative' : ''}`}>
              {formatted}
            </span>
          </div>
        );
      })()}

      {loading ? (
        <p className="status-text">Loading chart...</p>
      ) : data.length === 0 ? (
        <p className="status-text">No portfolio data available.</p>
      ) : (
        <ResponsiveContainer width="100%" height={300}>
          <AreaChart data={data} margin={{ top: 8, right: 16, bottom: 0, left: 0 }}>
            <defs>
              <linearGradient id="valueGoldWash" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor={COLOR_GOLD} stopOpacity={0.28} />
                <stop offset="100%" stopColor={COLOR_GOLD} stopOpacity={0} />
              </linearGradient>
            </defs>
            <XAxis
              dataKey="date"
              tickFormatter={formatDate}
              tick={{ fill: COLOR_AXIS, fontSize: 12 }}
              axisLine={false}
              tickLine={false}
              minTickGap={60}
            />
            <YAxis
              tickFormatter={v => `€${v.toLocaleString('fi-FI', { maximumFractionDigits: 0 })}`}
              tick={{ fill: COLOR_AXIS, fontSize: 12 }}
              axisLine={false}
              tickLine={false}
              width={72}
            />
            <CartesianGrid stroke={COLOR_GRID} vertical={false} />
            <Tooltip
              content={<ValueTooltip />}
              cursor={{ stroke: COLOR_GOLD, strokeWidth: 1 }}
            />
            <Area
              type="monotone"
              dataKey="value"
              stroke={COLOR_LINE}
              strokeWidth={1.9}
              fill="url(#valueGoldWash)"
              dot={false}
              activeDot={{ r: 4, fill: '#fff', stroke: COLOR_GOLD, strokeWidth: 2.2 }}
            />
          </AreaChart>
        </ResponsiveContainer>
      )}
    </div>
  );
}

export default PortfolioValueChart;
