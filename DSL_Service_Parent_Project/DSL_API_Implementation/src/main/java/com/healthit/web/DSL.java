package com.healthit.web;


import com.healthit.dsl_api_impl.service.DSLEntityCollectionProcessor;
import com.healthit.dsl_api_impl.service.Edmprovider;
import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.commons.api.edmx.EdmxReference;


public class DSL extends HttpServlet {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(DSL.class);

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      HttpSession session = req.getSession(true);
//      Storage storage = (Storage) session.getAttribute(Storage.class.getName());
//      if (storage == null) {
//        storage = new Storage();
//        session.setAttribute(Storage.class.getName(), storage);
//      }

      // create odata handler and configure it with EdmProvider and Processor
      OData odata = OData.newInstance();
      ServiceMetadata edm = odata.createServiceMetadata(new Edmprovider(), new ArrayList<EdmxReference>());
      ODataHttpHandler handler = odata.createHandler(edm);
      handler.register(new DSLEntityCollectionProcessor());
      //handler.register(new DemoEntityProcessor(storage));
      //handler.register(new DemoPrimitiveProcessor(storage));

      // let the handler do the work
      handler.process(req, resp);
    } catch (RuntimeException e) {
      LOG.error("Server Error occurred in DemoServlet", e);
      throw new ServletException(e);
    }

  }

}
