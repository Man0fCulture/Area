# OAuth2 Popup Integration Guide

## Why Popup Flow?

- ✅ Better UX - No full page redirect
- ✅ Preserves application state
- ✅ Works with SPAs (Single Page Applications)
- ✅ Same backend, different mode

## React Integration

### 1. Install Dependencies
```bash
npm install axios
```

### 2. OAuth2 Service
```javascript
// services/oauth2Service.js
import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

export class OAuth2Service {
  constructor() {
    this.popup = null;
    this.listeners = new Map();
  }

  async loginWithGoogle() {
    return this.authenticate('google');
  }

  async authenticate(provider) {
    return new Promise(async (resolve, reject) => {
      try {
        // 1. Get auth URL with popup mode
        const { data } = await axios.post(`${API_URL}/auth/oauth/init`, {
          provider,
          mode: 'popup', // IMPORTANT: Tell backend we want popup mode
          state: this.generateState()
        });

        // 2. Open popup
        this.popup = window.open(
          data.authUrl,
          'oauth2-popup',
          'width=500,height=600,left=100,top=100'
        );

        // 3. Listen for message from popup
        const messageHandler = (event) => {
          // Security: Check origin in production
          if (event.origin !== 'http://localhost:8080') return;

          if (event.data.type === 'oauth2_success') {
            // Success! Store tokens
            localStorage.setItem('accessToken', event.data.accessToken);
            localStorage.setItem('refreshToken', event.data.refreshToken);

            window.removeEventListener('message', messageHandler);
            resolve({
              accessToken: event.data.accessToken,
              refreshToken: event.data.refreshToken
            });
          } else if (event.data.type === 'oauth2_error') {
            window.removeEventListener('message', messageHandler);
            reject(new Error(event.data.error));
          }
        };

        window.addEventListener('message', messageHandler);

        // 4. Check if popup was blocked
        if (!this.popup || this.popup.closed) {
          window.removeEventListener('message', messageHandler);
          reject(new Error('Popup was blocked. Please allow popups for this site.'));
        }

        // 5. Monitor popup closing
        const checkClosed = setInterval(() => {
          if (this.popup && this.popup.closed) {
            clearInterval(checkClosed);
            window.removeEventListener('message', messageHandler);
            reject(new Error('Authentication cancelled'));
          }
        }, 1000);

      } catch (error) {
        reject(error);
      }
    });
  }

  generateState() {
    return Math.random().toString(36).substring(7);
  }
}
```

### 3. React Component
```jsx
// components/LoginButton.jsx
import React, { useState } from 'react';
import { OAuth2Service } from '../services/oauth2Service';

const oauth2Service = new OAuth2Service();

export function LoginButton() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleGoogleLogin = async () => {
    setLoading(true);
    setError(null);

    try {
      const tokens = await oauth2Service.loginWithGoogle();
      console.log('Login successful!', tokens);

      // Redirect to dashboard or update app state
      window.location.href = '/dashboard';
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <button
        onClick={handleGoogleLogin}
        disabled={loading}
        className="google-login-btn"
      >
        {loading ? 'Authenticating...' : 'Login with Google'}
      </button>

      {error && (
        <div className="error-message">
          {error}
        </div>
      )}
    </div>
  );
}
```

## Flutter Web Integration

### 1. Dependencies
```yaml
# pubspec.yaml
dependencies:
  http: ^1.1.0
  url_launcher: ^6.1.14
  js: ^0.6.7
```

### 2. OAuth2 Service for Flutter Web
```dart
// lib/services/oauth2_service.dart
import 'dart:html' as html;
import 'dart:convert';
import 'package:http/http.dart' as http;

class OAuth2Service {
  static const String apiUrl = 'http://localhost:8080/api';
  html.WindowBase? _popup;

  Future<Map<String, String>> loginWithGoogle() async {
    try {
      // 1. Get auth URL with popup mode
      final response = await http.post(
        Uri.parse('$apiUrl/auth/oauth/init'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'provider': 'google',
          'mode': 'popup',
          'state': _generateState(),
        }),
      );

      if (response.statusCode != 200) {
        throw Exception('Failed to initialize OAuth');
      }

      final data = jsonDecode(response.body);
      final authUrl = data['authUrl'];

      // 2. Open popup
      _popup = html.window.open(
        authUrl,
        'oauth2-popup',
        'width=500,height=600,left=100,top=100',
      );

      // 3. Listen for messages from popup
      final completer = Completer<Map<String, String>>();

      html.window.onMessage.listen((event) {
        final messageData = event.data;

        if (messageData['type'] == 'oauth2_success') {
          completer.complete({
            'accessToken': messageData['accessToken'],
            'refreshToken': messageData['refreshToken'],
          });
          _popup?.close();
        } else if (messageData['type'] == 'oauth2_error') {
          completer.completeError(
            Exception(messageData['error'] ?? 'Authentication failed')
          );
          _popup?.close();
        }
      });

      // 4. Monitor popup status
      Timer.periodic(Duration(seconds: 1), (timer) {
        if (_popup != null && _popup!.closed!) {
          timer.cancel();
          if (!completer.isCompleted) {
            completer.completeError(Exception('Authentication cancelled'));
          }
        }
      });

      return await completer.future;
    } catch (e) {
      throw Exception('OAuth2 login failed: $e');
    }
  }

  String _generateState() {
    return DateTime.now().millisecondsSinceEpoch.toString();
  }
}
```

### 3. Flutter Widget
```dart
// lib/widgets/login_button.dart
import 'package:flutter/material.dart';
import '../services/oauth2_service.dart';

class LoginButton extends StatefulWidget {
  @override
  _LoginButtonState createState() => _LoginButtonState();
}

class _LoginButtonState extends State<LoginButton> {
  final _oauth2Service = OAuth2Service();
  bool _isLoading = false;
  String? _error;

  Future<void> _handleGoogleLogin() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final tokens = await _oauth2Service.loginWithGoogle();

      // Store tokens (use secure storage in production)
      // await SecureStorage.store('accessToken', tokens['accessToken']);
      // await SecureStorage.store('refreshToken', tokens['refreshToken']);

      // Navigate to home
      Navigator.pushReplacementNamed(context, '/home');
    } catch (e) {
      setState(() {
        _error = e.toString();
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        ElevatedButton.icon(
          onPressed: _isLoading ? null : _handleGoogleLogin,
          icon: _isLoading
              ? SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(strokeWidth: 2),
                )
              : Image.asset('assets/google_logo.png', height: 20),
          label: Text(_isLoading ? 'Authenticating...' : 'Login with Google'),
          style: ElevatedButton.styleFrom(
            padding: EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          ),
        ),
        if (_error != null) ...[
          SizedBox(height: 16),
          Text(
            _error!,
            style: TextStyle(color: Colors.red),
          ),
        ],
      ],
    );
  }
}
```

## Google Console Configuration

Add these URLs to your OAuth2 client:

**Authorized JavaScript origins:**
```
http://localhost:3000
http://localhost:8080
http://localhost:8081
```

**Authorized redirect URIs:**
```
http://localhost:8080/api/auth/oauth/google/callback
http://localhost:8080/api/auth/oauth/google/callback?mode=popup
```

## Testing

### 1. Test Popup Mode
```bash
# Start backend
./gradlew run

# Test popup flow
curl -X POST http://localhost:8080/api/auth/oauth/init \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "mode": "popup"
  }'
```

### 2. Test Redirect Mode (fallback)
```bash
curl -X POST http://localhost:8080/api/auth/oauth/init \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "mode": "redirect"
  }'
```

## Security Considerations

1. **Check Origin**: Always verify `event.origin` in production
2. **Use HTTPS**: Required for production OAuth2
3. **State Parameter**: Prevent CSRF attacks
4. **Secure Storage**: Use secure storage for tokens (not localStorage in production)
5. **Token Expiry**: Implement token refresh logic

## Troubleshooting

### Popup Blocked
- Add site to popup exceptions
- Show user-friendly message
- Provide fallback redirect option

### CORS Issues
- Check `.env` has correct `CORS_ALLOWED_HOSTS`
- Verify origins in Google Console

### postMessage Not Received
- Check popup origin matches expected
- Verify HTML callback page is served correctly
- Check browser console for errors