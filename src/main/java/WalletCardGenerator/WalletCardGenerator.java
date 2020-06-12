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
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.ryantenney.passkit4j.Pass;
import com.ryantenney.passkit4j.PassResource;
import com.ryantenney.passkit4j.PassSerializationException;
import com.ryantenney.passkit4j.PassSerializer;
import com.ryantenney.passkit4j.model.*;
import com.ryantenney.passkit4j.sign.PassSigner;
import com.ryantenney.passkit4j.sign.PassSignerImpl;
import com.ryantenney.passkit4j.sign.PassSigningException;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.siebel.data.SiebelBusComp;
import com.siebel.data.SiebelBusObject;
import com.siebel.data.SiebelDataBean;
import com.siebel.data.SiebelException;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpSession;
import org.krysalis.barcode4j.servlet.BarcodeServlet;
import org.krysalis.barcode4j.webapp.BarcodeRequestBean;

/**
 *
 * @author VashurinVlad
 */


@WebServlet(name = "WalletCardGenerator", description = "REST Wallet Card Generator Servlet", urlPatterns = {"/WalletCardGenerator", "/WalletCardGenerator/wallet"})
public   class WalletCardGenerator extends HttpServlet {

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    
    static String SiebelConnectString;
    static String SiebelUser;
    static String SiebelUserPassword; 
    static String sPass, keyPassword, logoText, organizationName, teamIdentifier, passTypeIdentifier;
    static String sFirstName;
    static String sLastName;
    static String sMiddleName;
    BarcodeRequestBean BarCodeBean;
    static File jarFile;
    static String jarFilePath;
    File catalinaBase = new File(System.getProperty("catalina.base")).getAbsoluteFile();
        
     @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, PassSerializationException, FileNotFoundException {
       
        if (request.getRequestURI().contains("/wallet/generate")) {
            String user = request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try {
                GetProperties () ;
            } catch (URISyntaxException ex) {
                Logger.getLogger(WalletCardGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                pass(user);
            } catch (PassSigningException ex) {
                Logger.getLogger(WalletCardGenerator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(WalletCardGenerator.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            int nSessions  = GetContactInfo(user); 
//            BarCodeBean.toURL();
            
            String[][] data;
            response.getWriter().print("{\n" +
                    "  \"user\":" + user + ",\n" +
                    "  \"FirstName\":" + sFirstName + ",\n" +     
                    "  \"MiddleName\":" + sMiddleName + ",\n" +  
                    "  \"LastName\":" + sLastName + "\n" +  
                    "}");
        } else if  (request.getRequestURI().contains("/wallet/get")){
            
            // Выдача документа клиенту.
        String user = request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1);    
        String pkpass = catalinaBase+"/webapps/siebel-wallet/"+user+".pkpass";    
        ServletContext context = getServletContext();
        ServletOutputStream out = response.getOutputStream();
        byte[] byteArray = null;
        Path path = Paths.get(pkpass);
        byteArray = Files.readAllBytes(path);
        //данный контент type говорит что будет файл в формате pkpass
        response.setContentType("application/vnd.apple.pkpass");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + user+".pkpass" + "\"");
        out.write(byteArray);
        out.flush();
        out.close();
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
//          request.getRequestDispatcher("/testpost.jsp").forward(request, response);
    }

    

    public  int pass (String user) throws FileNotFoundException, PassSigningException, PassSerializationException, ParseException {
        

        String pass_strings =   catalinaBase+"/webapps/siebel-wallet/storecard/en.lproj/pass.strings";
        String icon_png =       catalinaBase+"/webapps/siebel-wallet/storecard/icon.png";
        String icon_2x_png =    catalinaBase+"/webapps/siebel-wallet/storecard/icon@2x.png";
        String logo_png =       catalinaBase+"/webapps/siebel-wallet/storecard/logo.png";
        String logo_2x_png =    catalinaBase+"/webapps/siebel-wallet/storecard/logo@2x.png";
        String strip_png =      catalinaBase+"/webapps/siebel-wallet/storecard/strip.png";
        String strip_2x_png =   catalinaBase+"/webapps/siebel-wallet/storecard/strip@2x.png";
        String CertP12 =        catalinaBase+"/webapps/siebel-wallet/Certificates.p12";
        String CertApple =      catalinaBase+"/webapps/siebel-wallet/AppleWWDRCA.cer";
        String pkpass =         catalinaBase+"/webapps/siebel-wallet/"+user+".pkpass";

        Pass pass = new Pass()
			.teamIdentifier(teamIdentifier)
			.passTypeIdentifier(passTypeIdentifier)
			.organizationName(organizationName)
                        
			.description("This card is generated for " + user)
			.serialNumber("9bcc08b")
                        .expirationDate(new SimpleDateFormat("dd/MM/yyyy").parse("01/01/2100"))
			.locations(
				new Location(43.145863, -77.602690).relevantText(organizationName),
				new Location(43.131063, -77.636425).relevantText(organizationName)
			)
			.barcode(new Barcode(BarcodeFormat.PDF417, "0294154197253"))
			.barcodes(
					new Barcode(BarcodeFormat.CODE128, "0294154197253"),
					new Barcode(BarcodeFormat.PDF417, "0294154197253"))
			.logoText(logoText)
			.foregroundColor(Color.WHITE)
			.backgroundColor(new Color(0x2c, 0x97, 0xdb))
			.files(
				new PassResource("en.lproj/pass.strings", new File(pass_strings)),
                                new PassResource("ru.lproj/pass.strings", new File(pass_strings)),
				new PassResource(icon_png),
				new PassResource(icon_2x_png),
				new PassResource(logo_png),
				new PassResource(logo_2x_png),
				new PassResource(strip_png),
				new PassResource(strip_2x_png)
			)
			.passInformation(
				new StoreCard()
					.headerFields(
						new NumberField("Balance", "Баланс", 0)
							.textAlignment(TextAlignment.RIGHT)
							.currencyCode("UAH")
					)
					.auxiliaryFields(
						new TextField("owner","Владелец карты" , sFirstName +" "+sLastName),
						new TextField("usual", "Препочтительно", "+380675555555")
					)
					.backFields(
						new TextField("terms", "Соглашение", "terms_value")
					)
			);

                char[] password = keyPassword.toCharArray();
                
                try {
                    FileInputStream is =new FileInputStream(new File(CertP12));
                    KeyStore ks = KeyStore.getInstance("PKCS12");
                    
                    ks.load(is, password);
                    } catch (Exception e) {
                    e.printStackTrace();
                    }

		PassSigner signer = PassSignerImpl.builder()
			.keystore(new FileInputStream(CertP12) , keyPassword )
			.intermediateCertificate(new FileInputStream(CertApple))
			.build();

		PassSerializer.writePkPassArchive(pass, signer, new FileOutputStream(pkpass));
        return 0;
                
    }
    
 int GetContactInfo(String sROWID) throws FileNotFoundException, IOException
 {
    int n=0;
    try {
            SiebelDataBean sblConnect = new SiebelDataBean();
            sblConnect.login(SiebelConnectString, SiebelUser, SiebelUserPassword, "enu");

            SiebelBusObject BO = sblConnect.getBusObject("Contact");
            SiebelBusComp BC = BO.getBusComp("Contact");
            BC.clearToQuery();
            BC.activateField("Account Location");
            BC.activateField("Account Number");
            BC.activateField("Account Organization");
            BC.activateField("First Name");
            BC.activateField("Last Name");
            BC.activateField("Work Phone #");
            BC.activateField("Middle Name");
            BC.activateField("Row Id");
            BC.setSearchSpec("Row Id", sROWID);
            BC.executeQuery(true);

            if(BC.firstRecord())
            {
                sFirstName=null;
                sLastName=null;
                sMiddleName=null;
//                if (BC.nextRecord()){
                sFirstName = BC.getFieldValue("First Name");
                sLastName = BC.getFieldValue("Last Name");
                sMiddleName = BC.getFieldValue("Middle Name");
                System.out.println("Contact: "+ sFirstName +" "+sMiddleName+" "+sLastName);
            }
            BC = null;
            BO = null;
            sblConnect.logoff();

        }
        catch (SiebelException e)
        {           
                e.printStackTrace();
        }


        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        String strDate = dateFormat.format(date);        
                
    return n;
                
 }

void GetProperties () throws IOException, URISyntaxException
{
    //файл, который хранит свойства нашего проекта
    //создаем объект Properties и загружаем в него данные из файла.        
        String jarFilePath = catalinaBase+"/webapps/siebel-wallet/";
        System.out.println(jarFilePath);
        jarFilePath=jarFilePath+"siebel.properties";

        try {
                File myObj = new File(jarFilePath);
                InputStream inp;   
        FileInputStream fileInputStream = new FileInputStream(jarFilePath);          
    
        Properties properties = new Properties();
        // load a properties file
        properties.load(fileInputStream);
        // get the property value and print it out
        SiebelConnectString =   properties.getProperty("siebel.connectstring");
        SiebelUser=             properties.getProperty("siebel.user");
        SiebelUserPassword=     properties.getProperty("siebel.password");
        organizationName =      properties.getProperty("wallet.organizationName");
        passTypeIdentifier =    properties.getProperty("wallet.passTypeIdentifier");
        teamIdentifier =        properties.getProperty("wallet.teamIdentifier");
        logoText =              properties.getProperty("wallet.logoText");
        keyPassword =           properties.getProperty("wallet.keyPassword");
        
          Scanner myReader = new Scanner(myObj);
          while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            System.out.println(data);
          }
          myReader.close();
        } catch (FileNotFoundException e) {
          System.out.println("An error occurred.");
          e.printStackTrace();
        }
  
        InputStream inp;   
        inp = WalletCardGenerator.class.getResourceAsStream("siebel.properties");
        System.out.println(inp != null);  
        System.out.println(jarFilePath);
}
}
