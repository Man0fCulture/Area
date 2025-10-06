class Service {
  final String id;
  final String name;
  final String displayName;
  final String description;
  final String category;
  final bool requiresAuth;
  final bool enabled;
  final List<ActionItem> actions;
  final List<ReactionItem> reactions;

  Service({
    required this.id,
    required this.name,
    required this.displayName,
    required this.description,
    required this.category,
    required this.requiresAuth,
    required this.enabled,
    required this.actions,
    required this.reactions,
  });

  factory Service.fromJson(Map<String, dynamic> json) {
    return Service(
      id: json['id'],
      name: json['name'],
      displayName: json['displayName'],
      description: json['description'],
      category: json['category'],
      requiresAuth: json['requiresAuth'] ?? false,
      enabled: json['enabled'] ?? true,
      actions: (json['actions'] as List? ?? [])
          .map((a) => ActionItem.fromJson(a))
          .toList(),
      reactions: (json['reactions'] as List? ?? [])
          .map((r) => ReactionItem.fromJson(r))
          .toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'displayName': displayName,
      'description': description,
      'category': category,
      'requiresAuth': requiresAuth,
      'enabled': enabled,
      'actions': actions.map((a) => a.toJson()).toList(),
      'reactions': reactions.map((r) => r.toJson()).toList(),
    };
  }
}

class ActionItem {
  final String id;
  final String name;
  final String description;
  final List<Parameter> parameters;

  ActionItem({
    required this.id,
    required this.name,
    required this.description,
    required this.parameters,
  });

  factory ActionItem.fromJson(Map<String, dynamic> json) {
    return ActionItem(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      parameters: (json['parameters'] as List? ?? [])
          .map((p) => Parameter.fromJson(p))
          .toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'description': description,
      'parameters': parameters.map((p) => p.toJson()).toList(),
    };
  }
}

class ReactionItem {
  final String id;
  final String name;
  final String description;
  final List<Parameter> parameters;

  ReactionItem({
    required this.id,
    required this.name,
    required this.description,
    required this.parameters,
  });

  factory ReactionItem.fromJson(Map<String, dynamic> json) {
    return ReactionItem(
      id: json['id'],
      name: json['name'],
      description: json['description'],
      parameters: (json['parameters'] as List? ?? [])
          .map((p) => Parameter.fromJson(p))
          .toList(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'description': description,
      'parameters': parameters.map((p) => p.toJson()).toList(),
    };
  }
}

class Parameter {
  final String name;
  final String type;
  final bool required;
  final String description;

  Parameter({
    required this.name,
    required this.type,
    required this.required,
    required this.description,
  });

  factory Parameter.fromJson(Map<String, dynamic> json) {
    return Parameter(
      name: json['name'],
      type: json['type'],
      required: json['required'] ?? false,
      description: json['description'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'type': type,
      'required': required,
      'description': description,
    };
  }
}
