/**
 * PortfolioChart.js
 *
 * Line+area chart comparing CS2 portfolio P&L against the S&P 500 over a selected time range.
 * Both series display percentage change from the first point in the selected range.
 *
 * Palette is tied to the Counter-Strike banner: warm-ink portfolio line, CS-gold area
 * wash + accents, neutral warm-gray benchmark. Colors live as CSS tokens in PortfolioChart.css.
 *
 * @author Ville Laaksoaho
 * Dependencies: recharts, PortfolioChart.css
 */
import { useEffect, useState } from 'react';
import {
  AreaChart, Area, XAxis, YAxis, Tooltip,
  ResponsiveContainer, ReferenceLine, CartesianGrid
} from 'recharts';
import './PortfolioChart.css';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080';

const COLOR_LINE = '#171715';
const COLOR_BENCH = '#9a9890';
const COLOR_GRID = '#e5e3dc';
const COLOR_HAIR = '#cfcdc5';
const COLOR_AXIS = '#74736d';

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
 * @brief Flat custom tooltip showing CS2 P&L % and optionally S&P 500 % for the hovered date.
 *
 * @param {boolean} active Whether the tooltip is currently visible
 * @param {object[]} payload Recharts payload array containing the hovered data point
 * @param {string} label The date string for the hovered x-axis position
 * @returns {JSX.Element|null}
 */
function ChartTooltip({ active, payload, label }) {
  if (!active || !payload || payload.length === 0) return null;
  const fmtPct = v => (v == null ? 'N/A' : `${v > 0 ? '+' : ''}${v}%`);
  const cs2 = payload.find(p => p.dataKey === 'portfolio');
  const sp = payload.find(p => p.dataKey === 'sp500');
  return (
    <div className="chart-tip">
      <div className="chart-tip-date">{formatDate(label)}</div>
      <div className="chart-tip-row">
        {cs2 && <span className="chart-tip-cs2"><span className="dot" />CS2 {fmtPct(cs2.value)}</span>}
        {sp && sp.value != null && <span className="chart-tip-sp">S&amp;P {fmtPct(sp.value)}</span>}
      </div>
    </div>
  );
}

/**
 * @brief Chart fetching portfolio history and S&P 500 data and rendering P&L % over the selected range.
 *        The chart summary shows range-based % and EUR change, with an optional S&P 500 overlay.
 *
 * @returns {JSX.Element} The portfolio P&L chart with range selector and optional S&P 500 overlay
 */
function PortfolioChart() {
  const [data, setData] = useState([]);
  const [eurChange, setEurChange] = useState(null);
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

            const portfolioDates = new Set(
              history.filter(h => h.date >= sp500From).map(h => h.date)
            );
            const allDates = [
              ...new Set([
                ...portfolioDates,
                ...normSp500.filter(p => p.date >= sp500From).map(p => p.date)
              ])
            ].sort();

            let lastSp500Pct = null;
            const merged = [];
            for (const date of allDates) {
              if (sp500Map[date] != null) lastSp500Pct = sp500Map[date];
              if (portfolioDates.has(date)) {
                merged.push({
                  date,
                  sp500: lastSp500Pct,
                  portfolio: portfolioMap[date] ?? null
                });
              }
            }

            const filteredHistory = history.filter(h => h.date >= sp500From);
            if (filteredHistory.length >= 2) {
              const firstEur = filteredHistory[0].totalValueEur;
              const lastEur = filteredHistory[filteredHistory.length - 1].totalValueEur;
              setEurChange(parseFloat((lastEur - firstEur).toFixed(2)));
            } else {
              setEurChange(null);
            }

            setData(merged);
            setLoading(false);
          });
      })
      .catch(() => setLoading(false));
  }, [range]);

  const firstPortfolio = data.find(p => p.portfolio != null)?.portfolio;
  const lastPortfolio = [...data].reverse().find(p => p.portfolio != null)?.portfolio;
  const portfolioRangeChange = firstPortfolio != null && lastPortfolio != null
    ? parseFloat((lastPortfolio - firstPortfolio).toFixed(2))
    : null;

  const firstSp500 = data.find(p => p.sp500 != null)?.sp500;
  const lastSp500 = [...data].reverse().find(p => p.sp500 != null)?.sp500;
  const sp500RangeChange = firstSp500 != null && lastSp500 != null
    ? parseFloat((lastSp500 - firstSp500).toFixed(2))
    : null;

  const fmt = v => v != null ? `${v > 0 ? '+' : ''}${v.toFixed(2)}%` : null;

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

      {!loading && data.length > 0 && (
        <div className="chart-summary">
          {portfolioRangeChange != null && (
            <span className={`chart-summary-stat ${portfolioRangeChange > 0 ? 'positive' : portfolioRangeChange < 0 ? 'negative' : ''}`}>
              CS2 {fmt(portfolioRangeChange)}
            </span>
          )}
          {eurChange != null && (
            <span className={`chart-summary-stat chart-summary-eur ${eurChange > 0 ? 'positive' : eurChange < 0 ? 'negative' : ''}`}>
              {eurChange > 0 ? '+' : ''}€{eurChange.toFixed(2)}
            </span>
          )}
          {showSp500 && sp500RangeChange != null && (
            <span className={`chart-summary-stat sp500 ${sp500RangeChange > 0 ? 'positive' : sp500RangeChange < 0 ? 'negative' : ''}`}>
              S&P 500 {fmt(sp500RangeChange)}
            </span>
          )}
        </div>
      )}

      {loading ? (
        <p className="status-text">Loading chart...</p>
      ) : data.length === 0 ? (
        <p className="status-text">No market data available.</p>
      ) : (
        <ResponsiveContainer width="100%" height={300}>
          <AreaChart data={data} margin={{ top: 8, right: 16, bottom: 0, left: 0 }}>
            <XAxis
              dataKey="date"
              tickFormatter={formatDate}
              tick={{ fill: COLOR_AXIS, fontSize: 12 }}
              axisLine={false}
              tickLine={false}
              minTickGap={60}
            />
            <YAxis
              domain={['dataMin - 2', 'dataMax + 2']}
              tickFormatter={v => `${v > 0 ? '+' : ''}${Math.round(v)}%`}
              tick={{ fill: COLOR_AXIS, fontSize: 12 }}
              axisLine={false}
              tickLine={false}
              width={56}
            />
            <CartesianGrid stroke={COLOR_GRID} vertical={false} />
            <ReferenceLine y={0} stroke={COLOR_HAIR} />
            <Tooltip
              content={<ChartTooltip />}
              cursor={{ stroke: COLOR_HAIR, strokeWidth: 1 }}
            />
            {showSp500 && (
              <Area
                type="monotone"
                dataKey="sp500"
                stroke={COLOR_BENCH}
                strokeWidth={1.5}
                strokeDasharray="4 4"
                fill="none"
                dot={false}
                activeDot={{ r: 3, fill: '#fff', stroke: COLOR_BENCH, strokeWidth: 1.5 }}
                connectNulls
              />
            )}
            <Area
              type="monotone"
              dataKey="portfolio"
              stroke={COLOR_LINE}
              strokeWidth={1.5}
              fill="none"
              baseValue="dataMin"
              dot={false}
              activeDot={{ r: 3, fill: '#f4f3ef', stroke: COLOR_LINE, strokeWidth: 1.5 }}
              connectNulls
            />
          </AreaChart>
        </ResponsiveContainer>
      )}
    </div>
  );
}

export default PortfolioChart;
