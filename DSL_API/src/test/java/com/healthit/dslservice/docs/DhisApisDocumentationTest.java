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
public class DhisApisDocumentationTest {

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
    public void testIndicatorsReturnedApiCall() throws Exception {
        this.mockMvc.perform(
                get("/indicators").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-indicators-returned-api-call", responseFields(
                fieldWithPath("[].id")
                        .description("Indicator ID"),
                fieldWithPath("[].name")
                        .description("Indicator Name"),
                fieldWithPath("[].groupId").description("Indicator Group Id"),
                fieldWithPath("[].description").description("Indicator description")
        )));
    }

    @Test
    public void testIndicatorsValuesReturnedWithRequestParameters() throws Exception {
        String periodDec = "Period parameter. Can be an explicit year, YYYY (eg 2018) which will give the stated year values, or YYYYmm which gives "
                + "values for only a particular month";
        this.mockMvc.perform(
                get("/indicators?pe=2017&ouid=23408&id=61829").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-indicators-values-returned-with-request-parameters", requestParameters(
                parameterWithName("pe").description(periodDec),
                parameterWithName("ouid").description("Organisation unit id, if not provided defaults to national"),
                parameterWithName("id").description("Indicator ID which is mandatory")
        ),
                responseFields(
                        fieldWithPath("result")
                                .description("Response envelope object"),
                        //                Meta data section
                        fieldWithPath("result.dictionary")
                                .description("Carries metadata for the payload"),
                        fieldWithPath("result.dictionary.orgunits")
                                .description("Metadata for organization units contained in the reponse payload"),
                        fieldWithPath("result.dictionary.orgunits[]")
                                .description("List of organisation unit(s)"),
                        fieldWithPath("result.dictionary.orgunits[].id")
                                .description("Organisation unit id"),
                        fieldWithPath("result.dictionary.orgunits[].name")
                                .description("Organisation unit name"),
                        fieldWithPath("result.dictionary.indicators")
                                .description("Metadata for indicators contained in the reponse payload"),
                        fieldWithPath("result.dictionary.indicators[]")
                                .description("List of indicator(s) in the payload"),
                        fieldWithPath("result.dictionary.indicators[].id")
                                .description("Indicator ID"),
                        fieldWithPath("result.dictionary.indicators[].name")
                                .description("Indicator name"),
                        fieldWithPath("result.dictionary.indicators[].description")
                                .description("Indicator description"),
                        fieldWithPath("result.dictionary.indicators[].last_updated")
                                .description("Indicator indicator update date"),
                        fieldWithPath("result.dictionary.indicators[].date_created")
                                .description("Indicator creation date"),
                        fieldWithPath("result.dictionary.indicators[].source")
                                .description("Source for this Indicator"),
                        //
                        fieldWithPath("result.dictionary.parameters")
                                .description("Metadata for requested parameters value"),
                        fieldWithPath("result.dictionary.parameters.period")
                                .description("List of period id(s) requested"),
                        fieldWithPath("result.dictionary.parameters.location")
                                .description("List of organanisation unit id(s) requested"),
                        fieldWithPath("result.dictionary.parameters.indicators")
                                .description("List of indicator id(s) requested"),
                        //                data section 
                        fieldWithPath("result.data")
                                .description("The reponse payload"),
                        fieldWithPath("result.data.61829")
                                .description("Indicator id"),
                        fieldWithPath("result.data.61829.[]")
                                .description("List of response data associated with this indicator"),
                        fieldWithPath("result.data.61829.[].value")
                                .description("Indicator value (The key performance indicator value)"),
                        fieldWithPath("result.data.61829.[].period")
                                .description("Indicator period id "),
                        fieldWithPath("result.data.61829.[].ou")
                                .description("Indicator organisation unit id")
                )
        ));
    }

    @Test
    public void testIndicatorsValuesReturnedWithPathAndRequestParameters() throws Exception {
        String periodDec = "Period parameter. Can be an explicit year, YYYY (eg 2018) which will give the stated year values, or YYYYmm which gives "
                + "values for only a particular month";
        this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/indicators/{id}?pe=2017&ouid=23408", 61829).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-indicators-values-returned-with-path-and-request-parameters", pathParameters(
                parameterWithName("id").description("Indicator ID which is mandatory")
        ), requestParameters(
                parameterWithName("pe").description(periodDec),
                parameterWithName("ouid").description("Organisation unit id, if not provided defaults to national")
        ), responseFields(
                fieldWithPath("result")
                        .description("Response envelope object"),
                //                Meta data section
                fieldWithPath("result.dictionary")
                        .description("Carries metadata for the payload"),
                fieldWithPath("result.dictionary.orgunits")
                        .description("Metadata for organization units contained in the reponse payload"),
                fieldWithPath("result.dictionary.orgunits[]")
                        .description("List of organisation unit(s)"),
                fieldWithPath("result.dictionary.orgunits[].id")
                        .description("Organisation unit id"),
                fieldWithPath("result.dictionary.orgunits[].name")
                        .description("Organisation unit name"),
                fieldWithPath("result.dictionary.indicators")
                        .description("Metadata for indicators contained in the reponse payload"),
                fieldWithPath("result.dictionary.indicators[]")
                        .description("List of indicator(s) in the payload"),
                fieldWithPath("result.dictionary.indicators[].id")
                        .description("Indicator ID"),
                fieldWithPath("result.dictionary.indicators[].name")
                        .description("Indicator name"),
                fieldWithPath("result.dictionary.indicators[].description")
                        .description("Indicator description"),
                fieldWithPath("result.dictionary.indicators[].last_updated")
                        .description("Indicator indicator update date"),
                fieldWithPath("result.dictionary.indicators[].date_created")
                        .description("Indicator creation date"),
                fieldWithPath("result.dictionary.indicators[].source")
                        .description("Source for this Indicator"),
                //
                fieldWithPath("result.dictionary.parameters")
                        .description("Metadata for requested parameters value"),
                fieldWithPath("result.dictionary.parameters.period")
                        .description("List of period id(s) requested"),
                fieldWithPath("result.dictionary.parameters.location")
                        .description("List of organanisation unit id(s) requested"),
                fieldWithPath("result.dictionary.parameters.indicators")
                        .description("List of indicator id(s) requested"),
                //                data section 
                fieldWithPath("result.data")
                        .description("The reponse payload"),
                fieldWithPath("result.data.61829")
                        .description("Indicator id"),
                fieldWithPath("result.data.61829.[]")
                        .description("List of response data associated with this indicator"),
                fieldWithPath("result.data.61829.[].value")
                        .description("Indicator value (The key performance indicator value)"),
                fieldWithPath("result.data.61829.[].period")
                        .description("Indicator period id "),
                fieldWithPath("result.data.61829.[].ou")
                        .description("Indicator organisation unit id")
        )
        ));
    }

    @Test
    public void testIndicatorGroupsReturnedApiCall() throws Exception {
        this.mockMvc.perform(
                get("/indicatorgroups").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-indicator-groups-returned-api-call", responseFields(
                fieldWithPath("[].id")
                        .description("Indicator group ID"),
                fieldWithPath("[].name")
                        .description("Indicator group Name"))));
    }

    @Test
    public void testIndicatorsByGroupIdReturned() throws Exception {

        this.mockMvc.perform(
                get("/indicators?groupId=31591").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-indicators-by-group-id-returned", requestParameters(
                parameterWithName("groupId").description("Indicator group ID. Returns indicators under this indicator group")
        ), responseFields(
                fieldWithPath("[].id")
                        .description("Indicator ID"),
                fieldWithPath("[].name")
                        .description("Indicator Name"),
                fieldWithPath("[].groupId").description("Indicator Group Id"),
                fieldWithPath("[].description").description("Indicator description")
        )
        ));
    }

    @Test
    public void testPredictor() throws Exception {
        //http://localhost:8080/DSL_API/api/forecast/61829?ouid=23408&periodtype=yearly&periodspan=12
        String periodDec = "Indicate weather to make yearly or monthly projections, defaults to yearly";
        this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/forecast/{id}?ouid=23408&periodtype=x&periodspan=x", 61829).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-predictor", pathParameters(
                parameterWithName("id").description("Indicator ID which is mandatory")
        ), requestParameters(
                parameterWithName("ouid").description("Organisation unit id, if not provided defaults to national"),
                parameterWithName("periodtype").description(periodDec),
                parameterWithName("periodspan").description("Number of months (if periodtype is monthly) or years (if periodtype is yearly) to project,"
                        + "if not provided defaults to 2")
        ), responseFields(
                fieldWithPath("result")
                        .description("Response envelope object"),
                //                Meta data section
                fieldWithPath("result.dictionary")
                        .description("Carries metadata for the payload"),
                fieldWithPath("result.dictionary.orgunits")
                        .description("Metadata for organization units contained in the reponse payload"),
                fieldWithPath("result.dictionary.orgunits[]")
                        .description("List of organisation unit(s)"),
                fieldWithPath("result.dictionary.orgunits[].id")
                        .description("Organisation unit id"),
                fieldWithPath("result.dictionary.orgunits[].name")
                        .description("Organisation unit name"),
                fieldWithPath("result.dictionary.indicators")
                        .description("Metadata for indicators contained in the reponse payload"),
                fieldWithPath("result.dictionary.indicators[]")
                        .description("List of indicator(s) in the payload"),
                fieldWithPath("result.dictionary.indicators[].id")
                        .description("Indicator ID"),
                fieldWithPath("result.dictionary.indicators[].name")
                        .description("Indicator name"),
                fieldWithPath("result.dictionary.indicators[].description")
                        .description("Indicator description"),
                fieldWithPath("result.dictionary.indicators[].last_updated")
                        .description("Indicator indicator update date"),
                fieldWithPath("result.dictionary.indicators[].date_created")
                        .description("Indicator creation date"),
                fieldWithPath("result.dictionary.indicators[].source")
                        .description("Source for this Indicator"),
                //
                fieldWithPath("result.dictionary.parameters")
                        .description("Metadata for requested parameters value"),
                fieldWithPath("result.dictionary.parameters.periodtype")
                        .description("Period type selected, could be 'yearly' or 'monthly'"),
                fieldWithPath("result.dictionary.parameters.periodspan")
                        .description("Period span selected"),
                fieldWithPath("result.dictionary.parameters.location")
                        .description("List of organanisation unit id(s) requested"),
                fieldWithPath("result.dictionary.parameters.indicators")
                        .description("List of indicator id(s) requested"),
                //                data section 
                fieldWithPath("result.data")
                        .description("The reponse payload"),
                fieldWithPath("result.data.61829")
                        .description("Indicator id"),
                fieldWithPath("result.data.61829.18")
                        .description("Org unit id"),
                fieldWithPath("result.data.61829.18.trend")
                        .description("Data showing the overall historical trend of the indicator"),
                fieldWithPath("result.data.61829.18.projection")
                        .description("Projection of the indicator over the selected period"),
                fieldWithPath("result.data.61829.18.yearly")
                        .description("Average trend of the indicator in a given year"),
                fieldWithPath("result.data.61829.18.trend[].time")
                        .description("Period"),
                fieldWithPath("result.data.61829.18.trend[].value")
                        .description("Value for this period"),
                fieldWithPath("result.data.61829.18.projection[].time")
                        .description("Period"),
                fieldWithPath("result.data.61829.18.projection[].value")
                        .description("Value for this period"),
                fieldWithPath("result.data.61829.18.yearly[].time")
                        .description("Period"),
                fieldWithPath("result.data.61829.18.yearly[].value")
                        .description("Value for this period")
        )
        ));
    }

}
