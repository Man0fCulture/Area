import React from 'react'
import { Link } from 'react-router-dom'

const Success: React.FC = () => {
  return (
    <div className="login-container">
      <div className="success-content">
        <div className="success-icon">✓</div>
        <h1>You're successfully logged in!</h1>
        <p>Welcome back! You have been successfully authenticated.</p>
        <Link to="/login" className="logout-btn">
          Logout
        </Link>
      </div>
    </div>
  )
}

export default Success