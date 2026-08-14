import { useState, useEffect } from 'react';

/*
 * useState  → tracks form field values and validation errors
 * useEffect → when editing, populates form with existing employee data
 */

const DEPARTMENTS = ['IT', 'HR', 'Finance', 'Sales', 'Marketing', 'Operations'];

const EMPTY_FORM = {
  firstName: '', lastName: '', email: '', phoneNumber: '',
  department: '', jobTitle: '', salary: '', hireDate: '',
};

function validate(data) {
  const errors = {};
  if (!data.firstName.trim()) errors.firstName = 'First name is required';
  if (!data.lastName.trim()) errors.lastName = 'Last name is required';
  if (!data.email.trim()) errors.email = 'Email is required';
  else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) errors.email = 'Enter a valid email address';
  if (data.phoneNumber && !/^\d{10}$/.test(data.phoneNumber)) errors.phoneNumber = 'Phone must be 10 digits';
  if (!data.department) errors.department = 'Department is required';
  if (!data.jobTitle.trim()) errors.jobTitle = 'Job title is required';
  if (!data.salary) errors.salary = 'Salary is required';
  else if (isNaN(data.salary) || Number(data.salary) <= 0) errors.salary = 'Salary must be a positive number';
  if (!data.hireDate) errors.hireDate = 'Hire date is required';
  return errors;
}

function EmployeeForm({ initialData, onSubmit, submitting, submitLabel }) {
  const [form, setForm] = useState(EMPTY_FORM);
  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (initialData) setForm(initialData);
  }, [initialData]);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: '' }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    const validationErrors = validate(form);
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }
    onSubmit({ ...form, salary: parseFloat(form.salary) });
  }

  function field(label, name, type = 'text', extra = {}) {
    return (
      <div className="form-group">
        <label>{label}</label>
        <input
          type={type}
          name={name}
          value={form[name]}
          onChange={handleChange}
          className={errors[name] ? 'input-error' : ''}
          {...extra}
        />
        {errors[name] && <span className="error-text">{errors[name]}</span>}
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="employee-form">
      <div className="form-row">
        {field('First Name', 'firstName')}
        {field('Last Name', 'lastName')}
      </div>
      <div className="form-row">
        {field('Email', 'email', 'email')}
        {field('Phone Number', 'phoneNumber', 'tel', { placeholder: '10 digits' })}
      </div>
      <div className="form-row">
        <div className="form-group">
          <label>Department</label>
          <select
            name="department"
            value={form.department}
            onChange={handleChange}
            className={errors.department ? 'input-error' : ''}
          >
            <option value="">Select Department</option>
            {DEPARTMENTS.map((d) => <option key={d} value={d}>{d}</option>)}
          </select>
          {errors.department && <span className="error-text">{errors.department}</span>}
        </div>
        {field('Job Title', 'jobTitle')}
      </div>
      <div className="form-row">
        {field('Salary', 'salary', 'number', { min: '0', step: '0.01' })}
        {field('Hire Date', 'hireDate', 'date')}
      </div>
      <button type="submit" className="btn btn-primary" disabled={submitting}>
        {submitting ? 'Saving...' : submitLabel}
      </button>
    </form>
  );
}

export default EmployeeForm;
