const DEPARTMENTS = ['IT', 'HR', 'Finance', 'Sales', 'Marketing', 'Operations'];

function SearchBar({ keyword, department, onKeywordChange, onDepartmentChange, onClear }) {
  return (
    <div className="search-bar">
      <input
        type="text"
        placeholder="Search employees by name or email..."
        value={keyword}
        onChange={(e) => onKeywordChange(e.target.value)}
        className="search-input"
      />
      <select
        value={department}
        onChange={(e) => onDepartmentChange(e.target.value)}
        className="department-select"
      >
        <option value="">All Departments</option>
        {DEPARTMENTS.map((dept) => (
          <option key={dept} value={dept}>{dept}</option>
        ))}
      </select>
      {(keyword || department) && (
        <button onClick={onClear} className="btn btn-secondary">
          Clear
        </button>
      )}
    </div>
  );
}

export default SearchBar;
