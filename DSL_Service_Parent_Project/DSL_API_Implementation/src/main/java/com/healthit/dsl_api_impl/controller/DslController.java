/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dsl_api_impl.controller;

import com.healthit.dsl_api_impl.service.Edmprovider;
import com.healthit.dsl_api_impl.service.DSLEntityCollectionProcessor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;

/**
 *
 * @author duncan
 */
public class DslController extends HttpServlet {

  private static final long serialVersionUID = 1L;
  final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DslController.class);

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

    try {
      // create odata handler and configure it with EdmProvider and Processor
      OData odata = OData.newInstance();
      ServiceMetadata edm = odata.createServiceMetadata(new Edmprovider(), new ArrayList<EdmxReference>());
      ODataHttpHandler handler = odata.createHandler(edm);
      handler.register(new DSLEntityCollectionProcessor());

      // let the handler do the work
      handler.process(req, resp);

    } catch (RuntimeException e) {
      log.error("Server Error occurred in ExampleServlet", e);
      throw new ServletException(e);
    }
  }
}
