/**
 * index.js
 *
 * React application entry point. Mounts the root App component into the DOM.
 *
 * @author Ville Laaksoaho
 * Dependencies: App.js
 */
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
