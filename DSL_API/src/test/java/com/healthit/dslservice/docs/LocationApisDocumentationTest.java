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
public class LocationApisDocumentationTest {

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
    public void testWardsReturnedApiCall() throws Exception {
        this.mockMvc.perform(
                get("/wards").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-wards-returned-api-call", responseFields(
                fieldWithPath("[].id")
                        .description("Ward ID"),
                fieldWithPath("[].name")
                        .description("Ward Name"),
                fieldWithPath("[].level")
                        .description("This org unit level"),
                fieldWithPath("[].parentid").description("Parent org unit (Subcounty) that ward belongs to")
        )));
    }

    @Test
    public void testSubcountiesReturnedApiCall() throws Exception {
        this.mockMvc.perform(
                get("/subcounties").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-subcounties-returned-api-call", responseFields(
                fieldWithPath("[].id")
                        .description("sub countiy ID"),
                fieldWithPath("[].name")
                        .description("sub countiy Name"),
                fieldWithPath("[].level")
                        .description("This org unit level"),
                fieldWithPath("[].parentid").description("Parent id (county) sub-county belongs to")
        )));
    }

    @Test
    public void testCountiesReturnedApiCall() throws Exception {
        this.mockMvc.perform(
                get("/counties").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-counties-returned-api-call", responseFields(
                fieldWithPath("[].id")
                        .description("County ID"),
                fieldWithPath("[].name")
                        .description("County Name"),
                fieldWithPath("[].level")
                        .description("This org unit level"),
                fieldWithPath("[].parentid")
                        .description("Parent id Name (National)")
                        
        )));
    }

}
