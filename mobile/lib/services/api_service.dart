import 'dart:convert';
import 'package:http/http.dart' as http;
import '../constants/api_config.dart';

class ApiService {
  static final ApiService _instance = ApiService._internal();
  factory ApiService() => _instance;
  ApiService._internal();

  String? _accessToken;

  void setAccessToken(String token) {
    _accessToken = token;
  }

  void clearAccessToken() {
    _accessToken = null;
  }

  Map<String, String> _getHeaders({bool includeAuth = false}) {
    final headers = {
      'Content-Type': 'application/json',
    };

    if (includeAuth && _accessToken != null) {
      headers['Authorization'] = 'Bearer $_accessToken';
    }

    return headers;
  }

  Future<http.Response> get(String endpoint, {bool requiresAuth = false}) async {
    final url = Uri.parse('${ApiConfig.baseUrl}$endpoint');
    return await http.get(
      url,
      headers: _getHeaders(includeAuth: requiresAuth),
    );
  }

  Future<http.Response> post(
    String endpoint,
    Map<String, dynamic> body, {
    bool requiresAuth = false,
  }) async {
    final url = Uri.parse('${ApiConfig.baseUrl}$endpoint');
    return await http.post(
      url,
      headers: _getHeaders(includeAuth: requiresAuth),
      body: jsonEncode(body),
    );
  }

  Future<http.Response> patch(
    String endpoint,
    Map<String, dynamic> body, {
    bool requiresAuth = false,
  }) async {
    final url = Uri.parse('${ApiConfig.baseUrl}$endpoint');
    return await http.patch(
      url,
      headers: _getHeaders(includeAuth: requiresAuth),
      body: jsonEncode(body),
    );
  }

  Future<http.Response> delete(String endpoint, {bool requiresAuth = false}) async {
    final url = Uri.parse('${ApiConfig.baseUrl}$endpoint');
    return await http.delete(
      url,
      headers: _getHeaders(includeAuth: requiresAuth),
    );
  }
}
