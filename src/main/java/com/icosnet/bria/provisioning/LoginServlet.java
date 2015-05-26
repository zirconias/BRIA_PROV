package com.icosnet.bria.provisioning;

import com.provisionning.bean.LicenceBria;
import com.provisionning.bean.SipAccount;
import com.provisionning.business.ProvisionningActions;
import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by mohamed.drissi on 25/05/2015.
 */
public class LoginServlet  extends ProvisioningServlet  {

    public static final String PARAMETER_ENCODING = "UTF-8";
    private static final String EMPTY_STRING = "";
    private static final String XML = "xml";
    private static final String OUTPUT_TYPE = "output_type";
    private static final String USERNAME = "username";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    protected void processRequest(HttpServletRequest req,HttpServletResponse resp)throws javax.servlet.ServletException,java.io.IOException{
        PrintWriter out = resp.getWriter();
        String reqType = EMPTY_STRING;

        try{
            Map<String, String> parameters;
            try{
                parameters= getParameterMapFromBody(req.getReader());
            }catch (java.io.IOException error){
                throw new FailureDataException("Cannot extract parameters");
            }
            reqType = parameters.get(OUTPUT_TYPE);
            String username =parameters.get("username");
            String password  = parameters.get("password");
            System.out.println(ProvisionningActions.checkUser(username, password));
            SipAccount sipAccount = ProvisionningActions.checkSipConfig(username);
            LicenceBria licenceBria = ProvisionningActions.chekLicenceConfig(username);

        }catch(FailureDataException e){
            handleLoginException(out, reqType, e);
        }
    }

    protected static Map<String, String> getParameterMapFromBody(BufferedReader br) throws java.io.IOException {
        Map<String, String> parameters = new Hashtable<String, String>();
        String line = br.readLine();
        if (line == null) {
            return parameters;
        }

        if (!line.contains("<?xml version=")) {
            // LOG.debug("Parsing line: " + line);
            String[] pairs = line.split("\\&");
            for (String pair : pairs) {
                String[] fields = pair.split("=");
                if (fields.length == 2) {
                    String name = URLDecoder.decode(fields[0], PARAMETER_ENCODING);
                    String value = URLDecoder.decode(fields[1], PARAMETER_ENCODING);
                    parameters.put(name.toLowerCase(), value);
                }
            }
            parameters.put(OUTPUT_TYPE, "txt");
        } else {
            String fulltext = line;
            while (br.ready()) {
                line = br.readLine();
                fulltext = fulltext + line;
            }
            // LOG.debug("Parsing fulltext: " + fulltext);
            parseXmlParams(parameters, fulltext);
            parameters.put(OUTPUT_TYPE, XML);
        }
        // LOG.debug("Parsed params: " + parameters);

        return parameters;
    }


    private static void parseXmlParams(Map<String, String> params, String xmlRequest) {
        SAXReader reader = new SAXReader();
        reader.setEncoding(PARAMETER_ENCODING);
        try {
            Element rootElem = reader.read(new StringReader(xmlRequest)).getRootElement();
            for (Object elemObj : rootElem.elements()) {
                Element elem = (Element) elemObj;
                if ("login".equals(elem.getName())) {
                    for (Object attrObj : elem.attributes()) {
                        Attribute attr = (Attribute) attrObj;
                        params.put(attr.getName(), attr.getStringValue());
                    }
                }
            }
        } catch (DocumentException e) {
            System.out.println("Error parsing XML request: " + xmlRequest);
        }
        params.put(USERNAME, params.get("user"));
    }

    private static void handleLoginException(PrintWriter out, String reqType, Exception e) {
        System.out.println("Login error: " + e.getMessage());
        if (reqType == XML) {
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<cpc_mobile version=\"1.0\">\n<login_response>\n"
                    + "<status success=\"false\" error_text=\"" + e.getMessage() + "\"/>\n"
                    + "</login_response>\n</cpc_mobile>\n");
        } else {
            buildFailureResponse(out, e.getMessage());
        }
    }
}
