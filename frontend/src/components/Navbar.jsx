import { Link, useLocation } from 'react-router-dom';

function Navbar() {
  const location = useLocation();

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/">Employee Management System</Link>
      </div>
      <div className="navbar-links">
        <Link to="/" className={location.pathname === '/' ? 'active' : ''}>
          Dashboard
        </Link>
        <Link
          to="/employees/new"
          className={location.pathname === '/employees/new' ? 'active' : ''}
        >
          + Add Employee
        </Link>
      </div>
    </nav>
  );
}

export default Navbar;
