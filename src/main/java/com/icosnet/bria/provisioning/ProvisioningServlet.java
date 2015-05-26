package com.icosnet.bria.provisioning;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServlet;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by mohamed.drissi on 25/05/2015.
 */
public class ProvisioningServlet extends HttpServlet {

    public static final String DATA_SECTION = "[DATA]";
    public static final String EQUAL_SIGN = "=";
    public static final String QUOTE_CHAR = "\"";


    protected static void buildSuccessResponse(PrintWriter out, Map<String, String> settings) {
        out.println(DATA_SECTION);
        out.println("Success=1");
        out.println(StringUtils.EMPTY);
        out.println("[SETTINGS]");
        for (Map.Entry<String, String> e : settings.entrySet()) {
            out.println(e.getKey() + EQUAL_SIGN + QUOTE_CHAR + e.getValue() + QUOTE_CHAR);
        }
    }

    protected static void buildFailureResponse(PrintWriter out, String errorMessage) {
        out.println(DATA_SECTION);
        out.println("Success=0");
        if (errorMessage != null) {
            out.println("Failure=" + QUOTE_CHAR + errorMessage + QUOTE_CHAR);
        }
    }

}
