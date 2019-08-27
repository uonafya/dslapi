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
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *
 * @author duncan
 */
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring-servlet.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class DhisApisDocumentationTest {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).alwaysDo(document("{method-name}",
                preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()))).apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @Test
    public void indicators() throws Exception {
        this.mockMvc.perform(
                get("/indicators").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("indicators", responseFields(
                fieldWithPath("[].id")
                        .description("Indicator ID"),
                fieldWithPath("[].name")
                        .description("Indicator Name"),
                fieldWithPath("[].groupId").description("Indicator Group Id"))));
    }
    
    @Test
    public void indicatorsRequestParameters() throws Exception {
        String periodDec="Period parameter. Can be an explicit year, YYYY (eg 2018) which will give the stated year values, or YYYYmm which gives "
                + "values for only a particular month";
        this.mockMvc.perform(
                get("/indicators?pe=2017&ouid=23408&id=61829").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("indicators", requestParameters( 
			parameterWithName("pe").description(periodDec), 
			parameterWithName("ouid").description("Organisation unit id, if not provided defaults to national"),
                        parameterWithName("id").description("Indicator ID which is mandatory") 
	)));
    }
}
