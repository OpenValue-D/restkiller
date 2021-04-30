package de.openvalue.restkiller.web

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler


@ControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [IllegalStateException::class])
    protected fun handleConflict(ex: RuntimeException?, request: WebRequest?) =
        handleExceptionInternal(ex!!, ex.message, HttpHeaders(), CONFLICT, request!!)

    @ExceptionHandler(value = [IllegalArgumentException::class])
    protected fun handleBadRequest(ex: RuntimeException?, request: WebRequest?) =
        handleExceptionInternal(ex!!, ex.message, HttpHeaders(), UNPROCESSABLE_ENTITY, request!!)

    @ExceptionHandler(value = [ResponseStatusException::class])
    protected fun handleStatusException(ex: RuntimeException?, request: WebRequest?): ResponseEntity<Any?>? {
        ex as ResponseStatusException
        return handleExceptionInternal(ex, ex.reason, HttpHeaders(), ex.status, request!!)
    }

}
