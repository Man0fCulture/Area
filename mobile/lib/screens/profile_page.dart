import 'package:flutter/material.dart';
import '../models/user.dart';
import '../services/auth_service.dart';

class ProfilePage extends StatefulWidget {
  const ProfilePage({super.key});

  @override
  State<ProfilePage> createState() => _ProfilePageState();
}

class _ProfilePageState extends State<ProfilePage> {
  final AuthService _authService = AuthService();
  User? _user;
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadUserProfile();
  }

  Future<void> _loadUserProfile() async {
    try {
      final user = await _authService.getUserProfile();
      setState(() {
        _user = user;
        _isLoading = false;
        if (user == null) {
          _error = 'Impossible de charger le profil';
        }
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return _isLoading
        ? const Center(child: CircularProgressIndicator())
        : _error != null
            ? Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(Icons.error_outline, size: 48, color: Colors.red[300]),
                    const SizedBox(height: 16),
                    Text('Erreur: $_error'),
                  ],
                ),
              )
            : _user == null
                ? const Center(child: Text('Aucun profil trouvé'))
                : Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      children: [
                        const SizedBox(height: 20),
                        CircleAvatar(
                          radius: 60,
                          backgroundColor: Colors.blue[100],
                          child: Icon(
                            Icons.person,
                            size: 60,
                            color: Colors.blue[800],
                          ),
                        ),
                        const SizedBox(height: 16),
                        Text(
                          _user!.username,
                          style: TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                            color: Colors.blue[800],
                          ),
                        ),
                        const SizedBox(height: 8),
                        Text(
                          _user!.email,
                          style: TextStyle(
                            fontSize: 16,
                            color: Colors.grey[600],
                          ),
                        ),
                        const SizedBox(height: 32),
                        _buildProfileItem(
                          icon: Icons.email,
                          title: 'Email',
                          subtitle: _user!.email,
                        ),
                        _buildProfileItem(
                          icon: Icons.person,
                          title: 'Nom d\'utilisateur',
                          subtitle: _user!.username,
                        ),
                      ],
                    ),
                  );
  }

  Widget _buildProfileItem({required IconData icon, required String title, required String subtitle}) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 8),
      child: ListTile(
        leading: Icon(icon, color: Colors.blue),
        title: Text(title, style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Text(subtitle),
      ),
    );
  }
}
