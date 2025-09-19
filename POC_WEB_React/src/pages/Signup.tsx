import React, { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'

const Signup: React.FC = () => {
  const navigate = useNavigate()
  const [fullname, setFullname] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [terms, setTerms] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')
  const [touched, setTouched] = useState({
    email: false,
    password: false,
    confirmPassword: false
  })

  // Validate on change after field has been touched
  useEffect(() => {
    if (touched.email && email) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      if (!emailRegex.test(email)) {
        setErrorMessage('Please enter a valid email address')
      } else {
        setErrorMessage('')
      }
    }
  }, [email, touched.email])

  useEffect(() => {
    if (touched.password && password) {
      const specialCharRegex = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/
      if (!specialCharRegex.test(password)) {
        setErrorMessage('Password must include at least one special character')
      } else if (touched.confirmPassword && confirmPassword && password !== confirmPassword) {
        setErrorMessage('Passwords do not match')
      } else {
        setErrorMessage('')
      }
    }
  }, [password, touched.password, confirmPassword, touched.confirmPassword])

  useEffect(() => {
    if (touched.confirmPassword && confirmPassword && password) {
      if (password !== confirmPassword) {
        setErrorMessage('Passwords do not match')
      } else {
        const specialCharRegex = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/
        if (touched.password && !specialCharRegex.test(password)) {
          setErrorMessage('Password must include at least one special character')
        } else {
          setErrorMessage('')
        }
      }
    }
  }, [confirmPassword, password, touched.confirmPassword, touched.password])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()

    // Clear previous error messages
    setErrorMessage('')

    // Email validation regex
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

    // Check if email is correctly formatted
    if (!emailRegex.test(email)) {
      setErrorMessage('Please enter a valid email address')
      return
    }

    // Check for special character in password
    const specialCharRegex = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/
    if (!specialCharRegex.test(password)) {
      setErrorMessage('Password must include at least one special character')
      return
    }

    // Check if passwords match
    if (password !== confirmPassword) {
      setErrorMessage('Passwords do not match')
      return
    }

    // Check if terms are accepted
    if (!terms) {
      setErrorMessage('Please check the box to agree to the Terms & Conditions')
      return
    }

    // All validation passed - redirect to success page
    navigate('/signup-success')
  }

  return (
    <div className="login-container">
      <form className="login-form" onSubmit={handleSubmit}>
        <h1>Sign Up</h1>
        {errorMessage && (
          <div className="error-message">{errorMessage}</div>
        )}
        <div className="input-group">
          <label htmlFor="fullname">Full Name</label>
          <input
            type="text"
            id="fullname"
            name="fullname"
            required
            placeholder="Enter your full name"
            value={fullname}
            onChange={(e) => setFullname(e.target.value)}
          />
        </div>
        <div className="input-group">
          <label htmlFor="email">Email</label>
          <input
            type="email"
            id="email"
            name="email"
            required
            placeholder="Enter your email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            onBlur={() => setTouched({ ...touched, email: true })}
          />
        </div>
        <div className="input-group">
          <label htmlFor="password">
            Password <span className="password-hint">(must include a special character)</span>
          </label>
          <input
            type="password"
            id="password"
            name="password"
            required
            placeholder="Create a password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            onBlur={() => setTouched({ ...touched, password: true })}
          />
        </div>
        <div className="input-group">
          <label htmlFor="confirm-password">Confirm Password</label>
          <input
            type="password"
            id="confirm-password"
            name="confirm-password"
            required
            placeholder="Confirm your password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            onBlur={() => setTouched({ ...touched, confirmPassword: true })}
          />
        </div>
        <div className="remember-forgot">
          <label className="remember-me">
            <input
              type="checkbox"
              name="terms"
              required
              checked={terms}
              onChange={(e) => setTerms(e.target.checked)}
            />
            I agree to the Terms & Conditions
          </label>
        </div>
        <button type="submit" className="login-btn">
          Create Account
        </button>
        <div className="signup-link">
          Already have an account? <Link to="/login">Login</Link>
        </div>
      </form>
    </div>
  )
}

export default Signup