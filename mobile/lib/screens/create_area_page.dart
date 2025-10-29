import 'package:flutter/material.dart';
import '../models/area.dart';
import '../models/service.dart';
import '../services/area_service.dart';

extension FirstWhereOrNull<T> on List<T> {
  T? firstWhereOrNull(bool Function(T) test) {
    try {
      return firstWhere(test);
    } catch (e) {
      return null;
    }
  }
}

class CreateAreaPage extends StatefulWidget {
  final Area? existingArea; 

  const CreateAreaPage({super.key, this.existingArea});

  @override
  State<CreateAreaPage> createState() => _CreateAreaPageState();
}

class _CreateAreaPageState extends State<CreateAreaPage> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _descriptionController = TextEditingController();
  final AreaService _areaService = AreaService();

  List<Service> _services = [];
  bool _isLoadingServices = true;
  bool _isSaving = false;

  Service? _selectedActionService;
  ActionItem? _selectedAction;
  Service? _selectedReactionService;
  ReactionItem? _selectedReaction;

  final Map<String, TextEditingController> _actionConfigControllers = {};
  final Map<String, TextEditingController> _reactionConfigControllers = {};

  bool get _isEditing => widget.existingArea != null;

  @override
  void initState() {
    super.initState();
    _loadServices();
    if (_isEditing) {
      _loadExistingArea();
    }
  }

  Future<void> _loadExistingArea() async {
    final area = widget.existingArea!;
    _nameController.text = area.name;
    _descriptionController.text = area.description ?? '';

    await Future.delayed(const Duration(milliseconds: 100));

    if (mounted) {
      setState(() {
        _selectedActionService = _services.firstWhereOrNull(
          (s) => s.id == area.action.serviceId,
        );

        if (_selectedActionService != null) {
          _selectedAction = _selectedActionService!.actions.firstWhereOrNull(
            (a) => a.id == area.action.actionId,
          );

          if (_selectedAction != null) {
            _actionConfigControllers.clear();
            for (var param in _selectedAction!.parameters) {
              final controller = TextEditingController();
              final value = area.action.config[param.name];
              if (value != null) {
                controller.text = value.toString();
              }
              _actionConfigControllers[param.name] = controller;
            }
          }
        }

        if (area.reactions.isNotEmpty) {
          final firstReaction = area.reactions.first;
          _selectedReactionService = _services.firstWhereOrNull(
            (s) => s.id == firstReaction.serviceId,
          );

          if (_selectedReactionService != null) {
            _selectedReaction = _selectedReactionService!.reactions.firstWhereOrNull(
              (r) => r.id == firstReaction.reactionId,
            );

            if (_selectedReaction != null) {
              _reactionConfigControllers.clear();
              for (var param in _selectedReaction!.parameters) {
                final controller = TextEditingController();
                final value = firstReaction.config[param.name];
                if (value != null) {
                  controller.text = value.toString();
                }
                _reactionConfigControllers[param.name] = controller;
              }
            }
          }
        }
      });
    }
  }

  @override
  void dispose() {
    _nameController.dispose();
    _descriptionController.dispose();
    for (var controller in _actionConfigControllers.values) {
      controller.dispose();
    }
    for (var controller in _reactionConfigControllers.values) {
      controller.dispose();
    }
    super.dispose();
  }

  Future<void> _loadServices() async {
    try {
      final services = await _areaService.getServices();
      setState(() {
        _services = services;
        _isLoadingServices = false;
      });
    } catch (e) {
      setState(() {
        _isLoadingServices = false;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Erreur de chargement des services: ${e.toString()}'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  Future<void> _saveArea() async {
    if (!_formKey.currentState!.validate()) return;
    if (_selectedAction == null || _selectedReaction == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Veuillez sélectionner une action et une réaction'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    setState(() {
      _isSaving = true;
    });

    try {
      final actionConfig = <String, dynamic>{};
      for (var param in _selectedAction!.parameters) {
        final controller = _actionConfigControllers[param.name];
        if (controller != null) {
          actionConfig[param.name] = controller.text;
        }
      }

      final reactionConfig = <String, dynamic>{};
      for (var param in _selectedReaction!.parameters) {
        final controller = _reactionConfigControllers[param.name];
        if (controller != null) {
          reactionConfig[param.name] = controller.text;
        }
      }

      if (_isEditing) {
        await _areaService.updateArea(
          id: widget.existingArea!.id,
          name: _nameController.text.trim(),
          description: _descriptionController.text.trim().isNotEmpty
              ? _descriptionController.text.trim()
              : null,
          actionServiceId: _selectedActionService!.id,
          actionId: _selectedAction!.id,
          actionConfig: actionConfig,
          reactions: [
            {
              'serviceId': _selectedReactionService!.id,
              'reactionId': _selectedReaction!.id,
              'config': reactionConfig,
            }
          ],
        );

        if (mounted) {
          Navigator.pop(context, true);
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('AREA modifiée avec succès'),
              backgroundColor: Colors.green,
            ),
          );
        }
      } else {
        await _areaService.createArea(
          name: _nameController.text.trim(),
          description: _descriptionController.text.trim().isNotEmpty
              ? _descriptionController.text.trim()
              : null,
          actionServiceId: _selectedActionService!.id,
          actionId: _selectedAction!.id,
          actionConfig: actionConfig,
          reactions: [
            {
              'serviceId': _selectedReactionService!.id,
              'reactionId': _selectedReaction!.id,
              'config': reactionConfig,
            }
          ],
        );

        if (mounted) {
          Navigator.pop(context, true);
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('AREA créée avec succès'),
              backgroundColor: Colors.green,
            ),
          );
        }
      }
    } catch (e) {
      setState(() {
        _isSaving = false;
      });
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Erreur: ${e.toString()}'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(_isEditing ? 'Modifier l\'AREA' : 'Créer une AREA'),
      ),
      body: _isLoadingServices
          ? const Center(child: CircularProgressIndicator())
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text(
                      'Informations générales',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                        color: Colors.blue[800],
                      ),
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _nameController,
                      decoration: const InputDecoration(
                        labelText: 'Nom de l\'AREA',
                        border: OutlineInputBorder(),
                      ),
                      validator: (value) {
                        if (value == null || value.isEmpty) {
                          return 'Veuillez entrer un nom';
                        }
                        return null;
                      },
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _descriptionController,
                      decoration: const InputDecoration(
                        labelText: 'Description (optionnel)',
                        border: OutlineInputBorder(),
                      ),
                      maxLines: 3,
                    ),
                    const SizedBox(height: 32),
                    Text(
                      'Action (Déclencheur)',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                        color: Colors.blue[800],
                      ),
                    ),
                    const SizedBox(height: 16),
                    DropdownButtonFormField<Service>(
                      value: _selectedActionService,
                      decoration: const InputDecoration(
                        labelText: 'Service',
                        border: OutlineInputBorder(),
                      ),
                      items: _services
                          .where((s) => s.actions.isNotEmpty)
                          .map((service) => DropdownMenuItem(
                                value: service,
                                child: Text(service.displayName),
                              ))
                          .toList(),
                      onChanged: (value) {
                        setState(() {
                          _selectedActionService = value;
                          _selectedAction = null;
                          _actionConfigControllers.clear();
                        });
                      },
                    ),
                    if (_selectedActionService != null) ...[
                      const SizedBox(height: 16),
                      DropdownButtonFormField<ActionItem>(
                        value: _selectedAction,
                        decoration: const InputDecoration(
                          labelText: 'Action',
                          border: OutlineInputBorder(),
                        ),
                        items: _selectedActionService!.actions
                            .map((action) => DropdownMenuItem(
                                  value: action,
                                  child: Text(action.name),
                                ))
                            .toList(),
                        onChanged: (value) {
                          setState(() {
                            _selectedAction = value;
                            _actionConfigControllers.clear();
                            if (value != null) {
                              for (var param in value.parameters) {
                                _actionConfigControllers[param.name] =
                                    TextEditingController();
                              }
                            }
                          });
                        },
                      ),
                    ],
                    if (_selectedAction != null &&
                        _selectedAction!.parameters.isNotEmpty) ...[
                      const SizedBox(height: 16),
                      Text(
                        'Configuration de l\'action',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.w600,
                          color: Colors.grey[700],
                        ),
                      ),
                      const SizedBox(height: 8),
                      ..._selectedAction!.parameters.map((param) {
                        return Padding(
                          padding: const EdgeInsets.only(bottom: 16),
                          child: TextFormField(
                            controller: _actionConfigControllers[param.name],
                            decoration: InputDecoration(
                              labelText: param.name,
                              hintText: param.description,
                              border: const OutlineInputBorder(),
                            ),
                            validator: param.required
                                ? (value) {
                                    if (value == null || value.isEmpty) {
                                      return 'Ce champ est requis';
                                    }
                                    return null;
                                  }
                                : null,
                          ),
                        );
                      }),
                    ],
                    const SizedBox(height: 32),
                    Text(
                      'Réaction',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                        color: Colors.green[800],
                      ),
                    ),
                    const SizedBox(height: 16),
                    DropdownButtonFormField<Service>(
                      value: _selectedReactionService,
                      decoration: const InputDecoration(
                        labelText: 'Service',
                        border: OutlineInputBorder(),
                      ),
                      items: _services
                          .where((s) => s.reactions.isNotEmpty)
                          .map((service) => DropdownMenuItem(
                                value: service,
                                child: Text(service.displayName),
                              ))
                          .toList(),
                      onChanged: (value) {
                        setState(() {
                          _selectedReactionService = value;
                          _selectedReaction = null;
                          _reactionConfigControllers.clear();
                        });
                      },
                    ),
                    if (_selectedReactionService != null) ...[
                      const SizedBox(height: 16),
                      DropdownButtonFormField<ReactionItem>(
                        value: _selectedReaction,
                        decoration: const InputDecoration(
                          labelText: 'Réaction',
                          border: OutlineInputBorder(),
                        ),
                        items: _selectedReactionService!.reactions
                            .map((reaction) => DropdownMenuItem(
                                  value: reaction,
                                  child: Text(reaction.name),
                                ))
                            .toList(),
                        onChanged: (value) {
                          setState(() {
                            _selectedReaction = value;
                            _reactionConfigControllers.clear();
                            if (value != null) {
                              for (var param in value.parameters) {
                                _reactionConfigControllers[param.name] =
                                    TextEditingController();
                              }
                            }
                          });
                        },
                      ),
                    ],
                    if (_selectedReaction != null &&
                        _selectedReaction!.parameters.isNotEmpty) ...[
                      const SizedBox(height: 16),
                      Text(
                        'Configuration de la réaction',
                        style: TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.w600,
                          color: Colors.grey[700],
                        ),
                      ),
                      const SizedBox(height: 8),
                      ..._selectedReaction!.parameters.map((param) {
                        return Padding(
                          padding: const EdgeInsets.only(bottom: 16),
                          child: TextFormField(
                            controller: _reactionConfigControllers[param.name],
                            decoration: InputDecoration(
                              labelText: param.name,
                              hintText: param.description,
                              border: const OutlineInputBorder(),
                            ),
                            validator: param.required
                                ? (value) {
                                    if (value == null || value.isEmpty) {
                                      return 'Ce champ est requis';
                                    }
                                    return null;
                                  }
                                : null,
                          ),
                        );
                      }),
                    ],
                    const SizedBox(height: 32),
                    ElevatedButton(
                      onPressed: _isSaving ? null : _saveArea,
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 16),
                      ),
                      child: _isSaving
                          ? const SizedBox(
                              height: 20,
                              width: 20,
                              child: CircularProgressIndicator(
                                color: Colors.white,
                                strokeWidth: 2,
                              ),
                            )
                          : Text(
                              _isEditing ? 'Modifier l\'AREA' : 'Créer l\'AREA',
                              style: const TextStyle(fontSize: 16),
                            ),
                    ),
                  ],
                ),
              ),
            ),
    );
  }
}
