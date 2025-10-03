import 'package:flutter/material.dart';
import 'login_page.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  int _selectedIndex = 0;

  final List<Widget> _pages = [
    const DashboardPage(),
    const ProfilePage(),
    const SettingsPage(),
  ];

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  void _logout() {
    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(builder: (context) => const LoginPage()),
      (route) => false,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Accueil'),
        actions: [
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: _logout,
            tooltip: 'Se déconnecter',
          ),
        ],
      ),
      body: _pages[_selectedIndex],
      bottomNavigationBar: BottomNavigationBar(
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.dashboard),
            label: 'Tableau de bord',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.person),
            label: 'Profil',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.settings),
            label: 'Paramètres',
          ),
        ],
        currentIndex: _selectedIndex,
        selectedItemColor: Colors.blue,
        onTap: _onItemTapped,
      ),
    );
  }
}

class DashboardPage extends StatelessWidget {
  const DashboardPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Bienvenue !',
            style: TextStyle(
              fontSize: 28,
              fontWeight: FontWeight.bold,
              color: Colors.blue[800],
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Voici votre tableau de bord',
            style: TextStyle(
              fontSize: 16,
              color: Colors.grey[600],
            ),
          ),
          const SizedBox(height: 24),
          Expanded(
            child: GridView.count(
              crossAxisCount: 2,
              crossAxisSpacing: 16,
              mainAxisSpacing: 16,
              children: [
                _buildCard(
                  icon: Icons.analytics,
                  title: 'Statistiques',
                  color: Colors.blue,
                ),
                _buildCard(
                  icon: Icons.notifications,
                  title: 'Notifications',
                  color: Colors.blueAccent,
                ),
                _buildCard(
                  icon: Icons.folder,
                  title: 'Documents',
                  color: Colors.lightBlue,
                ),
                _buildCard(
                  icon: Icons.help,
                  title: 'Aide',
                  color: Colors.blue[300]!,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCard({required IconData icon, required String title, required Color color}) {
    return Card(
      elevation: 2,
      child: InkWell(
        onTap: () {},
        child: Container(
          padding: const EdgeInsets.all(16),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon, size: 48, color: color),
              const SizedBox(height: 12),
              Text(
                title,
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                ),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class ProfilePage extends StatelessWidget {
  const ProfilePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Padding(
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
            'Utilisateur',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Colors.blue[800],
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'user@example.com',
            style: TextStyle(
              fontSize: 16,
              color: Colors.grey[600],
            ),
          ),
          const SizedBox(height: 32),
          _buildProfileItem(
            icon: Icons.email,
            title: 'Email',
            subtitle: 'user@example.com',
          ),
          _buildProfileItem(
            icon: Icons.phone,
            title: 'Téléphone',
            subtitle: '+33 6 12 34 56 78',
          ),
          _buildProfileItem(
            icon: Icons.location_on,
            title: 'Adresse',
            subtitle: 'Paris, France',
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

class SettingsPage extends StatelessWidget {
  const SettingsPage({super.key});

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16.0),
      children: [
        Text(
          'Paramètres',
          style: TextStyle(
            fontSize: 24,
            fontWeight: FontWeight.bold,
            color: Colors.blue[800],
          ),
        ),
        const SizedBox(height: 16),
        _buildSettingsSection(
          title: 'Compte',
          items: [
            _buildSettingsItem(
              icon: Icons.person,
              title: 'Informations personnelles',
              onTap: () {},
            ),
            _buildSettingsItem(
              icon: Icons.security,
              title: 'Sécurité',
              onTap: () {},
            ),
          ],
        ),
        const SizedBox(height: 16),
        _buildSettingsSection(
          title: 'Préférences',
          items: [
            _buildSettingsItem(
              icon: Icons.notifications,
              title: 'Notifications',
              onTap: () {},
            ),
            _buildSettingsItem(
              icon: Icons.language,
              title: 'Langue',
              onTap: () {},
            ),
            _buildSettingsItem(
              icon: Icons.dark_mode,
              title: 'Thème',
              onTap: () {},
            ),
          ],
        ),
        const SizedBox(height: 16),
        _buildSettingsSection(
          title: 'Support',
          items: [
            _buildSettingsItem(
              icon: Icons.help,
              title: 'Aide',
              onTap: () {},
            ),
            _buildSettingsItem(
              icon: Icons.info,
              title: 'À propos',
              onTap: () {},
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildSettingsSection({required String title, required List<Widget> items}) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
          child: Text(
            title,
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: Colors.grey[700],
            ),
          ),
        ),
        Card(
          child: Column(children: items),
        ),
      ],
    );
  }

  Widget _buildSettingsItem({required IconData icon, required String title, required VoidCallback onTap}) {
    return ListTile(
      leading: Icon(icon, color: Colors.blue),
      title: Text(title),
      trailing: const Icon(Icons.arrow_forward_ios, size: 16),
      onTap: onTap,
    );
  }
}
