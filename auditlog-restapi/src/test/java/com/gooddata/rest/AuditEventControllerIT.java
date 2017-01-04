/*
 * Copyright (C) 2007-2017, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.rest;

import static com.gooddata.dto.AuditEventDTO.ADMIN_URI;
import static com.gooddata.dto.AuditEventDTO.USER_URI;
import static com.gooddata.util.EntityDTOIdMatcher.hasSameIdAs;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.gooddata.c4.domain.C4Domain;
import com.gooddata.c4.domain.DomainService;
import com.gooddata.c4.user.C4User;
import com.gooddata.c4.user.UserService;
import com.gooddata.collections.PageRequest;
import com.gooddata.dto.AuditEventsDTO;
import com.gooddata.exception.servlet.ErrorStructure;
import com.gooddata.model.AuditEvent;
import com.gooddata.repository.AuditEventRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
public class AuditEventControllerIT {

    private static final String DOMAIN = RandomStringUtils.randomAlphabetic(10);

    private static final String USER1 = RandomStringUtils.randomAlphabetic(10);
    private static final String USER2 = RandomStringUtils.randomAlphabetic(10);

    @MockBean
    private UserService userService;

    @MockBean
    private DomainService domainService;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private AuditEventRepository auditEventRepository;

    private AuditEvent event1 = new AuditEvent(DOMAIN, USER1, new DateTime());
    private AuditEvent event2 = new AuditEvent(DOMAIN, USER2, new DateTime());
    private AuditEvent event3 = new AuditEvent(DOMAIN, USER1, new DateTime());
    private AuditEvent event4 = new AuditEvent(DOMAIN, USER1, new DateTime());
    private AuditEvent event5 = new AuditEvent(DOMAIN, USER2, new DateTime());

    private C4User c4User1;
    private C4User c4User2;

    private C4Domain c4Domain;

    @Before
    public void setUp() {
        auditEventRepository.deleteAllByDomain(DOMAIN);

        auditEventRepository.save(event1);
        auditEventRepository.save(event2);
        auditEventRepository.save(event3);
        auditEventRepository.save(event3);
        auditEventRepository.save(event4);
        auditEventRepository.save(event5);

        c4User1 = mock(C4User.class);
        c4User2 = mock(C4User.class);

        doReturn("/gdc/c4/domain/" + DOMAIN).when(c4User1).getDomainUri();
        doReturn("/gdc/c4/domain/" + DOMAIN).when(c4User2).getDomainUri();

        doReturn(c4User1).when(userService).getUser(USER1);
        doReturn(c4User2).when(userService).getUser(USER2);

        c4Domain = mock(C4Domain.class);

        doReturn(c4Domain).when(domainService).getDomain(DOMAIN);

        doReturn("/gdc/c4/user/" + USER1).when(c4Domain).getOwner();
    }

    @Test
    public void testListAuditEvents() {
        ResponseEntity<AuditEventsDTO> result = testRestTemplate.exchange(ADMIN_URI, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2),  hasSameIdAs(event3), hasSameIdAs(event4), hasSameIdAs(event5)));
    }

    @Test
    public void testListAuditEventsMultiplePages() {
        String firstPageUri = new PageRequest(2).getPageUri(UriComponentsBuilder.fromUri(URI.create(ADMIN_URI))).toString();

        ResponseEntity<AuditEventsDTO> firstPage = testRestTemplate.exchange(firstPageUri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(firstPage.getBody(), containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event2)));
        String secondPageUri = firstPage.getBody().getNextPage().getPageUri(UriComponentsBuilder.newInstance()).toString();
        assertThat(secondPageUri, notNullValue());

        ResponseEntity<AuditEventsDTO> secondPage = testRestTemplate.exchange(secondPageUri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(secondPage.getBody(), containsInAnyOrder(hasSameIdAs(event3), hasSameIdAs(event4)));

        String thirdPageUri = secondPage.getBody().getNextPage().getPageUri(UriComponentsBuilder.newInstance()).toString();
        assertThat(thirdPageUri, notNullValue());

        ResponseEntity<AuditEventsDTO> thirdPage = testRestTemplate.exchange(thirdPageUri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(thirdPage, is(notNullValue()));
        assertThat(thirdPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(thirdPage.getBody(), contains(hasSameIdAs(event5)));

        String fourthPageUri = thirdPage.getBody().getNextPage().getPageUri(UriComponentsBuilder.newInstance()).toString();
        assertThat(fourthPageUri, notNullValue());
    }

    @Test
    public void testListAuditEventsNotAdmin() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-GDC-PUBLIC-USER-ID", USER2);
        HttpEntity<Object> objectHttpEntity = new HttpEntity<>(headers);

        ResponseEntity<ErrorStructure> result = testRestTemplate.exchange(ADMIN_URI, HttpMethod.GET, objectHttpEntity, ErrorStructure.class);

        assertThat(result.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void testListAuditEventsInvalidOffset() {
        String uri = new PageRequest("incorrect offset", 2).getPageUri(UriComponentsBuilder.fromUri(URI.create(ADMIN_URI))).toString();

        ResponseEntity<ErrorStructure> result = testRestTemplate.exchange(uri, HttpMethod.GET, requestWithGdcHeader(), ErrorStructure.class);

        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void testListAuditEventsForUser() {
        ResponseEntity<AuditEventsDTO> result = testRestTemplate.exchange(USER_URI, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), containsInAnyOrder(hasSameIdAs(event1), hasSameIdAs(event3), hasSameIdAs(event4)));
    }

    @Test
    public void testListAuditEventsForUserMultiplePages() {
        String firstPageUri = new PageRequest(1).getPageUri(UriComponentsBuilder.fromUri(URI.create(USER_URI))).toString();
        ResponseEntity<AuditEventsDTO> firstPage = testRestTemplate.exchange(firstPageUri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(firstPage, is(notNullValue()));
        assertThat(firstPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(firstPage.getBody(), contains(hasSameIdAs(event1)));
        String secondPageUri = firstPage.getBody().getNextPage().getPageUri(UriComponentsBuilder.newInstance()).toString();
        assertThat(secondPageUri, notNullValue());

        ResponseEntity<AuditEventsDTO> secondPage = testRestTemplate.exchange(secondPageUri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(secondPage, is(notNullValue()));
        assertThat(secondPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(secondPage.getBody(), contains(hasSameIdAs(event3)));

        String thirdPageUri = secondPage.getBody().getNextPage().getPageUri(UriComponentsBuilder.newInstance()).toString();
        assertThat(thirdPageUri, notNullValue());

        ResponseEntity<AuditEventsDTO> thirdPage = testRestTemplate.exchange(thirdPageUri, HttpMethod.GET, requestWithGdcHeader(), AuditEventsDTO.class);

        assertThat(thirdPage, is(notNullValue()));
        assertThat(thirdPage.getStatusCode(), is(HttpStatus.OK));
        assertThat(thirdPage.getBody(), contains(hasSameIdAs(event4)));

        String fourthPageUri = thirdPage.getBody().getNextPage().getPageUri(UriComponentsBuilder.newInstance()).toString();
        assertThat(fourthPageUri, notNullValue());
    }

    private HttpEntity<AuditEventsDTO> requestWithGdcHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-GDC-PUBLIC-USER-ID", USER1);
        return new HttpEntity<>(headers);
    }
}