import { useState } from 'react';
import Dashboard from './pages/Dashboard';
import About from './pages/About';
import './App.css';

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
