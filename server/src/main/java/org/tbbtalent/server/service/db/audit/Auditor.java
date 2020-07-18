package org.tbbtalent.server.service.db.audit;


import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.tbbtalent.server.model.db.AbstractDomainObject;
import org.tbbtalent.server.model.db.AuditLog;
import org.tbbtalent.server.model.db.User;
import org.tbbtalent.server.repository.db.AuditLogRepository;
import org.tbbtalent.server.security.UserContext;

@Aspect
@Component
public class Auditor {
    
    private static final Logger log = LoggerFactory.getLogger(Auditor.class);

    private final AuditLogRepository auditLogRepository;
    private final UserContext userContext;
    public static final Long ANONYMOUS_USER_ID = -999L;


    public Auditor(AuditLogRepository auditLogRepository, UserContext userContext) {
        this.auditLogRepository = auditLogRepository;
        this.userContext = userContext;
    }

    @AfterReturning(
            pointcut="@annotation(audit) && args(inputValue,..)",
            returning="returnValue")
    public void handleAuditLog(Audit audit, Object inputValue, Object returnValue)
    {
        try {
            String objectRef = null;
            String description = audit.extraInfo();
            if (returnValue instanceof AbstractDomainObject)
            {
                objectRef = ((AbstractDomainObject)returnValue).getId().toString();
            }
            else if (inputValue != null && ClassUtils.isPrimitiveOrWrapper(inputValue.getClass()))
            {
                objectRef = inputValue.toString();
            }
            else if (returnValue instanceof List)
            {
                objectRef = (String) ((List)returnValue).stream()
                        .map(item -> (item instanceof AbstractDomainObject) ? ((AbstractDomainObject)item).getId().toString() : "")
                        .collect(Collectors.joining(","));
            }
            else if (inputValue != null && inputValue instanceof AbstractDomainObject) {
                objectRef = ((AbstractDomainObject)inputValue).getId().toString();
            }
            
            // error prevention
            if (objectRef == null)
            {
                log.warn("unable to create audit log entry for: audit=" + audit + ", inputValue=" + inputValue + ", returnValue=" + returnValue);
                return;
            }
            
            
            User user = userContext.getLoggedInUser();
            String auditName = "Anonymous";
            Long userId = ANONYMOUS_USER_ID;
            if (user != null)
            {
                auditName = user.getDisplayName();
                userId = user.getId();
            }
            StringBuffer sb = new StringBuffer(auditName);
            AuditType auditType = audit.type();
            AuditAction auditAction = audit.action();
            sb.append(" ").append(auditAction.getName()).append(" ").append(auditType.getName());
            if (objectRef != null)
            {
                sb.append(" with id ").append(objectRef);
            }
            if (StringUtils.isNotBlank(description))
            {
                sb.append(" - ");
                
                boolean foundParams = false;
                Pattern patt = Pattern.compile("(\\{.*?\\})");
                Matcher m = patt.matcher(description);
                while (m.find()) {
                    foundParams = true;
                    String placeholder = m.group(1);
                    String sourceObjectType = "return";
                    String propertyName = null;
                    placeholder = placeholder.substring(1, placeholder.length() - 1);
                    String[] values = StringUtils.split(placeholder, '.');
                    if (values.length > 1) {
                        sourceObjectType = values[0];
                        propertyName = values[1];
                    }
                    Object sourceObject = returnValue;
                    if ("input".equals(sourceObjectType) && inputValue != null) {
                        sourceObject = inputValue;
                    }
                    String property = (propertyName != null ? BeanUtils.getProperty(sourceObject, propertyName) : sourceObject.toString());
                    m.appendReplacement(sb, Matcher.quoteReplacement(property));
                }
                if (foundParams)
                {
                    m.appendTail(sb);
                }
                else
                {
                    sb.append(description);
                }
            }
            String logDescription = sb.toString();
            AuditLog auditLog = new AuditLog(OffsetDateTime.now(), userId, auditType, auditAction, objectRef, logDescription);
            auditLogRepository.save(auditLog);


        } catch (Exception e) {
            log.warn("unable to log audit event '" + audit + "' for input '" + inputValue + "' and return '" + returnValue + "'", e);
        }
    }
    
//    public static void main(String[] args) {
//        String text = "test me {return.test} and dont do {blah} anything else";
//        Pattern patt = Pattern.compile("(\\{.*?\\})");
//        Matcher m = patt.matcher(text);
//        StringBuffer sb = new StringBuffer(text.length());
//        if (m.matches()) System.out.println("match found");
//        while (m.find()) {
//          String found = m.group(1);
//          System.out.println("found: " + found);
//          m.appendReplacement(sb, Matcher.quoteReplacement("XXXX"));
//        }
//        m.appendTail(sb);
//        System.out.println(sb.toString());
//    }
    
}
