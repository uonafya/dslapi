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
public class SurveyApisDocumentationTest {

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
                get("/survey/sources").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-survey-sources", responseFields(
                fieldWithPath("[].id")
                        .description("Data source ID"),
                fieldWithPath("[].name")
                        .description("Data source name")
        )));
    }

    @Test
    public void testSurveySourceIndicatorListing() throws Exception {
        this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/survey/sources/{source_id}", 3).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("get-indicators-from-a-source", pathParameters(
                parameterWithName("source_id").description("Survey source id")
        ), responseFields(
                fieldWithPath("[].source id")
                        .description("Indicator source id"),
                fieldWithPath("[].name")
                        .description("Indicator name"),
                fieldWithPath("[].description")
                        .description("Indicator description"),
                fieldWithPath("[].id")
                        .description("Indicator id"),
                fieldWithPath("[].source")
                        .description("Indicator source name")
        )
        ));
    }

    @Test
    public void testSurveyIndicatorsValuesReturned() throws Exception {
        String avail = "availble for request for the combination of given url parameters";
        String catDecription="You can filter the returned indicator data using the category(catID), period(pe) and orgId parameters."
                + "pe takes a peiod if available from the requested indicator. This can be checked from 'available' field of responce json "
                + "($/survey/sources/{source_id}?id=9530). eg. pe=2016."
                + "orgId takes an organisation id for which to return its data eg orId=18"
                + "The catID takes a list of categories slice the requested indicator against. It takes a comma separated list of categories from"
                + " gotten from the 'available' field of response json. eg, catID=1,2,3; Some indicator categories may be request in groups the occur"
                + "in if available. eg, female (id=x) that are age18-20 (id=y), this is requested as catID=x;y ";
        this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/survey/sources/{source_id}?id=9530&catID=7;65,18", 7).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test--survey-indicators-values-returned", pathParameters(
                parameterWithName("source_id").description("Survey source id")
        ), requestParameters(
                parameterWithName("id").description("Survey indicator ID gotten from $/survey/sources/{source_id}"),
                parameterWithName("catID").description(catDecription)
        ), responseFields(
                fieldWithPath("result")
                        .description("Response envelope object"),
                //                Meta data section
                fieldWithPath("result.dictionary")
                        .description("Carries metadata for the payload"),
                fieldWithPath("result.dictionary.available")
                        .description("Carries metadata showing all available data combination that can be requested for the combination of given url parameters"),
                fieldWithPath("result.dictionary.available.orgunits")
                        .description("Available organization units " + avail),
                fieldWithPath("result.dictionary.available.orgunits[]")
                        .description("List of organisation unit(s) " + avail),
                fieldWithPath("result.dictionary.available.orgunits[].id")
                        .description("Organisation unit id"),
                fieldWithPath("result.dictionary.available.orgunits[].name")
                        .description("Organisation unit name"),
                fieldWithPath("result.dictionary.available.categories")
                        .description("Available indicator categories " + avail),
                fieldWithPath("result.dictionary.available.categories[]")
                        .description("List of indicator categories " + avail),
                fieldWithPath("result.dictionary.available.categories[][]")
                        .description("An array that holds categories can be requested in combination"),
                fieldWithPath("result.dictionary.available.categories[][].name")
                        .description("Name of the indicator category"),
                fieldWithPath("result.dictionary.available.categories[][].id")
                        .description("ID of the indicator category"),
                fieldWithPath("result.dictionary.available.periods")
                        .description("Available periods units " + avail),
                fieldWithPath("result.dictionary.available.periods[]")
                        .description("List of period(s) " + avail),
                fieldWithPath("result.dictionary.orgunits")
                        .description("Requested organization units "),
                fieldWithPath("result.dictionary.orgunits[]")
                        .description("List of requested organisation unit(s)"),
                fieldWithPath("result.dictionary.orgunits[].id")
                        .description("Organisation unit id"),
                fieldWithPath("result.dictionary.orgunits[].name")
                        .description("Organisation unit name"),
                fieldWithPath("result.dictionary.categories")
                        .description("Requested indicator categories"),
                fieldWithPath("result.dictionary.categories[]")
                        .description("List of requested indicator categories"),
                fieldWithPath("result.dictionary.categories[][]")
                        .description("An array that holds categories that are requested in combination"),
                fieldWithPath("result.dictionary.categories[][].name")
                        .description("Name of the indicator category"),
                fieldWithPath("result.dictionary.categories[][].id")
                        .description("ID of the indicator category"),
                fieldWithPath("result.dictionary.periods")
                        .description("Periods requested "),
                fieldWithPath("result.dictionary.periods[]")
                        .description("List of requested period(s)"),
                fieldWithPath("result.dictionary.indicators")
                        .description("Requested indicator(s)"),
                fieldWithPath("result.dictionary.indicators[]")
                        .description("List of requested indicator(s)"),
                fieldWithPath("result.dictionary.indicators[].name")
                        .description("Name of the indicator"),
                fieldWithPath("result.dictionary.indicators[].id")
                        .description("ID of the indicator"),
                fieldWithPath("result.dictionary.indicators[].source id")
                        .description("Data source ID of the indicator"),
                fieldWithPath("result.dictionary.indicators[].description")
                        .description("description of the indicator"),
                fieldWithPath("result.dictionary.indicators[].source")
                        .description("Data source name of the indicator"),
                fieldWithPath("result.data")
                        .description("Data payload"),
                fieldWithPath("result.data[]")
                        .description("List of data requested"),
                fieldWithPath("result.data[].source_id")
                        .description("Data source id for this indicator"),
                fieldWithPath("result.data[].category[]")
                        .description("Indicator category of this data value that was requested combined"),
                fieldWithPath("result.data[].category[].name")
                        .description("Name of the category"),
                fieldWithPath("result.data[].category[].id")
                        .description("ID of the category"),
                fieldWithPath("result.data[].indicator_id")
                        .description("Indicator ID"),
                fieldWithPath("result.data[].value")
                        .description("Value of this indicator and its parameters matrix")
        )
        ));
    }

}
