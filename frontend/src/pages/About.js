/**
 * About.js
 *
 * Info/About page summarising the project purpose, methodology, and tech stack.
 *
 * @author Ville Laaksoaho
 * Dependencies: Dashboard.css, About.css
 */
import './Dashboard.css';
import './About.css';

/**
 * @brief Renders the About page with project description, methodology, and stack information.
 *
 * @returns {JSX.Element} The About page
 */
function About() {
  return (
    <div className="dashboard">
      <h1 className="page-title">About</h1>

      <div className="about-section">
        <h2 className="about-heading">What is this?</h2>
        <p>A personal CS2 skin investment tracker that monitors portfolio value over time and compares it against the S&P 500.</p>
      </div>

      <div className="about-section">
        <h2 className="about-heading">What is the CS2 skin market?</h2>
        <p>CS2 (Counter-Strike 2) is a free-to-play shooter with an in-game economy where players own cosmetic weapon skins. Unlike most in-game items, CS2 skins have real monetary value and can be freely traded, bought, and sold.</p>
        <p>The CS2 skin market has been active since 2013 and has grown into a multi-billion dollar economy. This tracker measures total portfolio value rather than betting on single items, treating the inventory more like an index than individual stock picks.</p>
      </div>

      <div className="about-section">
        <h2 className="about-heading">How it works</h2>
        <p>A nightly job runs at 11 PM UTC, fetches the Steam inventory, and collects current Steam Market prices for each item. Prices are saved to the database daily, building a historical record over time.</p>
        <p>Steam's Market API is rate limited with no official pricing endpoint, so the job fetches one price every 129 seconds. This is the compliant approach. Many third-party sites bypass this by running networks of Steam bot accounts, which violates Steam's Terms of Service.</p>
      </div>

      <div className="about-section">
        <h2 className="about-heading">P&L calculation</h2>
        <p>Portfolio profit/loss is calculated relative to cost basis:</p>
        <pre className="about-formula">P&L % = (current value − cost basis) / cost basis × 100</pre>
        <p>Each item's cost basis is set to its market price on the day it first becomes available for sale. This means the chart only moves when prices change. Adding new items does not count as a gain. This mirrors how real investment portfolio trackers work.</p>
      </div>

      <div className="about-section">
        <h2 className="about-heading">Stack</h2>
        <ul className="about-list">
          <li><strong>Backend:</strong> Java 21 / Spring Boot 3, deployed on Railway via Docker</li>
          <li><strong>Database:</strong> PostgreSQL, managed by Railway</li>
          <li><strong>Frontend:</strong> React with Recharts, deployed on Vercel</li>
          <li><strong>External APIs:</strong> Steam Community Market (prices), Yahoo Finance (S&P 500 history)</li>
        </ul>
      </div>

      <div className="about-section">
        <h2 className="about-heading">Limitations</h2>
        <ul className="about-list">
          <li><strong>Storage Containers:</strong> Items stored inside Steam Storage Containers are not visible to the API and cannot be tracked.</li>
          <li><strong>Trade cooldowns:</strong> Newly traded items have a 7-day market cooldown and are skipped until they become marketable.</li>
          <li><strong>Single inventory:</strong> The app currently tracks one hardcoded Steam inventory.</li>
        </ul>
      </div>
    </div>
  );
}

export default About;
