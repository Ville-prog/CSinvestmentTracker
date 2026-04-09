import { useEffect, useState } from 'react';
import {
  LineChart, Line, XAxis, YAxis, Tooltip,
  ResponsiveContainer, ReferenceLine, Legend
} from 'recharts';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080';

function normalize(dataPoints, valueKey) {
  if (!dataPoints || dataPoints.length === 0) return [];
  const base = dataPoints[0][valueKey];
  if (!base) return [];
  return dataPoints.map(p => ({
    date: p.date,
    pct: parseFloat((((p[valueKey] - base) / base) * 100).toFixed(2))
  }));
}

function mergeByDate(portfolio, sp500) {
  const sp500Map = Object.fromEntries(sp500.map(p => [p.date, p.pct]));
  return portfolio.map(p => ({
    date: p.date,
    portfolio: p.pct,
    sp500: sp500Map[p.date] ?? null
  }));
}

function formatDate(dateStr) {
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: '2-digit' });
}

function PortfolioChart({ fromDate }) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!fromDate) return;

    Promise.all([
      fetch(`${API_BASE}/api/portfolio/history`).then(r => r.json()),
      fetch(`${API_BASE}/api/market/sp500?from=${fromDate}`).then(r => r.json())
    ]).then(([history, sp500]) => {
      const normPortfolio = normalize(
        history.map(h => ({ date: h.date, value: h.totalValueUsd })),
        'value'
      );
      const normSp500 = normalize(sp500.map(p => ({ date: p.date, value: p.close })), 'value');
      setData(mergeByDate(normPortfolio, normSp500));
      setLoading(false);
    }).catch(() => setLoading(false));
  }, [fromDate]);

  if (loading) return <p className="status-text">Loading chart...</p>;
  if (data.length < 2) return <p className="status-text">Not enough data yet. Check back after more nightly jobs have run.</p>;

  return (
    <div className="chart-wrapper">
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
            formatter={(value, name) => [`${value > 0 ? '+' : ''}${value}%`, name === 'portfolio' ? 'CS2 Portfolio' : 'S&P 500']}
            labelFormatter={formatDate}
            contentStyle={{ backgroundColor: '#1a1a1a', border: '1px solid #2a2a2a', borderRadius: 6 }}
            labelStyle={{ color: '#888' }}
          />
          <Legend
            formatter={name => name === 'portfolio' ? 'CS2 Portfolio' : 'S&P 500'}
            wrapperStyle={{ fontSize: 13, color: '#888' }}
          />
          <ReferenceLine y={0} stroke="#333" strokeDasharray="3 3" />
          <Line
            type="monotone"
            dataKey="portfolio"
            stroke="#4f9eff"
            strokeWidth={2}
            dot={false}
            activeDot={{ r: 4 }}
          />
          <Line
            type="monotone"
            dataKey="sp500"
            stroke="#f0c040"
            strokeWidth={2}
            dot={false}
            activeDot={{ r: 4 }}
            connectNulls
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}

export default PortfolioChart;
