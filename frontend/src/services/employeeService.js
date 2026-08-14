import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

const employeeService = {
  getEmployees: () => api.get('/employees'),
  getEmployeeById: (id) => api.get(`/employees/${id}`),
  createEmployee: (employee) => api.post('/employees', employee),
  updateEmployee: (id, employee) => api.put(`/employees/${id}`, employee),
  deleteEmployee: (id) => api.delete(`/employees/${id}`),
  searchEmployees: (keyword) => api.get(`/employees/search?keyword=${keyword}`),
  getEmployeesByDepartment: (department) => api.get(`/employees/department/${department}`),
};

export default employeeService;
