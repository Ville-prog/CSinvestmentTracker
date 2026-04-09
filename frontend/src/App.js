/**
 * App.js
 *
 * Root application component that renders the top-level page layout.
 * Currently renders the Dashboard page as the only route.
 *
 * @author Ville Laaksoaho
 */
import Dashboard from './pages/Dashboard';
import './App.css';

/**
 * @brief Root component that wraps the application in a styled container and renders the Dashboard.
 *
 * @returns {JSX.Element} The top-level application layout
 */
function App() {
  return (
    <div className="App">
      <Dashboard />
    </div>
  );
}

export default App;
