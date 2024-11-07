package helloworld;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

/**
 * Handler for requests to Lambda function.
 */
record Range(int min, int max) {

}

public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    static HashMap<String, Range> ranges = new HashMap<>() {
        {
            put("123", new Range(60, 150));
            put("130", new Range(70, 160));
            put("150", new Range(50, 250));

        }
    };

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> mapParameters = input.getQueryStringParameters();
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);

        try {
            if (!mapParameters.containsKey("patientId")) {
                throw new IllegalArgumentException("no patientId parameter");
            }
            String patientIdStr = mapParameters.get("patientId");
            Range range = ranges.get(patientIdStr);
            if (range == null) {
                throw new IllegalStateException(patientIdStr + " not found in ranges");
            }

            response
                    .withStatusCode(200)
                    .withBody(getRangeJSON(range));
        } catch (IllegalArgumentException e) {
            String errorJSON = getErrorJSON(e.getMessage());
            response
                    .withBody(errorJSON)
                    .withStatusCode(400);
        } catch (IllegalStateException e) {
            String errorJSON = getErrorJSON(e.getMessage());
            response
                    .withBody(errorJSON)
                    .withStatusCode(404);
        }
        return response;
    }

    private String getErrorJSON(String message) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("error", message);
        return jsonObj.toString();
    }

    private String getRangeJSON(Range range) {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("min", range.min());
        jsonObj.put("max", range.max());
        return jsonObj.toString();
    }

}
