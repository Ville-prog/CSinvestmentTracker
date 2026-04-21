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
        <p>A daily job runs at 5 AM UTC (8 AM Finnish time) and fetches the Steam inventory, upserting any newly discovered items into the database. It then collects the current Steam Market price for every tracked item in the database, not only the items returned by today's Steam response, so transient gaps or truncations in the Steam API don't distort the portfolio value.</p>
        <p>Each item carries a last-seen timestamp that is advanced whenever it appears in a sane Steam response. Items missing for more than 7 days are considered traded away, and a sanity gate protects against one bad Steam day silently ageing out the whole inventory.</p>
        <p>Steam's Market API is rate limited with no official pricing endpoint, so the job fetches one price every 4 seconds. This is the compliant approach. Many third-party sites bypass this by running networks of Steam bot accounts, which violates Steam's Terms of Service.</p>
      </div>

      <div className="about-section">
        <h2 className="about-heading">P&L calculation</h2>
        <p>Portfolio profit/loss is calculated relative to cost basis:</p>
        <pre className="about-formula">P&L % = (current value − cost basis) / cost basis × 100</pre>
        <p>When new units of an item are added to the tracked inventory, they enter the cost basis at today's market price. When units are sold, the cost basis is scaled proportionally. Items seen for the first time have their cost basis set to today's market price automatically. This means the chart only moves when prices change; adding items does not count as a gain. It mirrors how real investment portfolio trackers work.</p>
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
          <li><strong>Historical data:</strong> The app only shows data from when tracking began. There is no way to backfill historical portfolio value before the first nightly run.</li>
          <li><strong>Storage Containers:</strong> Items stored inside Steam Storage Containers are not visible to the API and cannot be tracked.</li>
          <li><strong>Trade cooldowns:</strong> Newly traded items have a 7-day market cooldown and are skipped until they become marketable.</li>
          <li><strong>Single inventory:</strong> The app currently tracks one hardcoded Steam inventory.</li>
          <li><strong>Trade-out detection delay:</strong> Since items missing from a single Steam response are still priced from the DB, truly traded-away items are only recognised after 7 consecutive days outside the inventory response.</li>
        </ul>
      </div>
    </div>
  );
}

export default About;
