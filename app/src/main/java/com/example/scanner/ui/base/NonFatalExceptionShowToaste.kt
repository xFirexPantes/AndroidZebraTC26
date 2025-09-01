package com.example.scanner.ui.base

class NonFatalExceptionShowDialogMessage(override val message: String): Exception(message)
class NonFatalExceptionShowToaste(override val message: String): Exception(message)
