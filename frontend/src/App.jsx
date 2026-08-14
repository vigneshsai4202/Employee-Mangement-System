import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import { useEffect, useState } from 'react';
import Navbar from './components/Navbar';
import Dashboard from './pages/Dashboard';
import AddEmployee from './pages/AddEmployee';
import EditEmployee from './pages/EditEmployee';

function AppContent() {
  const location = useLocation();
  const [flash, setFlash] = useState('');

  // useEffect → picks up success messages passed via navigate(path, { state })
  useEffect(() => {
    if (location.state?.success) {
      setFlash(location.state.success);
      setTimeout(() => setFlash(''), 3000);
    }
  }, [location]);

  return (
    <>
      <Navbar />
      <main className="main-content">
        {flash && <div className="alert alert-success flash">{flash}</div>}
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/employees/new" element={<AddEmployee />} />
          <Route path="/employees/edit/:id" element={<EditEmployee />} />
        </Routes>
      </main>
    </>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
}

export default App;
