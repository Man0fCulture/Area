import 'dart:io';

class ApiConfig {
  static const String _serverHost = '192.168.1.28';
  static const String _serverPort = '8080';

  static String get baseUrl {
    if (Platform.isAndroid) {
      return 'http://$_serverHost:$_serverPort';
    } else {
      return 'http://$_serverHost:$_serverPort';
    }
  }

  // Auth endpoints
  static const String loginEndpoint = '/api/auth/login';
  static const String registerEndpoint = '/api/auth/register';
  static const String refreshEndpoint = '/api/auth/refresh';

  // Services endpoints
  static const String servicesEndpoint = '/api/services';

  // Areas endpoints
  static const String areasEndpoint = '/api/areas';

  // Public endpoints
  static const String healthEndpoint = '/health';
  static const String aboutEndpoint = '/about.json';
}
