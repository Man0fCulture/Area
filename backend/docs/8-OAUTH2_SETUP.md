# OAuth2 Setup Guide

## Google OAuth2 Configuration

### 1. Google Cloud Console Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Enable Google+ API
4. Go to "Credentials" → "Create Credentials" → "OAuth 2.0 Client ID"

### 2. OAuth2 Client Configuration

Create **ONE OAuth2 Web Application** client:

```
Application Type: Web application
Name: AREA Platform

Authorized JavaScript origins:
- http://localhost:3000      # React development
- http://localhost:8080      # Backend
- http://localhost:8081      # Flutter web debug
- https://yourdomain.com     # Production

Authorized redirect URIs:
- http://localhost:8080/api/auth/oauth/google/callback
- https://yourdomain.com/api/auth/oauth/google/callback
```

### 3. Configure Backend (.env)

```bash
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-client-secret
GOOGLE_REDIRECT_URI=http://localhost:8080/api/auth/oauth/google/callback
```

## Integration Guide

### Web (React) Integration

```javascript
// 1. Initialize OAuth2 flow
const response = await fetch('/api/auth/oauth/init', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    provider: 'google',
    state: 'optional-csrf-token'
  })
});
const { authUrl } = await response.json();

// 2. Redirect user to Google
window.location.href = authUrl;

// 3. Handle callback in your React app
// User will be redirected to: /auth/success?access_token=xxx&refresh_token=xxx
// Store tokens and redirect to dashboard
```

### Mobile (Flutter) Integration

```dart
import 'package:google_sign_in/google_sign_in.dart';
import 'package:http/http.dart' as http;

// 1. Configure Google Sign In
final GoogleSignIn _googleSignIn = GoogleSignIn(
  scopes: ['email', 'profile'],
  clientId: 'YOUR_GOOGLE_CLIENT_ID', // Same as backend
);

// 2. Sign in with Google
Future<void> signInWithGoogle() async {
  try {
    // Get authorization code from Google
    final GoogleSignInAccount? account = await _googleSignIn.signIn();
    final GoogleSignInAuthentication auth = await account!.authentication;

    // 3. Exchange code with backend
    final response = await http.post(
      Uri.parse('http://localhost:8080/api/auth/oauth/google/token'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'code': auth.serverAuthCode,
        'redirectUri': 'com.yourapp://oauth2redirect', // Flutter redirect URI
      }),
    );

    // 4. Store JWT tokens
    final data = jsonDecode(response.body);
    await saveTokens(data['accessToken'], data['refreshToken']);

    // 5. Navigate to home screen
    Navigator.pushReplacementNamed(context, '/home');
  } catch (error) {
    print('Sign in failed: $error');
  }
}
```

## API Endpoints

### Web Flow
```
POST /api/auth/oauth/init
GET  /api/auth/oauth/google/callback?code=xxx&state=xxx
```

### Mobile Flow
```
POST /api/auth/oauth/init                    # Get auth URL
POST /api/auth/oauth/google/token           # Exchange code for tokens
```

### Common Endpoints (Both Platforms)
```
GET  /api/auth/oauth/providers              # List available providers
POST /api/auth/oauth/link                   # Link account (requires auth)
DELETE /api/auth/oauth/unlink               # Unlink account (requires auth)
GET  /api/auth/oauth/linked-accounts        # Get linked accounts
```

## Testing

### Test Web Flow
```bash
# 1. Get auth URL
curl -X POST http://localhost:8080/api/auth/oauth/init \
  -H "Content-Type: application/json" \
  -d '{"provider":"google"}'

# 2. Open authUrl in browser
# 3. After Google auth, you'll be redirected to your frontend
```

### Test Mobile Flow
```bash
# Exchange authorization code for tokens
curl -X POST http://localhost:8080/api/auth/oauth/google/token \
  -H "Content-Type: application/json" \
  -d '{
    "code": "authorization-code-from-google",
    "redirectUri": "com.yourapp://oauth2redirect"
  }'
```

## Security Notes

1. **NEVER expose** `GOOGLE_CLIENT_SECRET` to frontend
2. **State parameter** should be used for CSRF protection in web flow
3. **Tokens in URL** (web flow) should be extracted and stored immediately
4. **Mobile apps** should use secure storage for tokens (e.g., Keychain, Keystore)

## Troubleshooting

### "Provider not configured"
- Check `.env` file has Google credentials
- Restart server after updating `.env`

### "Invalid redirect URI"
- Ensure redirect URI in request matches Google Console config exactly
- For mobile: Use custom scheme (e.g., `com.yourapp://`)

### "Authentication failed"
- Check Google Console has correct origins/redirects
- Verify API is enabled in Google Console
- Check server logs for detailed error