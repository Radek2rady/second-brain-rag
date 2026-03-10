package org.example.secondbrainrag.infrastructure.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.example.secondbrainrag.application.AuditService
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler(private val auditService: AuditService) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        val auth = SecurityContextHolder.getContext().authentication
        val username = auth?.name ?: "anonymous"
        // In our case tenantId is same as username
        val tenantId = username 

        auditService.logAction(
            username = username,
            tenantId = tenantId,
            action = "ACCESS_DENIED - ${request.method} ${request.requestURI}",
            details = "User $username attempted unauthorized access. Original error: ${accessDeniedException.message}",
            status = "DENIED"
        )

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied")
    }
}
