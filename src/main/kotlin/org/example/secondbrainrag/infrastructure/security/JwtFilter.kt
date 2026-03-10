package org.example.secondbrainrag.infrastructure.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import org.slf4j.LoggerFactory

class JwtFilter(private val tokenProvider: TokenProvider) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtFilter::class.java)

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val jwt = resolveToken(request)
        if (StringUtils.hasText(jwt)) {
            if (tokenProvider.validateToken(jwt!!)) {
                val username = tokenProvider.getUsername(jwt)
                val roles = tokenProvider.getRoles(jwt)
                val authorities = roles.map { SimpleGrantedAuthority(it) }
                val auth = UsernamePasswordAuthenticationToken(username, null, authorities)
                SecurityContextHolder.getContext().authentication = auth
                logger.debug("Nastaven kontext autentizace pro uživatele: $username")
            } else {
                logger.warn("Neplatný nebo expirovaný JWT token: $jwt")
            }
        } else {
            logger.debug("Žádný JWT token nenalezen v požadavku na ${request.requestURI}")
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        return null
    }
}
