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
                + "values for only a particular month. Simicolon seperate period to get data for different periods";
        this.mockMvc.perform(
                get("/indicators?pe=2017&ouid=23408&id=61829").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-indicators-values-returned-with-request-parameters", requestParameters(
                parameterWithName("pe").description(periodDec),
                parameterWithName("ouid").description("Organisation unit id, if not provided defaults to national. Simicolon seperate org unit id to get data for different organisation units"),
                parameterWithName("id").description("Indicator ID which is mandatory.  Simicolon seperate indicator ids to get data for different indicators")
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
                        fieldWithPath("result.dictionary.parameters.location[].ouid")
                                .description("Organanisation unit id"),
                        fieldWithPath("result.dictionary.parameters.location[].name")
                                .description("Organanisation unit name"),
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
                fieldWithPath("result.dictionary.parameters.location[].ouid")
                        .description("Organanisation unit id"),
                fieldWithPath("result.dictionary.parameters.location[].name")
                        .description("Organanisation unit name"),
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
    public void testIndicatorsValuesRequestedPerLevelBasis() throws Exception {
        String periodDec = "Period parameter. Can be an explicit year, YYYY (eg 2018) which will give the stated year values, or YYYYmm which gives "
                + "values for only a particular month";
        this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/indicators/{id}?pe=2017&ouid=18&level=2", 61829).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-indicators-values-requested-per-level-basis", pathParameters(
                parameterWithName("id").description("Indicator ID which is mandatory")
        ), requestParameters(
                parameterWithName("pe").description(periodDec),
                parameterWithName("ouid").description("Organisation unit id, if not provided defaults to national"),
                parameterWithName("level").description("Organisation unit level whose indicator value are to be returned.  "
                        + "\n"
                        + "Level 1= National, Leve 2 = County, Level 3= Sub-County, Level 4= Ward, Level 5 = Facility")
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
                fieldWithPath("result.dictionary.parameters.location[].ouid")
                        .description("Organanisation unit id"),
                fieldWithPath("result.dictionary.parameters.location[].name")
                        .description("Organanisation unit name"),
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

    @Test
    public void testWeatherIndicatorCorrelation() throws Exception {
        this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/weather_correlation/{indicatorid}/{ouid}", 23185, 23408).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-weather-indicator", pathParameters(
                parameterWithName("indicatorid").description("Indicator ID which is mandatory"),
                parameterWithName("ouid").description("Organisation ID which is mandatory")
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
                fieldWithPath("result.dictionary.weather")
                        .description("Holds weather entities used in analyses. Its a dictionary with key as weather type ID and value as weather type"),
                
                fieldWithPath("result.dictionary.weather.1")
                        .description("Id for the temperature variable"),
                fieldWithPath("result.dictionary.weather.2")
                        .description("Id for the dew point variable"),
                fieldWithPath("result.dictionary.weather.3")
                        .description("Id for the humidity variable"),
                fieldWithPath("result.dictionary.weather.5")
                        .description("Id for the pressure variable"),
                //

                fieldWithPath("result.dictionary.analyses")
                        .description("Holds metadata for the correlation model"),
                fieldWithPath("result.dictionary.analyses.correlation_dimension")
                        .description("Holds the order used for calculation of the covarience values in the same order as the returned correlation data in the payload"),
                fieldWithPath("result.dictionary.analyses.variables")
                        .description("dictionary of variables used in the correlation model. Key is ID, value is the name of the variable"),
                fieldWithPath("result.dictionary.analyses.variables.23185")
                        .description("Id for the indicator used in analysis"),
                fieldWithPath("result.dictionary.analyses.variables.1")
                        .description("Id for the temperature variable"),
                fieldWithPath("result.dictionary.analyses.variables.2")
                        .description("Id for the dew point variable"),
                fieldWithPath("result.dictionary.analyses.variables.3")
                        .description("Id for the humidity variable"),
                fieldWithPath("result.dictionary.analyses.variables.5")
                        .description("Id for the pressure variable"),
                
                
                fieldWithPath("result.dictionary.analyses.period_type")
                        .description("Data period frequency used in the analyses."),
                fieldWithPath("result.dictionary.analyses.correlation_coeffient")
                        .description("Correlation model used in the analysis"),
                fieldWithPath("result.dictionary.analyses.period_span")
                        .description("Shows the period range of data used in the analyses"),
                fieldWithPath("result.dictionary.analyses.period_span.start_date")
                        .description("Begining period for data used in analysis"),
                fieldWithPath("result.dictionary.analyses.period_span.end_date")
                        .description("End period for data used in analysis"),
                //                data section 

                fieldWithPath("result.data")
                        .description("The reponse payload"),
                fieldWithPath("result.data.weather")
                        .description("Data payload for weather data"),
                
                fieldWithPath("result.data.weather.1")
                        .description("Weather type id"),
                fieldWithPath("result.data.weather.1[]")
                        .description("List of data for this weather type"),
                fieldWithPath("result.data.weather.1[].value")
                        .description("Value for this period"),
                fieldWithPath("result.data.weather.1[].date")
                        .description("Period for this value"),
                fieldWithPath("result.data.weather.1[].ouid")
                        .description("The organisation unit for this value"),
                
                
                fieldWithPath("result.data.weather.2")
                        .description("Weather type id"),
                fieldWithPath("result.data.weather.2[]")
                        .description("List of data for this weather type"),
                fieldWithPath("result.data.weather.2[].value")
                        .description("Value for this period"),
                fieldWithPath("result.data.weather.2[].date")
                        .description("Period for this value"),
                fieldWithPath("result.data.weather.2[].ouid")
                        .description("The organisation unit for this value"),
                
                fieldWithPath("result.data.weather.3")
                        .description("Weather type id"),
                fieldWithPath("result.data.weather.3[]")
                        .description("List of data for this weather type"),
                fieldWithPath("result.data.weather.3[].value")
                        .description("Value for this period"),
                fieldWithPath("result.data.weather.3[].date")
                        .description("Period for this value"),
                fieldWithPath("result.data.weather.3[].ouid")
                        .description("The organisation unit for this value"),
                
                fieldWithPath("result.data.weather.5")
                        .description("Weather type id"),
                fieldWithPath("result.data.weather.5[]")
                        .description("List of data for this weather type"),
                fieldWithPath("result.data.weather.5[].value")
                        .description("Value for this period"),
                fieldWithPath("result.data.weather.5[].date")
                        .description("Period for this value"),
                fieldWithPath("result.data.weather.5[].ouid")
                        .description("The organisation unit for this value"),
                
                
                fieldWithPath("result.data.indicator")
                        .description("Data payload for weather data"),
                fieldWithPath("result.data.indicator.23185")
                        .description("Indicator id used in this correlation"),
                fieldWithPath("result.data.indicator.23185.[]")
                        .description("List of data for this indicator"),
                fieldWithPath("result.data.indicator.23185.[].value")
                        .description("Value for this period"),
                fieldWithPath("result.data.indicator.23185.[].date")
                        .description("Period for this value"),
                fieldWithPath("result.data.indicator.23185.[].ouid")
                        .description("The organisation unit for this value"),
                
                
                fieldWithPath("result.data.correlation")
                        .description("Correlation matrix - covariance scoring"),
                fieldWithPath("result.data.correlation.kpivalue[]")
                        .description("Covariance scoring for the indicator against other corr variables in order of correlation_dimension from the metadata element"),
                fieldWithPath("result.data.correlation.dew_point[]")
                        .description("Covariance scoring for dew point against other corr variables in order of correlation_dimension from the metadata element"),
                fieldWithPath("result.data.correlation.humidity[]")
                        .description("Covariance scoring for humidity against other corr variables in order of correlation_dimension from the metadata element"),
                fieldWithPath("result.data.correlation.temperature[]")
                        .description("Covariance scoring for temperature against other corr variables in order of correlation_dimension from the metadata element"),
                fieldWithPath("result.data.correlation.pressure[]")
                        .description("Covariance scoring for pressure against other corr variables in order of correlation_dimension from the metadata element")
        )
        ));
    }
    
    
    @Test
    public void testIndicatorIndicatorCorrelation() throws Exception {
        this.mockMvc.perform(
                RestDocumentationRequestBuilders.get("/indicator_correlation/{indicatorid}/{ouid}/{correlationIndicators}", 23185,23408,"23191,31589").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andDo(document("test-indicator-indicator", pathParameters(
                parameterWithName("indicatorid").description("Indicator ID which is mandatory"),
                parameterWithName("ouid").description("Organisation ID which is mandatory"),
                parameterWithName("correlationIndicators").description("List of indicators to process correlation against (Mandatory)")
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

                fieldWithPath("result.dictionary.analyses")
                        .description("Holds metadata for the correlation model"),
                fieldWithPath("result.dictionary.analyses.correlation_dimension")
                        .description("Holds the order used for calculation of the covarience values in the same order as the returned correlation data in the payload"),
                
                fieldWithPath("result.dictionary.analyses.variables")
                        .description("dictionary of variables used in the correlation model. Key is ID, value is the name of the variable"),
                fieldWithPath("result.dictionary.analyses.variables.23185")
                        .description("Indicator Id"),
                fieldWithPath("result.dictionary.analyses.variables.23191")
                        .description("Indicator Id"),
                fieldWithPath("result.dictionary.analyses.variables.31589")
                        .description("Indicator Id"),
                
                
                fieldWithPath("result.dictionary.analyses.period_type")
                        .description("Data period frequency used in the analyses."),
                fieldWithPath("result.dictionary.analyses.correlation_coeffient")
                        .description("Correlation model used in the analysis"),
                fieldWithPath("result.dictionary.analyses.period_span")
                        .description("Shows the period range of data used in the analyses"),
                fieldWithPath("result.dictionary.analyses.period_span.start_date")
                        .description("Begining period for data used in analysis"),
                fieldWithPath("result.dictionary.analyses.period_span.end_date")
                        .description("End period for data used in analysis"),
               
                //                data section 

     
                fieldWithPath("result.data.indicator")
                        .description("Data payload for weather data"),
                fieldWithPath("result.data.indicator.23185")
                        .description("Indicator id used in this correlation"),
                fieldWithPath("result.data.indicator.23185.[]")
                        .description("List of data for this indicator"),
                fieldWithPath("result.data.indicator.23185.[].value")
                        .description("Value for this period"),
                fieldWithPath("result.data.indicator.23185.[].date")
                        .description("Period for this value"),
                fieldWithPath("result.data.indicator.23185.[].ouid")
                        .description("The organisation unit for this value"),
                

                fieldWithPath("result.data.indicator.23191")
                        .description("Indicator id used in this correlation"),
                fieldWithPath("result.data.indicator.23191.[]")
                        .description("List of data for this indicator"),
                fieldWithPath("result.data.indicator.23191.[].value")
                        .description("Value for this period"),
                fieldWithPath("result.data.indicator.23191.[].date")
                        .description("Period for this value"),
                fieldWithPath("result.data.indicator.23191.[].ouid")
                        .description("The organisation unit for this value"),

                fieldWithPath("result.data.indicator.31589")
                        .description("Indicator id used in this correlation"),
                fieldWithPath("result.data.indicator.31589.[]")
                        .description("List of data for this indicator"),
                fieldWithPath("result.data.indicator.31589.[].value")
                        .description("Value for this period"),
                fieldWithPath("result.data.indicator.31589.[].date")
                        .description("Period for this value"),
                fieldWithPath("result.data.indicator.31589.[].ouid")
                        .description("The organisation unit for this value"),
                
                
                fieldWithPath("result.data.correlation")
                        .description("Correlation matrix - covariance scoring"),
                fieldWithPath("result.data.correlation.23185[]")
                        .description("Covariance scoring for this indicator against other corr variables in order of correlation_dimension from the metadata element"),
                fieldWithPath("result.data.correlation.23191[]")
                        .description("Covariance scoring for this indicator against other corr variables in order of correlation_dimension from the metadata element"),
                fieldWithPath("result.data.correlation.31589[]")
                        .description("Covariance scoring for this indicator against other corr variables in order of correlation_dimension from the metadata element")
        )
        ));
    }

}
