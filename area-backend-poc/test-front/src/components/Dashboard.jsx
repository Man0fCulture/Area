import React from 'react'

function Dashboard({ user }) {
  return (
    <div className="container">
      <div className="dashboard">
        <h1>Welcome to AREA Dashboard</h1>

        <div className="user-info">
          <h3>User Information</h3>
          <p><strong>ID:</strong> {user?.id}</p>
          <p><strong>Email:</strong> {user?.email}</p>
          <p><strong>First Name:</strong> {user?.firstName || user?.first_name}</p>
          <p><strong>Last Name:</strong> {user?.lastName || user?.last_name}</p>
        </div>

        <div className="user-info">
          <h3>Authentication Token</h3>
          <p style={{
            fontFamily: 'monospace',
            fontSize: '12px',
            wordBreak: 'break-all',
            background: '#f0f0f0',
            padding: '10px',
            borderRadius: '5px'
          }}>
            {localStorage.getItem('token')}
          </p>
        </div>

        <div className="user-info">
          <h3>Available Services</h3>
          <p>You can test the following API endpoints:</p>
          <ul style={{ marginLeft: '20px', marginTop: '10px' }}>
            <li>Authentication (Login/Register)</li>
            <li>About endpoint for service information</li>
            <li>User management</li>
          </ul>
          <p style={{ marginTop: '15px' }}>
            Navigate to <strong>Test API</strong> to interact with the different backends.
          </p>
        </div>
      </div>
    </div>
  )
}

export default Dashboard