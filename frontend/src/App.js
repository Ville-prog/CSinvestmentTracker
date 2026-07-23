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
        <button
          className={`nav-btn ${page === 'dashboard' ? 'active' : ''}`}
          onClick={() => setPage('dashboard')}
        >
          Dashboard
        </button>
        <button
          className={`nav-btn ${page === 'about' ? 'active' : ''}`}
          onClick={() => setPage('about')}
        >
          About
        </button>
      </nav>

      {page === 'dashboard' && <Dashboard />}
      {page === 'about' && <About />}
    </div>
  );
}

export default App;
