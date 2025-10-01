package com.example.domain.exceptions

sealed class DomainException(val code: Int, message: String) : Exception(message) {

    class UnknownException(message: String = "Unknown error")
        : DomainException(0, message)

    class UserNotFoundException(message: String = "User not found")
        : DomainException(1, message)

    class MessageSendFailedException(message: String = "Failed to send message")
        : DomainException(2, message)

    class MessageRetryFailedException(message: String = "Failed to retry message")
        : DomainException(3, message)
    class MediaLimitExceededException(message: String = "Cannot send more than 10 media items")
        : DomainException(4, message)

    class EmptyMessageException(message: String = "Message content is empty")
        : DomainException(5, message)

    class NetworkException(message: String = "Network error")
        : DomainException(6, message)

    class PermissionDeniedException(message: String = "Permission denied")
        : DomainException(7, message)
}