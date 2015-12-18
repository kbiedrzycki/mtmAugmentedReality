package pg.eti.biedrzycki.findmyfriends.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;

import pg.eti.biedrzycki.findmyfriends.models.Param;

public class APIInterceptor {
    private String endpoint;
    private UriComponentsBuilder builder;
    private HttpHeaders headers = new HttpHeaders();
    private MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
    private HttpMethod httpMethod;

    private static final String API_URL = "http://findfriends.biedrzycki.info/api/v1/";

    // error messages
    public static final String NOT_AUTHENTICATED = "not_authenticated";
    public static final String WRONG_USER_PASSWORD = "wrong_user_or_password";
    public static final String USER_ALREDY_EXISTS = "user_already_exists";
    public static final String REQUIRED_FIELDS_EMPTY = "required_fields_empty";
    public static final String NO_AVATAR = "no_avatar";
    public static final String NOT_A_FRIENDS = "not_a_friends";
    public static final String NOT_INVITED = "not_invited";
    public static final String ALREADY_INVITED = "already_invited";
    public static final String WAITING = "check_waiting_invites";
    public static final String DOESNT_EXISTS = "user_doesnt_exists";
    public static final String CANNOT_INVITE_YOURSELF = "cannot_invite_yourself";

    public String error = null;

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint() {
        return this.endpoint;
    }

    public HttpHeaders getHeaders() {
        return this.headers;
    }

    public MultiValueMap<String, String> getBody() {
        return this.body;
    }

    public void addToBody(String key, String value) {
        this.body.add(key, value);
    }

    public void setHttpMethod(String method) {
        method = method.toLowerCase();

        switch (method) {
            case "post":
                this.httpMethod = HttpMethod.POST;
                break;
            case "put":
                this.httpMethod = HttpMethod.PUT;
                break;
            case "delete":
                this.httpMethod = HttpMethod.DELETE;
                break;
            case "get":
            default:
                this.httpMethod = HttpMethod.GET;
        }
    }

    public HttpEntity call() {
        return this.call(new ArrayList<Param>(), String.class);
    }

    public HttpEntity call(Class responseType) {
        return this.call(new ArrayList<Param>(), responseType);
    }

    public HttpEntity call(ArrayList<Param> params, Class responseType) {
        RestTemplate restTemplate = new RestTemplate();

        this.headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        String url = this.API_URL + this.getEndpoint();

        HttpEntity<?> entity;

        if (this.body.size() > 0) {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            entity = new HttpEntity<>(this.body, this.headers);
        } else {
            entity = new HttpEntity<>(this.headers);
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

        if (params.size() > 0) {
            for (Param param : params) {
                builder.queryParam(param.getKey(), param.getValue());
            }
        }

        try {
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            HttpEntity<?> result = restTemplate.exchange(builder.build().encode().toUri(), this.httpMethod, entity, responseType);

            return result;
        } catch(HttpStatusCodeException e){
            String errorPayload = e.getResponseBodyAsString();

            try {
                JSONObject errorObject = new JSONObject(errorPayload);
                this.error = errorObject.optString("error");
            } catch(JSONException jsonException) {
                return null;
            }
        }

        return null;
    }
}
