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
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
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
public class PandemicApisDocumentationTest {

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
    public void testSourcesReturnedApiCall() throws Exception {
        this.mockMvc.perform(
                get("/pandemics").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-pandemics-sources", responseFields(
                fieldWithPath("[].id")
                        .description("Pandemic id"),
                fieldWithPath("[].name")
                        .description("Pandemic name"),
                fieldWithPath("[].initial_report_date")
                        .description("Date for this pandemics' first available data"),
                fieldWithPath("[].name")
                        .description("Pandemic name"),
                fieldWithPath("[].indicators")
                        .description("List of indicators available for this pandemic"),
                fieldWithPath("[].indicators.[].id")
                        .description("Indicator id"),
                fieldWithPath("[].indicators.[].name")
                        .description("Indicator name")
        )));
    }

    @Test
    public void testPandemicsValuesReturned() throws Exception {
        String avail = "availble that, can be requested through the request parameters";
        this.mockMvc.perform( //covid19
                RestDocumentationRequestBuilders.get("/pandemics/{pandemic_id}?id=6074&start_date=2020-04-01&end_date=2020-06-02&org_id=10277", "covid19").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-pandemics-values-returned", pathParameters(
                parameterWithName("pandemic_id").description("Pandemic id")
        ), requestParameters(
                parameterWithName("id").description("Pandemic indicator id gotten from $/pandemics"),
                parameterWithName("start_date").description("Start period to return data from"),
                parameterWithName("end_date").description("End period of data to return"),
                parameterWithName("org_id").description("The organisation id for returned data, only at county level. id gotten from  gotten from $/counties  ")
        ), responseFields(
                fieldWithPath("result")
                        .description("Response envelope object"),
                //                Meta data section
                fieldWithPath("result.dictionary")
                        .description("Carries metadata for the payload"),
                fieldWithPath("result.dictionary.orgunits")
                        .description("Requested organization units "),
                fieldWithPath("result.dictionary.orgunits[]")
                        .description("List of requested organisation unit(s)"),
                fieldWithPath("result.dictionary.orgunits[].id")
                        .description("Organisation unit id"),
                fieldWithPath("result.dictionary.orgunits[].name")
                        .description("Organisation unit name"),
                fieldWithPath("result.dictionary.orgunits[].latitude")
                        .description("Organisation unit latitude"),
                fieldWithPath("result.dictionary.orgunits[].longitude")
                        .description("Organisation unit longitude"),
                fieldWithPath("result.dictionary.indicators")
                        .description("Requested indicator(s)"),
                fieldWithPath("result.dictionary.indicators[]")
                        .description("List of requested indicator(s)"),
                fieldWithPath("result.dictionary.indicators[].name")
                        .description("Name of the indicator"),
                fieldWithPath("result.dictionary.indicators[].id")
                        .description("ID of the indicator"),
                fieldWithPath("result.data")
                        .description("Data payload"),
                fieldWithPath("result.data.6074")
                        .description("6074 is the indicator id for this list, whose data entries corresponds to this indicator"),
                fieldWithPath("result.data.6074.[]")
                        .description("List of data returned associated to this indicator id"),
                fieldWithPath("result.data.6074.[].period")
                        .description("Period for this data set"),
                fieldWithPath("result.data.6074.[].ou")
                        .description("The organisation unit for this data set"),
                fieldWithPath("result.data.6074.[].value")
                        .description("The value for this data set")
        )
        ));
    }

}
