class ApiConfig {
  static const String baseUrl = 'http://localhost:8080';

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
