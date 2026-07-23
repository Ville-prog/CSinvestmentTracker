/**
 * App.js
 *
 * Root application component. Renders the top navigation bar and switches between pages.
 *
 * @author Ville Laaksoaho
 * Dependencies: Dashboard.js, About.js, App.css
 */
import { useState } from 'react';
import Dashboard from './pages/Dashboard';
import About from './pages/About';
import './App.css';

/**
 * @brief Top-level component managing page state and rendering the nav bar and active page.
 *
 * @returns {JSX.Element} The full application shell
 */
function App() {
  const [page, setPage] = useState('dashboard');

  return (
    <div className="App">
      <nav className="top-nav">
        <div className="nav-brand">
          <img src="/cs_logo_ink.png" alt="CS" className="nav-glyph" />
          <span className="nav-wordmark">Investment tracker</span>
        </div>
        <div className="nav-links">
          <button
            className={`nav-btn ${page === 'dashboard' ? 'active' : ''}`}
            onClick={() => setPage('dashboard')}
          >
            Portfolio
          </button>
          <button
            className={`nav-btn ${page === 'about' ? 'active' : ''}`}
            onClick={() => setPage('about')}
          >
            About
          </button>
        </div>
      </nav>

      {page === 'dashboard' && <Dashboard />}
      {page === 'about' && <About />}
    </div>
  );
}

export default App;
