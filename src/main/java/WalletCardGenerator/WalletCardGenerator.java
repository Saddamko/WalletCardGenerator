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

/**
* Сервлет для получения документа с сервера.
*
* <p>Потомки этого класса должны реализовать следующие методы:</p>
* <dl>
* <dt>populateIdentity(HttpServletRequest)</dt>
* <dd>Чтение из запроса данных, которые будут участвовать в поиске
* документа.</dd>
* <dt>getDocument(HttpServletRequest, Object)</dt>
* <dd>Получение непосредственно документа на клиента.</dd>
* </dl>
*
* <p>
* Параметры: либо набор параметров для получения документа с EJB (поиск
* на сервере по этим параметрам), либо параметр с именем
* AbstractGetFileAJAXWay#DSID (идентификатор, полученный после первого запроса
* сервера) для непосредственной выдачи документа клиенту.
* </p>
*
* <p>Варианты JSON-ответа первого шага:</p>
* <ul>
* <li>Успешное получение документа с EJB:<br/>
* <pre>{"result":"success","dsid":"362547383846347775"}</pre>
* </li>
* <li>Ошибка получения документа с EJB:<br/>
* <pre>{"result":"failure","reason":"Нет связи с сервером!"}</pre>
* </li>
* </ul>
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
    static String sPass, keyPassword, logoText, organizationName;
    static String sFirstName;
    static String sLastName;
    static String sMiddleName;
    BarcodeRequestBean BarCodeBean;
    static File jarFile;
    static String jarFilePath;
    public static final String DSID = "dsid";
    File catalinaBase = new File(System.getProperty("catalina.base")).getAbsoluteFile();

    public  class GetFileAJAXWayException extends Exception {
    public GetFileAJAXWayException() { super(); }
    public GetFileAJAXWayException(String msg) { super(msg); }
    public GetFileAJAXWayException(Throwable thw) { super(thw); }
    public GetFileAJAXWayException(String msg, Throwable thw) { super(msg, thw); }
    }
    
    public interface IFileContainer extends Serializable {
    public String getFileName();
    public String getContentType();
    public long getFileLength();
    public byte[] getFileContent();
    }

    /**
     *
     * @param req
     * @param identity
     * @return
     * @throws GetFileAJAXWayException
     */
     protected  IFileContainer getDocument(HttpServletRequest req,Object identity) throws GetFileAJAXWayException
     {
        return null;
         
     };
     protected  Object populateIdentity(HttpServletRequest req) throws GetFileAJAXWayException
     {
        return null;
     }
     ;
     
         
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
        //данный контент type говорит что будет файл в формате excel
        response.setContentType("application/vnd.apple.pkpass");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + user+".pkpass" + "\"");
        out.write(byteArray);
        out.flush();
        out.close();
        }
        }

    
    /**
    * Получение документа с сервера и сохранение его в сессии для последующего забора.
    * @param identity Объект с данными, которые участвуют в поиске документа. * @param req HTTP-запрос.
    * @param resp HTTP-ответ.
    * @throws ServletException
    * @throws IOException
    */
    private void retrieveDocument
    (Object identity, HttpServletRequest req, HttpServletResponse resp)
    throws IOException {

    // Сессия.
    HttpSession session = req.getSession(false);

    // Получение документа с помощью метода, реализованного в наследнике.
    IFileContainer cont;
    try {
    cont = getDocument(req, identity);
    } catch (Exception e) {
    final String err = "Ошибка получения документа с сервера: "
    + e.getMessage() + "!";
    log(err);
    sendFailureReply(err, resp);
    return;
    }
    }
    
    /**
        * Выдача ранее полученного документа клиенту.
        * @param dsid Идентификатор документа в сессии.
        * @param req HTTP-запрос.
        * @param resp HTTP-ответ.
        * @throws ServletException
        * @throws IOException
        */
        private void deliverDocument
        (String dsid, HttpServletRequest req, HttpServletResponse resp)
        throws GetFileAJAXWayException, IOException {

        // Сессия.
        HttpSession session = req.getSession(false);

        // Есть ли такой документ?
        Object sessobj = session.getAttribute(dsid);
        if (sessobj == null) {
        throw new GetFileAJAXWayException("Нет объекта \"" + DSID + "\" в сессии!");
        } else if (!(sessobj instanceof IFileContainer)) {
        throw new GetFileAJAXWayException("Неверный объект \"" + DSID + "\" в сессии!");
        }

        // Удаление документа из сессии.
        session.removeAttribute(dsid);

        // Документ.
        IFileContainer document = (IFileContainer) sessobj;

        // Выдача файла.
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentLength((int) document.getFileLength());
        resp.setContentType(document.getContentType());
        resp.setHeader("Content-Transfer-Encoding", "binary");
        /* // По стандарту -- в IE не работает
        String filename = "=?windows-1251?Q?" + new org.apache.commons.codec.net.QuotedPrintableCodec().encode(document.getFileName(), "Cp1251") + "?=";
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        */
        /* // Обещали работу в IE -- фиг
        String filename = java.net.URLEncoder.encode(document.getFileName(), "Cp1251").replaceAll("\\+", " ");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        */
        /**/ // По-тупому
//        String filename = document.getFileName();
//        String filename = document.getFileName();
//        int dotpos = filename.lastIndexOf('.');
//        if (dotpos > -1)
//        filename = "file." + filename.substring(dotpos + 1);
//        else
        String filename = "file.dat";
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        /**/
        OutputStream out = resp.getOutputStream();
        out.write(document.getFileContent());
        out.flush();
        out.close();
        }
        
        /**
        * Уникальный номер документа, положенного в сессию для последующего забора.
        *
        * @param trashheap Набор произвольных чисел для генерации случайного результата.
        * Может быть <tt>null</tt>, в этом случае в генерации не участвует.
        * @return Уникальный идентификатор документа в сессии.
        */
        private String dsid(long[] trashheap) {
        long dsid = System.currentTimeMillis();
        if (trashheap != null && trashheap.length > 0)
        for (int i = 0; i < trashheap.length; i++)
        dsid ^= trashheap[i];
        return Long.toString(Math.abs(new Random(dsid).nextLong()), 10);
        }

        /**
        * Экранирование символов в строках для присоединения их к строке формата JSON.
        * @param subject Исходная строка.
        * @return Результат.
        */
        private String escapeJSON(String subject) {
        if (subject == null || subject.length() == 0)
        return "";
        return subject.replaceAll("\"", "\\\"")
        .replaceAll("\\\\", "\\\\")
        .replaceAll("[\n\r]", "\\\\n");
        }
        
        
        /**
        * Формирование и отправка JSON-сообщения об успешном завершении работы.
        * @param dsid Идентификатор документа в сессии, который (документ) впоследствие можно забрать.
        * @param resp HTTP-ответ.
        * @throws ServletException
        * @throws IOException
        */
        private void sendSuccessReply(String dsid, HttpServletResponse resp)
        throws IOException {
        String dsidJSON = "{\"result\":\"success\",\"dsid\":\""
        + escapeJSON(dsid) + "\"}";

        sendAnyReply(dsidJSON, resp);
        }
        
        /**
        * Отправка сообщения клиенту.
        * @param json Отправляемая строка.
        * @param resp HTTP-ответ.
        * @throws IOException
        */
        private void sendAnyReply(String json, HttpServletResponse resp)
        throws IOException {

        final byte[] result_bytes = json.getBytes("UTF-8");
        final int CHUNK = 1024;
        final BufferedOutputStream output = new BufferedOutputStream(
        resp.getOutputStream(), CHUNK);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader("Content-Encoding", "UTF-8");
        resp.setContentType("text/plain; charset=UTF-8");
        resp.setContentLength(result_bytes.length);

        int bytes_pos = 0, bytes_chunk = 0;
        do {
        bytes_chunk = bytes_pos + CHUNK <= result_bytes.length
        ? CHUNK
        : result_bytes.length - bytes_pos;
        output.write(result_bytes, bytes_pos, bytes_chunk);
        bytes_pos += bytes_chunk;
        } while (bytes_pos < result_bytes.length);
        output.flush();
        output.close();
        }

        /**
        * Формирование и отправка JSON-сообщения об ошибке работы.
        * @param reason Строка ошибки.
        * @param resp HTTP-ответ.
        * @throws ServletException
        * @throws IOException
        */
        private void sendFailureReply(String reason, HttpServletResponse resp)
        throws IOException {
        String reasonJSON = "{\"result\":\"failure\",\"reason\":\""
        + escapeJSON(reason) + "\"}";

        sendAnyReply(reasonJSON, resp);
        }

        
        
        /**
        * Заполнение объекта необходимыми для поиска данными.
        * @param req HTTP-запрос.
        * @return Объект, который затем будет передан в {@link #getDocument(Object)}
        * для поиска документа.
        * @throws GetFileAJAXWayException Если в запросе недостаточно параметров
        * для поиска документа.
        */
//        protected abstract Object populateIdentity(HttpServletRequest req)
//        throws GetFileAJAXWayException;

        /**
        * Запрос документа с сервера, используя ранее созданный контейнер
        * с необходимыми для поиска данными.
        * @param req HTTP-запрос.
        * @param identity Параметры поиска документа на сервере.
        * @return Документ.
        * @throws GetFileAJAXWayException Невозможность возврата документа.
        */
//        protected abstract IFileContainer getDocument(HttpServletRequest req,
//        Object identity) throws GetFileAJAXWayException;

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
        

        String pass_strings = catalinaBase+"/webapps/siebel-wallet/storecard/en.lproj/pass.strings";
        String icon_png = catalinaBase+"/webapps/siebel-wallet/storecard/icon.png";
        String icon_2x_png = catalinaBase+"/webapps/siebel-wallet/storecard/icon@2x.png";
        String logo_png = catalinaBase+"/webapps/siebel-wallet/storecard/logo.png";
        String logo_2x_png = catalinaBase+"/webapps/siebel-wallet/storecard/logo@2x.png";
        String strip_png = catalinaBase+"/webapps/siebel-wallet/storecard/strip.png";
        String strip_2x_png = catalinaBase+"/webapps/siebel-wallet/storecard/strip@2x.png";
        String CertP12 = catalinaBase+"/webapps/siebel-wallet/Certificates.p12";
        String CertApple = catalinaBase+"/webapps/siebel-wallet/AppleWWDRCA.cer";
        String pkpass = catalinaBase+"/webapps/siebel-wallet/"+user+".pkpass";

        Pass pass = new Pass()
			.teamIdentifier("Areon Consulting")
			.passTypeIdentifier("pass.ua."+organizationName+".DemoCard")
			.organizationName(organizationName)
			.description("This card is generated for " + user)
			.serialNumber(user)
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
						new NumberField("Balance", "Баланс", 0)
							.textAlignment(TextAlignment.RIGHT)
							.currencyCode("UAH")
					)
					.auxiliaryFields(
						new TextField("owner","Владелец карты" , sFirstName +" + "+sLastName),
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
        SiebelConnectString = properties.getProperty("siebel.connectstring");
        SiebelUser=properties.getProperty("siebel.user");
        SiebelUserPassword=properties.getProperty("siebel.password");
        organizationName = properties.getProperty("wallet.organizationName");
        logoText = properties.getProperty("wallet.logoText");
        keyPassword = properties.getProperty("wallet.keyPassword");
        
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
