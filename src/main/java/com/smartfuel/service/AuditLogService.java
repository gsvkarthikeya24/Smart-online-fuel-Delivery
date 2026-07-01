package com.smartfuel.service;

import com.smartfuel.entity.AuditLog;
import com.smartfuel.entity.User;
import com.smartfuel.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void log(User user, String action, String details) {
        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc();
    }
}
