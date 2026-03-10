package org.example.secondbrainrag.infrastructure.audit

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuditEventRepository : JpaRepository<AuditEventJpaEntity, String>
