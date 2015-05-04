package tk.order_sys.API;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import tk.order_sys.config.appConfig;

/**
 * Created by HieuNguyen on 4/6/2015.
 */
public class API {
    private static InputStream is = null;
    private static JSONObject jObj = null;
    private static String json = "";

    static final String SECRET_KEY = "6dn9T3t2760yypWAhdhURmz7oZQrhdXjqRoTorybjWU=";

    static final String API_DELIVERY_LOGIN = "delivery/login";
    static final String API_DELIVERY_GET_ORDERS = "delivery/GetDeliveryOrders";
    static final String API_DELIVERY_GET_ORDER_DETAIL = "delivery/GetDeliveryOrderDetail";
    static final String API_DELIVERY_COMPLETE_ORDER = "delivery/CompleteDeliveryOrder";
    static final String API_DELIVERY_CHECK_TOKEN = "delivery/checktoken";
    // constructor

    public API() {
    }

    private static JSONObject getJSON(String address, JSONObject post_data, final JSONArray arrCookies) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(address);
        JSONArray jsonCookies = null;
        CookieStore cookieStore = null;

        try {
            cookieStore = new BasicCookieStore();
            if (arrCookies != null) {
                for (int i = 0; i < arrCookies.length(); i++) {
                    final JSONObject saveCookie = new JSONObject(arrCookies.get(i).toString());
                    cookieStore.addCookie(new Cookie() {
                        @Override
                        public String getName() {
                            try {
                                return saveCookie.get("name").toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        public String getValue() {
                            try {
                                return saveCookie.get("value").toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        public String getComment() {
                            return null;
                        }

                        @Override
                        public String getCommentURL() {
                            return null;
                        }

                        @Override
                        public Date getExpiryDate() {
                            return null;
                        }

                        @Override
                        public boolean isPersistent() {
                            return false;
                        }

                        @Override
                        public String getDomain() {
                            try {
                                return saveCookie.get("domain").toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        public String getPath() {
                            try {
                                return saveCookie.get("path").toString();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        public int[] getPorts() {
                            return new int[0];
                        }

                        @Override
                        public boolean isSecure() {
                            return false;
                        }

                        @Override
                        public int getVersion() {
                            try {
                                return Integer.valueOf(saveCookie.get("version").toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        }

                        @Override
                        public boolean isExpired(Date date) {
                            return false;
                        }
                    });
                }
            }

            HttpContext localContext = new BasicHttpContext();

            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);


            if (post_data == null) {
                post_data = new JSONObject();
            }

            post_data.put("secret_key", SECRET_KEY);

            StringEntity se = new StringEntity(post_data.toString());
            httpPost.setEntity(se);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            HttpResponse response = client.execute(httpPost, localContext);

            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == 200) {

                List<Cookie> cookies = cookieStore.getCookies();

                if (cookies.size() > 0) {
                    jsonCookies = new JSONArray();
                    for (int i = 0; i < cookies.size(); i++) {
                        JSONObject jsonCookie = new JSONObject();
                        jsonCookie.put("version", cookies.get(i).getVersion());
                        jsonCookie.put("name", cookies.get(i).getName());
                        jsonCookie.put("value", cookies.get(i).getValue());
                        jsonCookie.put("domain", cookies.get(i).getDomain());
                        jsonCookie.put("path", cookies.get(i).getPath());
                        jsonCookie.put("expiry", cookies.get(i).getExpiryDate());
                        jsonCookies.put(jsonCookie.toString());
                    }

                }

                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();

                BufferedReader reader = new BufferedReader(new InputStreamReader(content));

                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }

            } else {
                return null;
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(builder.toString());
            if (jsonCookies != null) {
                jsonObject.put("Cookies", jsonCookies.toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static JSONObject DeliveryLogin(JSONObject params, JSONArray jsonCookieStore) {
        return getJSON(appConfig.getApiUrl(true) + API_DELIVERY_LOGIN, params, jsonCookieStore);
    }

    public static JSONObject getDeliveryOrders(JSONObject params, JSONArray jsonCookieStore) {
        return getJSON(appConfig.getApiUrl(true) + API_DELIVERY_GET_ORDERS, params, jsonCookieStore);
    }

    public static JSONObject getDeliveryOrderDetail(JSONObject params, JSONArray jsonCookieStore) {
        return getJSON(appConfig.getApiUrl(true) + API_DELIVERY_GET_ORDER_DETAIL, params, jsonCookieStore);
    }

    public static JSONObject completeDeliveryOrder(JSONObject params, JSONArray jsonCookieStore) {
        return getJSON(appConfig.getApiUrl(true) + API_DELIVERY_COMPLETE_ORDER, params, jsonCookieStore);
    }

    public static JSONObject checkToken(JSONObject params, JSONArray jsonCookieStore) {
        return getJSON(appConfig.getApiUrl(true) + API_DELIVERY_CHECK_TOKEN, params, jsonCookieStore);
    }

}


