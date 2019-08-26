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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 *
 * @author duncan
 */
@ContextConfiguration("file:src/main/webapp/WEB-INF/spring-servlet.xml" )
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class DhisApisDocumentationTest {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup( this.wac ).build();
    }

    @Test
    public void getPersonByIdShouldReturnOk() throws Exception {
      this.mockMvc.perform(
            get( "/indicators" ).accept( MediaType.parseMediaType( "application/json;charset=UTF-8" ) ) )
            .andExpect( status().isOk() );
    }
}
