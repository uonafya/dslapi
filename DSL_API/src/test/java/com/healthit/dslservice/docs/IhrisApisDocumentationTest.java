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
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import org.springframework.restdocs.payload.JsonFieldType;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;

/**
 *
 * @author duncan
 */
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring-servlet.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class IhrisApisDocumentationTest {

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
    public void testCadresReturnedApiCall() throws Exception {
        this.mockMvc.perform(
                get("/cadres").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-cadres-returned-api-call", responseFields(
                fieldWithPath("[].id")
                        .description("Cadre ID"),
                fieldWithPath("[].name")
                        .description("Cadre Name"),
                fieldWithPath("[].cadreGroupId").description("Cadre Group Id"))));
    }

    @Test
    public void testCadresValuesReturnedWithRequestParameters() throws Exception {
        String periodDec = "Period parameter. Can be an explicit year, YYYY (eg 2018) which will give the stated year values, or YYYYmm (eg 201801) which gives "
                + "cadre count upto that given month";
        this.mockMvc.perform(
                get("/cadres?pe=2017&ouid=18&id=33").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-cadres-values-returned-with-request-parameters", requestParameters(
                parameterWithName("pe").description(periodDec),
                parameterWithName("ouid").description("Organisation unit id, if not provided defaults to national"),
                parameterWithName("id").description("Cadre ID, if not provided gives count for all cadres")
        ), responseFields(
                fieldWithPath("[].cadre")
                        .description("Cadre name"),
                fieldWithPath("[].cadreCount")
                        .description("Cadre count"),
                fieldWithPath("[].id").description("Cadre Id")
        )
        ));
    }

    @Test
    public void testCadresValuesReturnedWithPathAndRequestParameters() throws Exception {
        String periodDec = "Period parameter. Can be an explicit year, YYYY (eg 2018) which will give the stated year values, or YYYYmm (eg 201801) which gives "
                + "cadre count upto that given month";
        this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/cadres/{id}?pe=2017&ouid=23408", 33).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-cadres-values-returned-with-path-and-request-parameters", pathParameters(
                parameterWithName("id").description("Cadre ID, if not provided gives count for all cadres")
        ), requestParameters(
                parameterWithName("pe").description(periodDec),
                parameterWithName("ouid").description("Organisation unit id, if not provided defaults to national")
        ), responseFields(
                fieldWithPath("[].cadre")
                        .description("Cadre name"),
                fieldWithPath("[].cadreCount")
                        .description("Cadre count"),
                fieldWithPath("[].id").description("Cadre Id")
        )
        ));
    }
    
    
    
    
    @Test
    public void testCadresGroupValuesReturnedWithRequestParameters() throws Exception {
        String periodDec = "Period parameter. Can be an explicit year, YYYY (eg 2018) which will give the stated year values, or YYYYmm (eg 201801) which gives "
                + "cadre count upto that given month";
        this.mockMvc.perform(
                get("/cadregroups?pe=2017&ouid=18&id=2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-cadres-group-values-returned-with-request-parameters", requestParameters(
                parameterWithName("pe").description(periodDec),
                parameterWithName("ouid").description("Organisation unit id, if not provided defaults to national"),
                parameterWithName("id").description("Cadre group ID, if not provided gives count for all cadre groups")
        ), responseFields(
                fieldWithPath("[].cadre")
                        .description("Cadre group name"),
                fieldWithPath("[].cadreCount")
                        .description("Cadre count"),
                fieldWithPath("[].id").description("Cadre group Id")
        )
        ));
    }

    @Test
    public void testCadreGroupsReturnedApiCall() throws Exception {
        this.mockMvc.perform(
                get("/cadregroups").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-cadre-groups-returned-api-call", responseFields(
                fieldWithPath("[].id")
                        .description("Cadre group ID"),
                fieldWithPath("[].name")
                        .description("Cadre group Name"))));
    }

    @Test
    public void testCadresByGroupIdReturned() throws Exception {
        this.mockMvc.perform(
                get("/cadres?groupId=4").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-cadres-by-group-id-returned", requestParameters(
                parameterWithName("groupId").description("The cadre group id from which to return its cadres")
        ), responseFields(
                fieldWithPath("[].id")
                        .description("Cadre ID"),
                fieldWithPath("[].name")
                        .description("Cadre Name"),
                fieldWithPath("[].cadreGroupId")
                        .description("Cadre group id")
        )
        ));
    }

}
