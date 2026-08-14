import { useState, useEffect } from 'react';
import employeeService from '../services/employeeService';
import EmployeeTable from '../components/EmployeeTable';
import SearchBar from '../components/SearchBar';
import EmployeeModal from '../components/EmployeeModal';
import Loading from '../components/Loading';

/*
 * useState  → manages employees list, loading, error, success, search state, modal
 * useEffect → fetches employees on mount and whenever search/filter changes
 */

function getApiError(err) {
  const status = err.response?.status;
  if (status === 404) return 'Employee not found.';
  if (status === 409) return 'Email already exists.';
  if (status === 400) return 'Invalid request. Please check your input.';
  return 'Something went wrong. Please try again.';
}

function Dashboard() {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [keyword, setKeyword] = useState('');
  const [department, setDepartment] = useState('');
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [deletingId, setDeletingId] = useState(null);

  useEffect(() => {
    fetchEmployees();
  }, []);

  // Debounce keyword search — waits 400ms after user stops typing
  useEffect(() => {
    if (!keyword) return;
    const timer = setTimeout(() => handleSearch(keyword), 400);
    return () => clearTimeout(timer);
  }, [keyword]);

  async function fetchEmployees() {
    try {
      setLoading(true);
      setError('');
      const res = await employeeService.getEmployees();
      setEmployees(res.data);
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setLoading(false);
    }
  }

  async function handleSearch(kw) {
    try {
      setLoading(true);
      setError('');
      const res = await employeeService.searchEmployees(kw);
      setEmployees(res.data);
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setLoading(false);
    }
  }

  async function handleDepartmentChange(dept) {
    setDepartment(dept);
    setKeyword('');
    try {
      setLoading(true);
      setError('');
      const res = dept
        ? await employeeService.getEmployeesByDepartment(dept)
        : await employeeService.getEmployees();
      setEmployees(res.data);
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id) {
    if (!window.confirm('Are you sure you want to delete this employee?')) return;
    try {
      setDeletingId(id);
      await employeeService.deleteEmployee(id);
      setEmployees((prev) => prev.filter((e) => e.id !== id));
      setSuccess('Employee deleted successfully.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setDeletingId(null);
    }
  }

  function handleClear() {
    setKeyword('');
    setDepartment('');
    fetchEmployees();
  }

  const departments = [...new Set(employees.map((e) => e.department))];

  return (
    <div className="page">
      <h1>Dashboard</h1>

      <div className="stats-row">
        <div className="stat-card">
          <span className="stat-number">{employees.length}</span>
          <span className="stat-label">Total Employees</span>
        </div>
        <div className="stat-card">
          <span className="stat-number">{departments.length}</span>
          <span className="stat-label">Departments</span>
        </div>
      </div>

      {success && <div className="alert alert-success">{success}</div>}
      {error && <div className="alert alert-error">{error}</div>}

      <SearchBar
        keyword={keyword}
        department={department}
        onKeywordChange={setKeyword}
        onDepartmentChange={handleDepartmentChange}
        onClear={handleClear}
      />

      {loading ? (
        <Loading message="Loading employees..." />
      ) : (
        <EmployeeTable
          employees={employees}
          onView={setSelectedEmployee}
          onDelete={handleDelete}
          deletingId={deletingId}
        />
      )}

      <EmployeeModal employee={selectedEmployee} onClose={() => setSelectedEmployee(null)} />
    </div>
  );
}

export default Dashboard;
