import { Routes, Route, Navigate } from 'react-router-dom'
import Login from './pages/Login'
import Signup from './pages/Signup'
import Success from './pages/Success'
import SignupSuccess from './pages/SignupSuccess'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />
      <Route path="/success" element={<Success />} />
      <Route path="/signup-success" element={<SignupSuccess />} />
    </Routes>
  )
}

export default App