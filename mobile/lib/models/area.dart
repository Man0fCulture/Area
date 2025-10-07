class Area {
  final String id;
  final String userId;
  final String name;
  final String? description;
  final bool active;
  final AreaAction action;
  final List<AreaReaction> reactions;
  final int executionCount;
  final int? lastTriggeredAt;
  final int createdAt;
  final int updatedAt;

  Area({
    required this.id,
    required this.userId,
    required this.name,
    this.description,
    required this.active,
    required this.action,
    required this.reactions,
    required this.executionCount,
    this.lastTriggeredAt,
    required this.createdAt,
    required this.updatedAt,
  });

  factory Area.fromJson(Map<String, dynamic> json) {
    return Area(
      id: json['id'],
      userId: json['userId'],
      name: json['name'],
      description: json['description'],
      active: json['active'] ?? false,
      action: AreaAction.fromJson(json['action']),
      reactions: (json['reactions'] as List)
          .map((r) => AreaReaction.fromJson(r))
          .toList(),
      executionCount: json['executionCount'] ?? 0,
      lastTriggeredAt: json['lastTriggeredAt'],
      createdAt: json['createdAt'],
      updatedAt: json['updatedAt'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'userId': userId,
      'name': name,
      'description': description,
      'active': active,
      'action': action.toJson(),
      'reactions': reactions.map((r) => r.toJson()).toList(),
      'executionCount': executionCount,
      'lastTriggeredAt': lastTriggeredAt,
      'createdAt': createdAt,
      'updatedAt': updatedAt,
    };
  }
}

class AreaAction {
  final String serviceId;
  final String actionId;
  final Map<String, dynamic> config;

  AreaAction({
    required this.serviceId,
    required this.actionId,
    required this.config,
  });

  factory AreaAction.fromJson(Map<String, dynamic> json) {
    return AreaAction(
      serviceId: json['serviceId'],
      actionId: json['actionId'],
      config: Map<String, dynamic>.from(json['config']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'serviceId': serviceId,
      'actionId': actionId,
      'config': config,
    };
  }
}

class AreaReaction {
  final String serviceId;
  final String reactionId;
  final Map<String, dynamic> config;

  AreaReaction({
    required this.serviceId,
    required this.reactionId,
    required this.config,
  });

  factory AreaReaction.fromJson(Map<String, dynamic> json) {
    return AreaReaction(
      serviceId: json['serviceId'],
      reactionId: json['reactionId'],
      config: Map<String, dynamic>.from(json['config']),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'serviceId': serviceId,
      'reactionId': reactionId,
      'config': config,
    };
  }
}
