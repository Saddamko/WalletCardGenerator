/*
http://localhost:1488/WalletCardGeneratorContext/WalletCardGenerator
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WalletCardGenerator;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;

/**
 *
 * @author VashurinVlad
 */
@WebServlet(name = "WalletCardGenerator", description = "REST Wallet Card Generator Servlet", urlPatterns = {"/WalletCardGenerator", "/WalletCardGenerator/wallet"})
public class WalletCardGenerator extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Generated HTNL/title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Servlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
//        processRequest(request, response);

        
        if (request.getRequestURI().equals("/WalletCardGeneratorContext/WalletCardGenerator")) {
            response.setContentType("text/html");
//            response.getWriter().print("<html><head></head><body><h1>Welcome!</h1><p>This is a very cool page!</p></body></html>");
            request.getRequestDispatcher("/testget.jsp").forward(request, response);
            ;
            
        } else if (request.getRequestURI().startsWith("/WalletCardGeneratorContext/WalletCardGenerator/wallet")) {

            Integer prettyFragileUserId = Integer.valueOf(request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1));

            response.setContentType("application/json");

            // User user = dao.findUser(prettyFragileUserId)
            // actually: jsonLibrary.toString(user)
            response.getWriter().print("{\n" +
                    "  \"id\":" + prettyFragileUserId + ",\n" +
                    "  \"age\": 48,\n" +
                    "  \"name\" : \"Vladimir Vashurin\"\n" +
                    "}");
        } else {
            throw new IllegalStateException("Help, I don't know what to do with this url");
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
//        processRequest(request, response);
//        getServletInfo();
        request.getRequestDispatcher("/testpost.jsp").forward(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "getServletInfo () Short description";
    }// </editor-fold>

    
}
