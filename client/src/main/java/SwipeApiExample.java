import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.SwipeApi;

import java.io.File;
import java.util.*;

public class SwipeApiExample {

    private static final String LOCAL_URL = "http://localhost:8080/server_war";
    private static final String REMOTE_URL = "http://34.215.133.122:8080/server_war";
    public static void main(String[] args) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(REMOTE_URL);
        SwipeApi apiInstance = new SwipeApi(apiClient);
        SwipeDetails body = new SwipeDetails().swipee("1").swiper("11").comment("no");
        String leftorright = "left";
        try {
            //apiInstance.swipe(body, leftorright);
            ApiResponse apiResponse = apiInstance.swipeWithHttpInfo(body, leftorright);
            System.out.println(apiResponse.getStatusCode());
        } catch (ApiException e) {
            System.err.println("Exception when calling SwipeApi#swipe");
            System.err.println(e.getCode() + ": " + e.getResponseBody());
            e.printStackTrace();
        }
    }
}