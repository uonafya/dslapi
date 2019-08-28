/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.docs;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;

/**
 *
 * @author duncan
 */
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring-servlet.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class LocationApisDocumentationTest1 {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).alwaysDo(document("{method-name}",
                preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).apply(documentationConfiguration(this.restDocumentation).uris()
                .withScheme("http")
                .withHost("41.89.94.105/dsl/api")
                .withPort(80))
                .build();
    }

    @Test
    public void testWardsReturnedApiCall() throws Exception {
        this.mockMvc.perform(
                get("/wards").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-wards-returned-api-call", responseFields(
                fieldWithPath("[].id")
                        .description("Ward ID"),
                fieldWithPath("[].name")
                        .description("Ward Name"),
                fieldWithPath("[].subcountyId").description("Subcounty that ward belongs to")
        )));
    }

    @Test
    public void testFacilitylevelReturnedApiCall() throws Exception {
        this.mockMvc.perform(
                get("/facilitylevel").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-facilitylevel-returned-api-call", responseFields(
                fieldWithPath("[].id")
                        .description("Facility level ID"),
                fieldWithPath("[].name")
                        .description("Facility level Name")
        )));
    }

    @Test
    public void testFacilitiesByFacilityLevelIdReturned() throws Exception {
        this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/facilitylevel/{facilityLevelId}", 3).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-facilities-by-facility-level-id-returned", pathParameters(
                parameterWithName("facilityLevelId").description("The facility level id from which to return facilities in that category")
        ), responseFields(
                fieldWithPath("[].id")
                        .description("Facility ID"),
                fieldWithPath("[].name")
                        .description("Facility Name"),
                fieldWithPath("[].wardId").description("Ward Id that facility belongs to")
        )
        ));
    }

    @Test
    public void testFacilitytypeReturnedApiCall() throws Exception {
        this.mockMvc.perform(
                get("/facilitytype").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-facilitytype-returned-api-call", responseFields(
                fieldWithPath("[].id")
                        .description("Facility type ID"),
                fieldWithPath("[].name")
                        .description("Facility type Name")
        )));
    }

    @Test
    public void testFacilitiesByTypeIdReturned() throws Exception {
        this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/facilitytype/{facilityTypeId}", 1).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-facilities-by-facility-type-id-returned", pathParameters(
                parameterWithName("facilityTypeId").description("The facility type id from which to return facilities in that category")
        ), responseFields(
                fieldWithPath("[].id")
                        .description("Facility ID"),
                fieldWithPath("[].name")
                        .description("Facility Name"),
                fieldWithPath("[].wardId").description("Ward Id that facility belongs to")
        )
        ));
    }

    @Test
    public void testFacilityRegulatingBodyReturnedApiCall() throws Exception {
        this.mockMvc.perform(
                get("/facilityregulatingbody").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-facility-regulating-body-returned-api-call", responseFields(
                fieldWithPath("[].id")
                        .description("Facility regulating body ID"),
                fieldWithPath("[].name")
                        .description("Facility regulating body Name")
        )));
    }

    @Test
    public void testFacilitiesByRegulatingBodyIdReturned() throws Exception {
        this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/facilityregulatingbody/{regulatingBodyId}", 1).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-facilities-by-regulating-body-id-returned", pathParameters(
                parameterWithName("regulatingBodyId").description("The facility regulating body id from which to return facilities in that category")
        ), responseFields(
                fieldWithPath("[].id")
                        .description("Facility ID"),
                fieldWithPath("[].name")
                        .description("Facility Name"),
                fieldWithPath("[].wardId").description("Ward Id that facility belongs to")
        )
        ));
    }

    @Test
    public void testFacilityOwnerTypeReturnedApiCall() throws Exception {
        this.mockMvc.perform(
                get("/facilityownertype").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-facility-owner-type-returned-api-call", responseFields(
                fieldWithPath("[].id")
                        .description("Facility owner type ID"),
                fieldWithPath("[].name")
                        .description("Facility owner type Name")
        )));
    }

    @Test
    public void testFacilitiesByOwnerTypeIdReturned() throws Exception {
        this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/facilityownertype/{onwerTypeId}", 1).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-facilities-by-owner-type-id-returned", pathParameters(
                parameterWithName("onwerTypeId").description("The facility owner type id from which to return facilities in that category")
        ), responseFields(
                fieldWithPath("[].id")
                        .description("Facility ID"),
                fieldWithPath("[].name")
                        .description("Facility Name"),
                fieldWithPath("[].wardId").description("Ward Id that facility belongs to")
        )
        ));
    }

}
