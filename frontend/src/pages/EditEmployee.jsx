import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import EmployeeForm from '../components/EmployeeForm';
import Loading from '../components/Loading';
import employeeService from '../services/employeeService';

/*
 * useParams   → reads :id from the URL /employees/edit/:id
 * useNavigate → redirects to Dashboard after successful update
 * useEffect   → fetches the employee data when the page loads
 */

function getApiError(err) {
  const status = err.response?.status;
  const fieldErrors = err.response?.data?.fieldErrors;
  if (fieldErrors) return Object.values(fieldErrors).join(', ');
  if (status === 404) return 'Employee not found.';
  if (status === 409) return 'An employee with this email already exists.';
  if (status === 400) return 'Invalid data. Please check your input.';
  return 'Something went wrong. Please try again.';
}

function EditEmployee() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [initialData, setInitialData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    async function fetchEmployee() {
      try {
        const res = await employeeService.getEmployeeById(id);
        const emp = res.data;
        setInitialData({
          firstName: emp.firstName,
          lastName: emp.lastName,
          email: emp.email,
          phoneNumber: emp.phoneNumber || '',
          department: emp.department,
          jobTitle: emp.jobTitle,
          salary: emp.salary,
          hireDate: emp.hireDate,
        });
      } catch (err) {
        setError(getApiError(err));
      } finally {
        setLoading(false);
      }
    }
    fetchEmployee();
  }, [id]);

  async function handleSubmit(data) {
    try {
      setSubmitting(true);
      setError('');
      await employeeService.updateEmployee(id, data);
      navigate('/', { state: { success: 'Employee updated successfully.' } });
    } catch (err) {
      setError(getApiError(err));
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <Loading message="Loading employee..." />;

  return (
    <div className="page">
      <h1>Edit Employee</h1>
      {error && <div className="alert alert-error">{error}</div>}
      <div className="form-card">
        <EmployeeForm
          initialData={initialData}
          onSubmit={handleSubmit}
          submitting={submitting}
          submitLabel="Update Employee"
        />
      </div>
    </div>
  );
}

export default EditEmployee;
