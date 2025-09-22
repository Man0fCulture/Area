import React from 'react'
import { Link } from 'react-router-dom'

const SignupSuccess: React.FC = () => {
  return (
    <div className="login-container">
      <div className="success-content">
        <div className="success-icon">✓</div>
        <h1>You have successfully created your account!</h1>
        <p>Your account has been created. You can now login with your credentials.</p>
        <Link to="/login" className="logout-btn">
          Go to Login
        </Link>
      </div>
    </div>
  )
}

export default SignupSuccess