function EmployeeModal({ employee, onClose }) {
  if (!employee) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{employee.firstName} {employee.lastName}</h2>
          <button className="modal-close" onClick={onClose}>✕</button>
        </div>
        <div className="modal-body">
          <div className="detail-grid">
            <div className="detail-item"><span className="label">ID</span><span>{employee.id}</span></div>
            <div className="detail-item"><span className="label">Email</span><span>{employee.email}</span></div>
            <div className="detail-item"><span className="label">Phone</span><span>{employee.phoneNumber || '—'}</span></div>
            <div className="detail-item"><span className="label">Department</span><span>{employee.department}</span></div>
            <div className="detail-item"><span className="label">Job Title</span><span>{employee.jobTitle}</span></div>
            <div className="detail-item"><span className="label">Salary</span><span>${Number(employee.salary).toLocaleString()}</span></div>
            <div className="detail-item"><span className="label">Hire Date</span><span>{employee.hireDate}</span></div>
            <div className="detail-item"><span className="label">Created At</span><span>{new Date(employee.createdAt).toLocaleString()}</span></div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default EmployeeModal;
