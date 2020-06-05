/*
http://localhost:1488/WalletCardGeneratorContext/WalletCardGenerator
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WalletCardGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import com.ryantenney.passkit4j.Pass;
import com.ryantenney.passkit4j.PassResource;
import com.ryantenney.passkit4j.PassSerializationException;
import com.ryantenney.passkit4j.PassSerializer;
import com.ryantenney.passkit4j.model.*;
import com.ryantenney.passkit4j.sign.PassSigner;
import com.ryantenney.passkit4j.sign.PassSignerImpl;
import com.ryantenney.passkit4j.sign.PassSigningException;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            throws ServletException, IOException, PassSerializationException, FileNotFoundException {
//        processRequest(request, response);

        
        if (request.getRequestURI().endsWith("/WalletCardGenerator")) {
            response.setContentType("text/html");
//            response.getWriter().print("<html><head></head><body><h1>Welcome!</h1><p>This is a very cool page!</p></body></html>");
            request.getRequestDispatcher("/testget.jsp").forward(request, response);
            ;
            
        } else if (request.getRequestURI().endsWith("/WalletCardGenerator/wallet")) {
            String user = request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1);

            response.setContentType("application/json");

            try {
                pass(user);
            } catch (PassSigningException ex) {
                Logger.getLogger(WalletCardGenerator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(WalletCardGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
            response.getWriter().print("{\n" +
                    "  \"user\":" + user + ",\n" +
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

    
    public  int pass (String user) throws FileNotFoundException, PassSigningException, PassSerializationException, ParseException {
        File catalinaBase = new File(System.getProperty("catalina.base")).getAbsoluteFile();

        String pass_strings = catalinaBase+"/webapps/WalletCardGenerator-1.0/storecard/en.lproj/pass.strings";
        String icon_png = catalinaBase+"/webapps/WalletCardGenerator-1.0/storecard/icon.png";
        String icon_2x_png = catalinaBase+"/webapps/WalletCardGenerator-1.0/storecard/icon@2x.png";
        String logo_png = catalinaBase+"/webapps/WalletCardGenerator-1.0/storecard/logo.png";
        String logo_2x_png = catalinaBase+"/webapps/WalletCardGenerator-1.0/storecard/logo@2x.png";
        String strip_png = catalinaBase+"/webapps/WalletCardGenerator-1.0/storecard/strip.png";
        String strip_2x_png = catalinaBase+"/webapps/WalletCardGenerator-1.0/storecard/strip@2x.png";
        String CertP12 = catalinaBase+"/webapps/WalletCardGenerator-1.0/Certificates.p12";
        String CertApple = catalinaBase+"/webapps/WalletCardGenerator-1.0/AppleWWDRCA.cer";
        String pkpass = catalinaBase+"/webapps/WalletCardGenerator-1.0/StoreCard.pkpass";

        Pass pass = new Pass()
			.teamIdentifier("Fora")
			.passTypeIdentifier("pass.ua.fora.DemoCard")
			.organizationName("Fora Club")
			.description(user)
			.serialNumber("0294154197253")
                        .expirationDate(new SimpleDateFormat("dd/MM/yyyy").parse("01/01/2100"))
			.locations(
				new Location(43.145863, -77.602690).relevantText("Фора"),
				new Location(43.131063, -77.636425).relevantText("Фора")
			)
			.barcode(new Barcode(BarcodeFormat.PDF417, "0294154197253"))
			.barcodes(
					new Barcode(BarcodeFormat.CODE128, "0294154197253"),
					new Barcode(BarcodeFormat.PDF417, "0294154197253"))
			.logoText("Фора клуб")
			.foregroundColor(Color.WHITE)
			.backgroundColor(new Color(0x2c, 0x97, 0xdb))
			.files(
				new PassResource("en.lproj/pass.strings", new File(pass_strings)),
				new PassResource(icon_png),
				new PassResource(icon_2x_png),
				new PassResource(logo_png),
				new PassResource(logo_2x_png),
				new PassResource(strip_png),
				new PassResource(strip_2x_png)
			)
			.nfc(new NFC("test"))
			.passInformation(
				new StoreCard()
					.headerFields(
						new NumberField("Баланс", "balance_label", 0)
							.textAlignment(TextAlignment.RIGHT)
							.currencyCode("UAH")
					)
					.auxiliaryFields(
						new TextField("Уровень", "level_label", "level_gold"),
						new TextField("Предпочтение", "usual_label", "+380675555555")
					)
					.backFields(
						new TextField("terms", "terms_label", "terms_value")
					)
			);


		PassSigner signer = PassSignerImpl.builder()
			.keystore(new FileInputStream(CertP12), "Password")
			.intermediateCertificate(new FileInputStream(CertApple))
			.build();

		PassSerializer.writePkPassArchive(pass, signer, new FileOutputStream(pkpass));
        return 0;
                
    }
    
}
