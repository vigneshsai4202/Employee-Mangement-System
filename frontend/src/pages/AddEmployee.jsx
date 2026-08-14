import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import EmployeeForm from '../components/EmployeeForm';
import employeeService from '../services/employeeService';

/*
 * useNavigate → redirects to Dashboard after successful creation
 */

function getApiError(err) {
  const status = err.response?.status;
  const fieldErrors = err.response?.data?.fieldErrors;
  if (fieldErrors) return Object.values(fieldErrors).join(', ');
  if (status === 409) return 'An employee with this email already exists.';
  if (status === 400) return 'Invalid data. Please check your input.';
  return 'Something went wrong. Please try again.';
}

function AddEmployee() {
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(data) {
    try {
      setSubmitting(true);
      setError('');
      await employeeService.createEmployee(data);
      navigate('/', { state: { success: 'Employee created successfully.' } });
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="page">
      <h1>Add Employee</h1>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="form-card">
        <EmployeeForm onSubmit={handleSubmit} submitting={submitting} submitLabel="Create Employee" />
      </div>
    </div>
  );
}

export default AddEmployee;
