import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:google_sign_in/google_sign_in.dart';
import '../constants/api_config.dart';
import '../models/token_response.dart';
import '../models/api_error.dart';
import 'api_service.dart';

class AuthService {
  static final AuthService _instance = AuthService._internal();
  factory AuthService() => _instance;
  AuthService._internal();

  final ApiService _apiService = ApiService();
  final GoogleSignIn _googleSignIn = GoogleSignIn(
    scopes: ['email', 'profile'],
  );
  static const String _accessTokenKey = 'access_token';
  static const String _refreshTokenKey = 'refresh_token';

  Future<void> saveTokens(TokenResponse tokenResponse) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_accessTokenKey, tokenResponse.accessToken);
    await prefs.setString(_refreshTokenKey, tokenResponse.refreshToken);
    _apiService.setAccessToken(tokenResponse.accessToken);
  }

  Future<String?> getAccessToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_accessTokenKey);
  }

  Future<String?> getRefreshToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_refreshTokenKey);
  }

  Future<void> clearTokens() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_accessTokenKey);
    await prefs.remove(_refreshTokenKey);
    _apiService.clearAccessToken();
  }

  Future<Map<String, dynamic>> login(String email, String password) async {
    try {
      final response = await _apiService.post(
        ApiConfig.loginEndpoint,
        {
          'email': email,
          'password': password,
        },
      );

      if (response.statusCode == 200) {
        final tokenResponse = TokenResponse.fromJson(jsonDecode(response.body));
        await saveTokens(tokenResponse);
        return {'success': true, 'data': tokenResponse};
      } else {
        final error = ApiError.fromJson(jsonDecode(response.body));
        return {'success': false, 'error': error.message};
      }
    } catch (e) {
      return {'success': false, 'error': 'Erreur de connexion: ${e.toString()}'};
    }
  }

  Future<Map<String, dynamic>> register(String email, String password, String username) async {
    try {
      final response = await _apiService.post(
        ApiConfig.registerEndpoint,
        {
          'email': email,
          'password': password,
          'username': username,
        },
      );

      if (response.statusCode == 201) {
        final tokenResponse = TokenResponse.fromJson(jsonDecode(response.body));
        await saveTokens(tokenResponse);
        return {'success': true, 'data': tokenResponse};
      } else {
        final error = ApiError.fromJson(jsonDecode(response.body));
        return {'success': false, 'error': error.message};
      }
    } catch (e) {
      return {'success': false, 'error': 'Erreur d\'inscription: ${e.toString()}'};
    }
  }

  Future<bool> refreshAccessToken() async {
    try {
      final refreshToken = await getRefreshToken();
      if (refreshToken == null) return false;

      final response = await _apiService.post(
        ApiConfig.refreshEndpoint,
        {'refreshToken': refreshToken},
      );

      if (response.statusCode == 200) {
        final tokenResponse = TokenResponse.fromJson(jsonDecode(response.body));
        await saveTokens(tokenResponse);
        return true;
      }
      return false;
    } catch (e) {
      return false;
    }
  }

  Future<bool> isLoggedIn() async {
    final token = await getAccessToken();
    if (token != null) {
      _apiService.setAccessToken(token);
      return true;
    }
    return false;
  }

  Future<void> logout() async {
    await clearTokens();
    await _googleSignIn.signOut();
  }

  Future<Map<String, dynamic>> loginWithGoogle() async {
    try {
      final GoogleSignInAccount? account = await _googleSignIn.signIn();

      if (account == null) {
        return {'success': false, 'error': 'Connexion annulée'};
      }

      final GoogleSignInAuthentication auth = await account.authentication;

      // Send the Google ID token to your backend
      // For now, we'll use a placeholder endpoint - you'll need to implement this on your server
      final response = await _apiService.post(
        '${ApiConfig.loginEndpoint}/google',
        {
          'idToken': auth.idToken,
          'email': account.email,
          'displayName': account.displayName,
        },
      );

      if (response.statusCode == 200) {
        final tokenResponse = TokenResponse.fromJson(jsonDecode(response.body));
        await saveTokens(tokenResponse);
        return {'success': true, 'data': tokenResponse};
      } else {
        final error = ApiError.fromJson(jsonDecode(response.body));
        return {'success': false, 'error': error.message};
      }
    } catch (e) {
      return {'success': false, 'error': 'Erreur OAuth: ${e.toString()}'};
    }
  }
}
