class ApiError {
  final String error;
  final String message;

  ApiError({
    required this.error,
    required this.message,
  });

  factory ApiError.fromJson(Map<String, dynamic> json) {
    return ApiError(
      error: json['error'] as String,
      message: json['message'] as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'error': error,
      'message': message,
    };
  }
}
