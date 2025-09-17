import React, { useState } from 'react'
import axios from 'axios'

function TestAPI() {
  const [selectedBackend, setSelectedBackend] = useState('express')
  const [responses, setResponses] = useState({})
  const [loading, setLoading] = useState({})

  const backendUrls = {
    express: 'http://localhost:8080',
    python: 'http://localhost:8081',
    kotlin: 'http://localhost:8082'
  }

  const testEndpoint = async (endpoint, method = 'GET', data = null) => {
    const key = `${endpoint}-${selectedBackend}`
    setLoading(prev => ({ ...prev, [key]: true }))

    try {
      const url = backendUrls[selectedBackend] + endpoint
      const token = localStorage.getItem('token')

      const config = {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      }

      let response
      if (method === 'GET') {
        response = await axios.get(url, config)
      } else if (method === 'POST') {
        response = await axios.post(url, data, config)
      }

      setResponses(prev => ({
        ...prev,
        [key]: {
          status: response.status,
          data: response.data,
          timestamp: new Date().toISOString()
        }
      }))
    } catch (error) {
      setResponses(prev => ({
        ...prev,
        [key]: {
          error: true,
          message: error.response?.data || error.message,
          status: error.response?.status,
          timestamp: new Date().toISOString()
        }
      }))
    } finally {
      setLoading(prev => ({ ...prev, [key]: false }))
    }
  }

  const testAuth = async (type) => {
    const testData = {
      email: `test${Date.now()}@example.com`,
      password: 'Test123!',
      firstName: 'Test',
      lastName: 'User'
    }

    // Adjust for Python backend
    if (selectedBackend === 'python' && type === 'register') {
      testData.first_name = testData.firstName
      testData.last_name = testData.lastName
      delete testData.firstName
      delete testData.lastName
    }

    await testEndpoint(
      `/api/auth/${type}`,
      'POST',
      type === 'login'
        ? { email: testData.email, password: testData.password }
        : testData
    )
  }

  const getResponseKey = (endpoint) => `${endpoint}-${selectedBackend}`

  return (
    <div className="container">
      <div className="test-section">
        <h2>API Testing Console</h2>

        <div className="backend-selector">
          <button
            className={`backend-btn ${selectedBackend === 'express' ? 'active' : ''}`}
            onClick={() => setSelectedBackend('express')}
          >
            Express (8080)
          </button>
          <button
            className={`backend-btn ${selectedBackend === 'python' ? 'active' : ''}`}
            onClick={() => setSelectedBackend('python')}
          >
            Python (8081)
          </button>
          <button
            className={`backend-btn ${selectedBackend === 'kotlin' ? 'active' : ''}`}
            onClick={() => setSelectedBackend('kotlin')}
          >
            Kotlin (8082)
          </button>
        </div>

        <div className="api-test">
          <h3>About Endpoint</h3>
          <button onClick={() => testEndpoint('/about.json')}>
            GET /about.json
          </button>
          {loading[getResponseKey('/about.json')] && <span className="loading"></span>}
          {responses[getResponseKey('/about.json')] && (
            <div className="response-box">
              {JSON.stringify(responses[getResponseKey('/about.json')], null, 2)}
            </div>
          )}
        </div>

        <div className="api-test">
          <h3>Authentication Endpoints</h3>
          <button onClick={() => testAuth('register')}>
            POST /api/auth/register (New User)
          </button>
          <button onClick={() => testAuth('login')}>
            POST /api/auth/login (Test Login)
          </button>
          {loading[getResponseKey('/api/auth/register')] && <span className="loading"></span>}
          {loading[getResponseKey('/api/auth/login')] && <span className="loading"></span>}
          {responses[getResponseKey('/api/auth/register')] && (
            <div className="response-box">
              <strong>Register Response:</strong>
              {JSON.stringify(responses[getResponseKey('/api/auth/register')], null, 2)}
            </div>
          )}
          {responses[getResponseKey('/api/auth/login')] && (
            <div className="response-box">
              <strong>Login Response:</strong>
              {JSON.stringify(responses[getResponseKey('/api/auth/login')], null, 2)}
            </div>
          )}
        </div>

        <div className="api-test">
          <h3>Custom Test</h3>
          <p>You can test custom endpoints by modifying the code or using browser dev tools.</p>
          <div style={{ marginTop: '10px' }}>
            <strong>Current Backend:</strong> {backendUrls[selectedBackend]}
          </div>
          <div style={{ marginTop: '10px' }}>
            <strong>Available Endpoints:</strong>
            <ul style={{ marginLeft: '20px', marginTop: '5px' }}>
              <li>GET /about.json</li>
              <li>POST /api/auth/register</li>
              <li>POST /api/auth/login</li>
            </ul>
          </div>
        </div>

        <div className="api-test">
          <h3>Backend Status</h3>
          <p>Make sure the backends are running:</p>
          <ul style={{ marginLeft: '20px', marginTop: '10px' }}>
            <li><strong>Express:</strong> cd express-backend && npm install && npm start</li>
            <li><strong>Python:</strong> cd python-backend && pip install -r requirements.txt && python main.py</li>
            <li><strong>Kotlin:</strong> cd kotlin-backend && ./gradlew bootRun</li>
          </ul>
        </div>
      </div>
    </div>
  )
}

export default TestAPI