import React, { useState } from 'react'
import axios from 'axios'

function Login({ onLogin }) {
  const [isRegister, setIsRegister] = useState(false)
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: ''
  })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)
  const [selectedBackend, setSelectedBackend] = useState('express')

  const backendUrls = {
    express: 'http://localhost:8080',
    python: 'http://localhost:8081',
    kotlin: 'http://localhost:8082'
  }

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setLoading(true)

    try {
      const endpoint = isRegister ? '/api/auth/register' : '/api/auth/login'
      const url = backendUrls[selectedBackend] + endpoint

      const requestData = isRegister ? {
        email: formData.email,
        password: formData.password,
        firstName: formData.firstName,
        lastName: formData.lastName
      } : {
        email: formData.email,
        password: formData.password
      }

      // Adjust field names for Python backend
      if (selectedBackend === 'python') {
        if (isRegister) {
          requestData.first_name = requestData.firstName
          requestData.last_name = requestData.lastName
          delete requestData.firstName
          delete requestData.lastName
        }
      }

      const response = await axios.post(url, requestData)

      if (response.data) {
        const { token, user } = response.data
        setSuccess(isRegister ? 'Registration successful!' : 'Login successful!')

        // Normalize user data for Python backend
        const normalizedUser = selectedBackend === 'python' ? {
          ...user,
          firstName: user.first_name || user.firstName,
          lastName: user.last_name || user.lastName
        } : user

        setTimeout(() => {
          onLogin(token, normalizedUser)
        }, 1000)
      }
    } catch (err) {
      console.error('Auth error:', err)
      setError(err.response?.data?.error || err.response?.data?.detail || 'Authentication failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <form className="auth-form" onSubmit={handleSubmit}>
        <h2>{isRegister ? 'Register' : 'Login'}</h2>

        <div className="backend-selector">
          <button
            type="button"
            className={`backend-btn ${selectedBackend === 'express' ? 'active' : ''}`}
            onClick={() => setSelectedBackend('express')}
          >
            Express (8080)
          </button>
          <button
            type="button"
            className={`backend-btn ${selectedBackend === 'python' ? 'active' : ''}`}
            onClick={() => setSelectedBackend('python')}
          >
            Python (8081)
          </button>
          <button
            type="button"
            className={`backend-btn ${selectedBackend === 'kotlin' ? 'active' : ''}`}
            onClick={() => setSelectedBackend('kotlin')}
          >
            Kotlin (8082)
          </button>
        </div>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        <div className="form-group">
          <label>Email</label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            required
            placeholder="john.doe@example.com"
          />
        </div>

        <div className="form-group">
          <label>Password</label>
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            required
            placeholder="Enter your password"
          />
        </div>

        {isRegister && (
          <>
            <div className="form-group">
              <label>First Name</label>
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                required={isRegister}
                placeholder="John"
              />
            </div>

            <div className="form-group">
              <label>Last Name</label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                required={isRegister}
                placeholder="Doe"
              />
            </div>
          </>
        )}

        <button type="submit" className="btn" disabled={loading}>
          {loading ? 'Processing...' : (isRegister ? 'Register' : 'Login')}
        </button>

        <div className="auth-switch">
          {isRegister ? (
            <>
              Already have an account?{' '}
              <button type="button" onClick={() => setIsRegister(false)}>
                Login
              </button>
            </>
          ) : (
            <>
              Don't have an account?{' '}
              <button type="button" onClick={() => setIsRegister(true)}>
                Register
              </button>
            </>
          )}
        </div>
      </form>
    </div>
  )
}

export default Login