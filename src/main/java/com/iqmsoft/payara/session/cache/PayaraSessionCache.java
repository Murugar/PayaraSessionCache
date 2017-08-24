
package com.iqmsoft.payara.session.cache;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import fish.payara.cdi.jsr107.impl.NamedCache;
import fish.payara.micro.PayaraMicroRuntime;
import fish.payara.micro.data.ApplicationDescriptor;
import fish.payara.micro.data.InstanceDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebServlet(name = "PayaraSessionCache", urlPatterns = {"/PayaraSessionCache"})
public class PayaraSessionCache extends HttpServlet {
    
   @Inject
   PayaraMicroRuntime runtime;
   
   @Inject
   HazelcastInstance instance;
   
   @Inject
   CacheManager cm;
   
   @NamedCache (cacheName = "Payara JCache")
   @Inject Cache examplesCache;

   
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        
       // let us initialise the session
        HttpSession session = request.getSession(true);
        Long requestCount = (Long) session.getAttribute("RequestCount");
        if (requestCount == null) {
            requestCount = 0L;
        }
        session.setAttribute("RequestCount", requestCount+1);
        
        // Store stuff in JSR 107 JCache
        Long cacheCount = (Long) examplesCache.get("RequestCount");
        if (cacheCount == null) {
            cacheCount = 0L;
        }
        examplesCache.put("RequestCount", cacheCount+1);
        
        try (PrintWriter out = response.getWriter()) {
           
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Payara Session Cache</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet PayaraSessionCache at " + request.getContextPath() + "</h1>");
            out.println("Cluster-wide HTTP Session request count : " + session.getAttribute("RequestCount"));
            out.println("<br/>JCache stored request count : " + examplesCache.get("RequestCount"));
            
            
            
            
            out.println("<h2>Distributed Objects</h2><table>");
            out.println(getCacheDescriptions());
            
            
            out.println("</body>");
            out.println("</html>");
        }
    }

   
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

   
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

  
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    
    private String getCacheDescriptions() {
        StringBuilder result = new StringBuilder();
        for (DistributedObject dObject : instance.getDistributedObjects()) {
            result.append("HZ Object : ").append(dObject.getName()).append("<br/>");
        }
        
        for (String name : cm.getCacheNames()) {
            result.append("JCache : ").append(name).append("<br/>");            
        }
       return result.toString();
        
    }
}
