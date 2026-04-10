/**
 * App.js
 *
 * Root application component with top-level navigation between Dashboard and Inventory pages.
 *
 * @author Ville Laaksoaho
 * Dependencies: Dashboard.js, Inventory.js, App.css
 */
import { useState } from 'react';
import Dashboard from './pages/Dashboard';
import Inventory from './pages/Inventory';
import './App.css';

/**
 * @brief Root component that renders a top-level nav bar and switches between pages.
 *
 * @returns {JSX.Element} The top-level application layout with navigation
 */
function App() {
  const [page, setPage] = useState('dashboard');

  return (
    <div className="App">
      <nav className="top-nav">
        <button
          className={`nav-btn ${page === 'dashboard' ? 'active' : ''}`}
          onClick={() => setPage('dashboard')}
        >
          Dashboard
        </button>
        <button
          className={`nav-btn ${page === 'inventory' ? 'active' : ''}`}
          onClick={() => setPage('inventory')}
        >
          Inventory
        </button>
      </nav>

      {page === 'dashboard' ? <Dashboard /> : <Inventory />}
    </div>
  );
}

export default App;
