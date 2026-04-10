/**
 * App.js
 *
 * Root application component with top-level navigation between Dashboard, Inventory, and About pages.
 *
 * @author Ville Laaksoaho
 * Dependencies: Dashboard.js, Inventory.js, About.js, App.css
 */
import { useState } from 'react';
import Dashboard from './pages/Dashboard';
import Inventory from './pages/Inventory';
import About from './pages/About';
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
        <button
          className={`nav-btn ${page === 'about' ? 'active' : ''}`}
          onClick={() => setPage('about')}
        >
          About
        </button>
      </nav>

      {page === 'dashboard' && <Dashboard />}
      {page === 'inventory' && <Inventory />}
      {page === 'about' && <About />}
    </div>
  );
}

export default App;
