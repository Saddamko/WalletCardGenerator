/*
* @author Вашурин Владимир
* @version 2020.07.21.1
* Сервлет для Tomcat сервера
* При помощи библиотеки passkit4j выполняется генерация файлов для кошельков
* Google Pay — мобильное платёжное приложение для операционной системы Android.
* Во вкладке «Карты», хранятся карты программ лояльности, предложения и подарочные карты.
* Для получения параметров клиента, помещаемых в кошелек,
* выполняется запрос в Siebel
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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import org.krysalis.barcode4j.webapp.BarcodeRequestBean;

/** 
 * Сервлет для Tomcat сервера <b>WalletCardGenerator</b>
 * Назначение: генерация карт для электронных кошельков <b>Apple Wallet</b> и <b>Google Pay</b>
 * @author Владимир Вашурин
 * @version 1.0.1
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
    
    /** Строка соединения с базой Siebel, считывается с файла siebel.properties */
    static String SiebelConnectString;
    /** Имя пользователя для соединения с базой Siebel, считывается с файла siebel.properties*/
    static String SiebelUser;
    /** Пароль пользователя для соединения с базой Siebel, считывается с файла siebel.properties*/
    static String SiebelUserPassword; 
    /** Параметры для файла pkpass, считываются с файла siebel.properties*/
    static String sPass, keyPassword, logoText, organizationName, teamIdentifier, passTypeIdentifier, certAlias, backfld1, backfld2, mobileURL, prefered;
    /** Имя клиента для pkpass */
    static String sFirstName;
    /** Фамилия клиента для pkpass */
    static String sLastName;
    /** Отчество клиента для pkpass */
    static String sMiddleName;
    /** Компонент для генерашии штрихкодов */
    BarcodeRequestBean BarCodeBean;
    static File jarFile;
    static String jarFilePath;
    /** Определение физического каталога Tomcat Catalina для сохранения выходных файлов */
    File catalinaBase = new File(System.getProperty("catalina.base")).getAbsoluteFile();
        

/**
* Обработка HTTP <code>GET</code> метода.
* @param request servlet запрос
* @param response servlet ответ
* @throws ServletException при появлении servlet-specific ошибок
* @throws IOException  при ошибках ввода-вывода
*/
@Override
protected void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, PassSerializationException, FileNotFoundException 
{
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
 * Обработка HTTP <code>POST</code> метода.
 * @param request servlet запрос
 * @param response servlet ответ
 * @throws ServletException при появлении servlet-specific ошибок
 * @throws IOException  при ошибках ввода-вывода
 */
@Override
    protected void doPost (HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
//          request.getRequestDispatcher("/testpost.jsp").forward(request, response);
}  

/** 
 * Метод <b>pass</b>
 * Назначение: генерация файла pkpass для электронных кошельков <b>Apple Wallet</b>
 * @param user Row ID записи Контакта в Siebel
 * @return Код возврата с признаком успеха (пока все время 0)
 * @throws FileNotFoundException  при ошибках ввода-вывода
 * @throws PassSigningException  при ошибках формирования цифровой подписи
 * @throws PassSerializationException  при ошибках формирования цифровой подписи 
 * @throws ParseException  при ошибках формирования цифровой подписи 
 * @author Владимир Вашурин
 * @version 1.0.1
*/
    public  int pass (String user) throws FileNotFoundException, PassSigningException, PassSerializationException, ParseException {
        

        String pass_strings =   catalinaBase+"/webapps/siebel-wallet/storecard/en.lproj/pass.strings";
        String icon_png =       catalinaBase+"/webapps/siebel-wallet/storecard/icon.png";
        String icon_2x_png =    catalinaBase+"/webapps/siebel-wallet/storecard/icon@2x.png";
        String icon_3x_png =    catalinaBase+"/webapps/siebel-wallet/storecard/icon@3x.png";
        String logo_png =       catalinaBase+"/webapps/siebel-wallet/storecard/logo.png";
        String logo_2x_png =    catalinaBase+"/webapps/siebel-wallet/storecard/logo@2x.png";
        String logo_3x_png =    catalinaBase+"/webapps/siebel-wallet/storecard/logo@3x.png";
        String strip_png =      catalinaBase+"/webapps/siebel-wallet/storecard/strip.png";
        String strip_2x_png =   catalinaBase+"/webapps/siebel-wallet/storecard/strip@2x.png";
        String CertP12 =        catalinaBase+"/webapps/siebel-wallet/Certificates.p12";
        String CertApple =      catalinaBase+"/webapps/siebel-wallet/AppleWWDRCA.cer";
        String pkpass =         catalinaBase+"/webapps/siebel-wallet/"+user+".pkpass";

        Pass pass = new Pass()
			.teamIdentifier(teamIdentifier)
			.passTypeIdentifier(passTypeIdentifier)
			.organizationName(organizationName)                      
			.description("Карта сгенерирована для клиента " + user)
			.serialNumber(user)
                        .expirationDate(new SimpleDateFormat("dd/MM/yyyy").parse("01/01/2100"))
//			.locations(
//				new Location(43.145863, -77.602690).relevantText(organizationName),
//				new Location(43.131063, -77.636425).relevantText(organizationName)
//			)
			.barcode(new Barcode(BarcodeFormat.PDF417, "0294154197253"))
			.barcodes(
					new Barcode(BarcodeFormat.CODE128, "0294154197253")
//					new Barcode(BarcodeFormat.PDF417, "0294154197253")
                                        )
			.logoText(logoText)
			.foregroundColor(Color.WHITE)
			.backgroundColor(new Color(0x2c, 0x97, 0xdb))
			.files(
				new PassResource("en.lproj/pass.strings", new File(pass_strings)),
                                new PassResource("ru.lproj/pass.strings", new File(pass_strings)),
                                new PassResource("uk.lproj/pass.strings", new File(pass_strings)),
				new PassResource(icon_png),
				new PassResource(icon_2x_png),
//                                new PassResource(icon_3x_png),
				new PassResource(logo_png),
				new PassResource(logo_2x_png),
//                                new PassResource(logo_3x_png)
				new PassResource(strip_png),
				new PassResource(strip_2x_png)
			)
			.passInformation(
				new StoreCard()
					.headerFields(
						new NumberField("Balance","Баланс",  0)
							.textAlignment(TextAlignment.RIGHT)
							.currencyCode("UAH")
					)
                                        .primaryFields(
                                                new TextField("website","Осн. поля",  "UNIQA")
                                                
                                        )
					.auxiliaryFields(
						new TextField("owner" ,"Владелец карты", sFirstName +" "+sLastName),
						new TextField("prefered","Доп. поля",  prefered)                                                
					)
					.backFields(
                                                new TextField("backfld1","Обр. поле", backfld1),
                                                new TextField("backfld2","Поддержка", backfld2),
                                                new TextField("mobile","Мобильное приложение",  mobileURL),
                                                new TextField("website","Соглашение", "terms_value")
					)
			);

                char[] password = keyPassword.toCharArray();
                
                try {
                    FileInputStream is =new FileInputStream(new File(CertP12));
                    KeyStore ks = KeyStore.getInstance("PKCS12");  
                    ks.load(is, password);
                    } catch (Exception e) {e.printStackTrace();}

		PassSigner signer = PassSignerImpl.builder()
			.keystore(new FileInputStream(CertP12) , keyPassword )
			.intermediateCertificate(new FileInputStream(CertApple))
                        .alias(certAlias)
			.build();

		PassSerializer.writePkPassArchive(pass, signer, new FileOutputStream(pkpass));
        return 0;
                
}
    
 /** 
 * Метод <b>GetContactInfo</b>
 * Назначение: Получение данных о контакте из Siebel по полученному в параметре идентификаторе клиента
 * @author Владимир Вашурин
 * @version 1.0.1
*/
 public int GetContactInfo(String sROWID) throws FileNotFoundException, IOException
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
                n=0; //Data is aquired
            } else n=-1;
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

 
  /** 
 * Метод <b>GetProperties</b>
 * Назначение: Получение параметров для подключения к Siebel и формирования кошелька из файла siebel.properties
 * для обеспечения возможности конфигурирования без изменения кода программы
 * и исключения секретных данных из программного кода
 * @author Владимир Вашурин
 * @version 1.0.1
*/
void GetProperties () throws IOException, URISyntaxException
{
//файл, который хранит свойства нашего проекта
//создаем объект Properties и загружаем в него данные из файла.        
String jarFilePath = catalinaBase+"/webapps/siebel-wallet/";
System.out.println(jarFilePath);
jarFilePath=jarFilePath+"siebel.properties";

try {
    File myObj = new File(jarFilePath);
    FileInputStream fileInputStream = new FileInputStream(jarFilePath);          

    Properties properties = new Properties();
    // load a properties file
    properties.load(fileInputStream);
    // get the property value and print it out
    SiebelConnectString =   properties.getProperty("siebel.connectstring");
    SiebelUser =            properties.getProperty("siebel.user");
    SiebelUserPassword =    properties.getProperty("siebel.password");
    organizationName =      properties.getProperty("wallet.organizationName");
    passTypeIdentifier =    properties.getProperty("wallet.passTypeIdentifier");
    teamIdentifier =        properties.getProperty("wallet.teamIdentifier");
    logoText =              properties.getProperty("wallet.logoText");
    keyPassword =           properties.getProperty("wallet.keyPassword");
    certAlias =             properties.getProperty("wallet.certAlias");
    backfld1 =              properties.getProperty("wallet.backfld1");
    backfld2 =              properties.getProperty("wallet.backfld2");
    mobileURL =             properties.getProperty("wallet.mobileURL");
    prefered =             properties.getProperty("wallet.prefered");

    fileInputStream.close();
    } catch (FileNotFoundException e) 
        {
        System.out.println("An error occurred.");
        e.printStackTrace();
        }
}//GetProperties () end
}//main class end
