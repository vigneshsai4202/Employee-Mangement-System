import { useNavigate } from 'react-router-dom';

function EmployeeTable({ employees, onView, onDelete, deletingId }) {
  const navigate = useNavigate();

  if (employees.length === 0) {
    return <p className="empty-message">No employees found.</p>;
  }

  return (
    <div className="table-wrapper">
      <table className="employee-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Department</th>
            <th>Job Title</th>
            <th>Salary</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {employees.map((emp) => (
            <tr key={emp.id}>
              <td>{emp.id}</td>
              <td>{emp.firstName} {emp.lastName}</td>
              <td>{emp.email}</td>
              <td><span className="badge">{emp.department}</span></td>
              <td>{emp.jobTitle}</td>
              <td>${Number(emp.salary).toLocaleString()}</td>
              <td className="actions">
                <button className="btn btn-sm btn-info" onClick={() => onView(emp)}>View</button>
                <button className="btn btn-sm btn-warning" onClick={() => navigate(`/employees/edit/${emp.id}`)}>Edit</button>
                <button
                  className="btn btn-sm btn-danger"
                  onClick={() => onDelete(emp.id)}
                  disabled={deletingId === emp.id}
                >
                  {deletingId === emp.id ? 'Deleting...' : 'Delete'}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default EmployeeTable;
