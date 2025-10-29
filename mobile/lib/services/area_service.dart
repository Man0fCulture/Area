import 'dart:convert';
import '../models/area.dart';
import '../models/service.dart';
import '../constants/api_config.dart';
import 'api_service.dart';

class AreaService {
  static final AreaService _instance = AreaService._internal();
  factory AreaService() => _instance;
  AreaService._internal();

  final ApiService _apiService = ApiService();

  Future<List<Area>> getAreas() async {
    try {
      final response = await _apiService.get(ApiConfig.areasEndpoint, requiresAuth: true);

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((area) => Area.fromJson(area)).toList();
      } else {
        throw Exception('Failed to load areas');
      }
    } catch (e) {
      throw Exception('Error fetching areas: ${e.toString()}');
    }
  }

  Future<Area> getArea(String id) async {
    try {
      final response = await _apiService.get('${ApiConfig.areasEndpoint}/$id', requiresAuth: true);

      if (response.statusCode == 200) {
        return Area.fromJson(jsonDecode(response.body));
      } else {
        throw Exception('Failed to load area');
      }
    } catch (e) {
      throw Exception('Error fetching area: ${e.toString()}');
    }
  }

  Future<Area> createArea({
    required String name,
    String? description,
    required String actionServiceId,
    required String actionId,
    required Map<String, dynamic> actionConfig,
    required List<Map<String, dynamic>> reactions,
  }) async {
    try {
      final response = await _apiService.post(
        ApiConfig.areasEndpoint,
        {
          'name': name,
          'description': description,
          'action': {
            'serviceId': actionServiceId,
            'actionId': actionId,
            'config': actionConfig,
          },
          'reactions': reactions,
        },
        requiresAuth: true,
      );

      if (response.statusCode == 201) {
        return Area.fromJson(jsonDecode(response.body));
      } else {
        throw Exception('Failed to create area');
      }
    } catch (e) {
      throw Exception('Error creating area: ${e.toString()}');
    }
  }

  Future<Area> updateArea(String id, {String? name, String? description, bool? active}) async {
    try {
      final Map<String, dynamic> data = {};
      if (name != null) data['name'] = name;
      if (description != null) data['description'] = description;
      if (active != null) data['active'] = active;

      final response = await _apiService.patch('${ApiConfig.areasEndpoint}/$id', data, requiresAuth: true);

      if (response.statusCode == 200) {
        return Area.fromJson(jsonDecode(response.body));
      } else {
        throw Exception('Failed to update area');
      }
    } catch (e) {
      throw Exception('Error updating area: ${e.toString()}');
    }
  }

  Future<void> deleteArea(String id) async {
    try {
      final response = await _apiService.delete('${ApiConfig.areasEndpoint}/$id', requiresAuth: true);

      if (response.statusCode != 204) {
        throw Exception('Failed to delete area');
      }
    } catch (e) {
      throw Exception('Error deleting area: ${e.toString()}');
    }
  }

  Future<Area> toggleArea(String id, bool active) async {
    try {
      final endpoint = active
          ? '${ApiConfig.areasEndpoint}/$id/activate'
          : '${ApiConfig.areasEndpoint}/$id/deactivate';

      final response = await _apiService.post(endpoint, {}, requiresAuth: true);

      if (response.statusCode == 200) {
        return Area.fromJson(jsonDecode(response.body));
      } else {
        throw Exception('Failed to toggle area');
      }
    } catch (e) {
      throw Exception('Error toggling area: ${e.toString()}');
    }
  }

  Future<void> testArea(String id) async {
    try {
      final response = await _apiService.post('${ApiConfig.areasEndpoint}/$id/test', {}, requiresAuth: true);

      if (response.statusCode != 200) {
        throw Exception('Failed to test area');
      }
    } catch (e) {
      throw Exception('Error testing area: ${e.toString()}');
    }
  }

  Future<List<Service>> getServices() async {
    try {
      final response = await _apiService.get(ApiConfig.servicesEndpoint);

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((service) => Service.fromJson(service)).toList();
      } else {
        throw Exception('Failed to load services');
      }
    } catch (e) {
      throw Exception('Error fetching services: ${e.toString()}');
    }
  }

  Future<List<Service>> getSubscribedServices() async {
    try {
      final response = await _apiService.get('/api/user/services', requiresAuth: true);

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((service) => Service.fromJson(service)).toList();
      } else {
        throw Exception('Failed to load subscribed services');
      }
    } catch (e) {
      throw Exception('Error fetching subscribed services: ${e.toString()}');
    }
  }

  Future<void> subscribeToService(String serviceId) async {
    try {
      final response = await _apiService.post(
        '/api/user/services/$serviceId/subscribe',
        {},
        requiresAuth: true,
      );

      if (response.statusCode != 200 && response.statusCode != 201) {
        throw Exception('Failed to subscribe to service');
      }
    } catch (e) {
      throw Exception('Error subscribing to service: ${e.toString()}');
    }
  }

  Future<void> unsubscribeFromService(String serviceId) async {
    try {
      final response = await _apiService.post(
        '/api/user/services/$serviceId/unsubscribe',
        {},
        requiresAuth: true,
      );

      if (response.statusCode != 200 && response.statusCode != 204) {
        throw Exception('Failed to unsubscribe from service');
      }
    } catch (e) {
      throw Exception('Error unsubscribing from service: ${e.toString()}');
    }
  }
}
