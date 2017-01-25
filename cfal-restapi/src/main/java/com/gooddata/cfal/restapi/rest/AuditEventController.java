/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.cfal.restapi.rest;

import com.gooddata.cfal.restapi.dto.AuditEventDTO;
import com.gooddata.cfal.restapi.dto.AuditEventsDTO;
import com.gooddata.cfal.restapi.exception.UserNotSpecifiedException;
import com.gooddata.cfal.restapi.service.AuditEventService;
import com.gooddata.cfal.restapi.service.UserDomainService;
import com.gooddata.collections.PageRequest;
import com.gooddata.context.GdcCallContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static org.apache.commons.lang3.Validate.notNull;


/**
 * Audit event controller
 */
@RestController
public class AuditEventController {

    private final AuditEventService auditEventService;
    private final UserDomainService userDomainService;

    @Autowired
    public AuditEventController(final AuditEventService auditEventService,
                                final UserDomainService userDomainService) {
        this.auditEventService = notNull(auditEventService, "auditEventService cannot be null");
        this.userDomainService = notNull(userDomainService, "userDomainService cannot be null");
    }

    @RequestMapping(path = AuditEventDTO.ADMIN_URI, method = RequestMethod.GET)
    public AuditEventsDTO listAuditEvents(@ModelAttribute PageRequest pageReq) {

        final String userId = getUserIdFromContext();

        final String domainForUser = userDomainService.findDomainForUser(userId);

        userDomainService.authorizeAdmin(userId, domainForUser);

        return auditEventService.findByDomain(domainForUser, pageReq);
    }

    @RequestMapping(path = AuditEventDTO.USER_URI, method = RequestMethod.GET)
    public AuditEventsDTO listAuditEventsForUser(@ModelAttribute PageRequest pageReq) {

        final String userId = getUserIdFromContext();

        final String domainForUser = userDomainService.findDomainForUser(userId);

        return auditEventService.findByDomainAndUser(domainForUser, userId, pageReq);
    }

    private String getUserIdFromContext() {
        final GdcCallContext currentContext = GdcCallContext.getCurrentContext();
        final String userId = currentContext.getUserId();
        if (userId == null) {
            throw new UserNotSpecifiedException("User ID is not specified");
        }
        return userId;
    }
}
