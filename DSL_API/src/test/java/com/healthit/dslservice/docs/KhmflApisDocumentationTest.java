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
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;

/**
 *
 * @author duncan
 */
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring-servlet.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class KhmflApisDocumentationTest {

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
                .withHost("servername/dsl/api")
                .withPort(80))
                .build();
    }

    @Test
    public void testFacilitiesReturnedApiCall() throws Exception {
        this.mockMvc.perform(
                get("/facilities").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-facilities-returned-api-call", responseFields(
                fieldWithPath("[].id")
                        .description("Facility ID"),
                fieldWithPath("[].name")
                        .description("Facility Name"),
                fieldWithPath("[].parentid").description("Parent Id (ward) that facility belongs to"),
                fieldWithPath("[].level").description("Level in the org unit hierarchy")
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
                fieldWithPath("[].parentid").description("Parent Id (ward) that facility belongs to"),
                fieldWithPath("[].level").description("Level in the org unit hierarchy")
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
                fieldWithPath("[].parentid").description("Parent Id (ward) that facility belongs to"),
                fieldWithPath("[].level").description("Level in the org unit hierarchy")

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
                fieldWithPath("[].parentid").description("Parent Id (ward) that facility belongs to"),
                fieldWithPath("[].level").description("Level in the org unit hierarchy")

                
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
                fieldWithPath("[].parentid").description("Parent Id (ward) that facility belongs to"),
                fieldWithPath("[].level").description("Level in the org unit hierarchy")

        )
        ));
    }
    
    
    @Test
    public void testFacilitiesGetCotsCountReturned() throws Exception {
        this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/resource/cots?&ouid=23506;32;13613637").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-facilities-get-cots-count-returned", requestParameters(
                parameterWithName("ouid").description("Single org unit id or semi-colon seperated organisation unit id, if not provided defaults to national")
        ), responseFields(
                fieldWithPath("[].id")
                        .description("Organisation unit ID"),
                fieldWithPath("[].name")
                        .description("Organisation unit Name"),
                fieldWithPath("[].parentid").description("Parent Ids' or this organisation unit"),
                fieldWithPath("[].level").description("Level in the org unit hierarchy"),
                fieldWithPath("[].count").description("Number of cots in this org unit")

        )
        ));
    }
    
    @Test
    public void testFacilitiesGetBedsCountReturned() throws Exception {
        this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/resource/beds?&ouid=23506;32;13613637").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-facilities-get-beds-count-returned", requestParameters(
                parameterWithName("ouid").description("Single org unit id or semi-colon seperated organisation unit id, if not provided defaults to national")
        ), responseFields(
                fieldWithPath("[].id")
                        .description("Organisation unit ID"),
                fieldWithPath("[].name")
                        .description("Organisation unit Name"),
                fieldWithPath("[].parentid").description("Parent Ids' or this organisation unit"),
                fieldWithPath("[].level").description("Level in the org unit hierarchy"),
                fieldWithPath("[].count").description("Number of beds in this org unit")

        )
        ));
    }

}
