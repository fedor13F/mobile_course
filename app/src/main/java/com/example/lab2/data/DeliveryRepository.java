package com.example.lab2.data;

import android.os.Handler;
import android.os.Looper;

import com.example.lab2.BuildConfig;
import com.example.lab2.R;
import com.example.lab2.model.CartLine;
import com.example.lab2.model.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Клиент к FastAPI backend: товары, корзина в БД, оформление заказа.
 */
public final class DeliveryRepository {

    public static final class AddressInfo {
        public final String label;
        public final double latitude;
        public final double longitude;

        public AddressInfo(String label, double latitude, double longitude) {
            this.label = label;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public interface ResultCallback<T> {
        void onSuccess(T value);

        void onError(String message);
    }

    public interface VoidCallback {
        void onSuccess();

        void onError(String message);
    }

    private static final DeliveryRepository INSTANCE = new DeliveryRepository();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Handler main;
    private final ExecutorService io;
    private final Set<String> cartProductIds;

    private DeliveryRepository() {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        main = new Handler(Looper.getMainLooper());
        io = Executors.newFixedThreadPool(4);
        cartProductIds = Collections.synchronizedSet(new HashSet<>());
    }

    public static DeliveryRepository get() {
        return INSTANCE;
    }

    public boolean isInCart(String productId) {
        return cartProductIds.contains(productId);
    }

    public void loadProducts(ResultCallback<List<Product>> callback) {
        io.execute(() -> {
            try {
                HttpUrl url = apiRoot().newBuilder()
                        .addPathSegment("api")
                        .addPathSegment("products")
                        .build();
                String body = getBody(new Request.Builder().url(url).get().build());
                List<Product> list = parseProducts(body);
                main.post(() -> callback.onSuccess(list));
            } catch (Exception e) {
                main.post(() -> callback.onError(e.getMessage() != null ? e.getMessage() : "error"));
            }
        });
    }

    public void loadProduct(String id, ResultCallback<Product> callback) {
        io.execute(() -> {
            try {
                HttpUrl url = apiRoot().newBuilder()
                        .addPathSegment("api")
                        .addPathSegment("products")
                        .addPathSegment(id)
                        .build();
                String body = getBody(new Request.Builder().url(url).get().build());
                Product p = Product.fromJson(new JSONObject(body));
                main.post(() -> callback.onSuccess(p));
            } catch (Exception e) {
                main.post(() -> callback.onError(e.getMessage() != null ? e.getMessage() : "error"));
            }
        });
    }

    /**
     * Обновить кэш наличия товаров в корзине с сервера.
     */
    public void refreshCart(Runnable onDone, Runnable onError) {
        io.execute(() -> {
            try {
                HttpUrl url = apiRoot().newBuilder()
                        .addPathSegment("api")
                        .addPathSegment("cart")
                        .build();
                String body = getBody(new Request.Builder().url(url).get().build());
                syncCartCacheFromCartJson(body);
                main.post(() -> {
                    if (onDone != null) onDone.run();
                });
            } catch (Exception e) {
                main.post(() -> {
                    if (onError != null) onError.run();
                });
            }
        });
    }

    public void addToCart(String productId, VoidCallback callback) {
        io.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("product_id", productId);
                json.put("quantity", 1);
                HttpUrl url = apiRoot().newBuilder()
                        .addPathSegment("api")
                        .addPathSegment("cart")
                        .addPathSegment("items")
                        .build();
                RequestBody rb = RequestBody.create(json.toString(), JSON);
                String body = getBody(new Request.Builder().url(url).post(rb).build());
                syncCartCacheFromCartJson(body);
                main.post(callback::onSuccess);
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "error";
                main.post(() -> callback.onError(msg));
            }
        });
    }

    public void loadCartLines(ResultCallback<List<CartLine>> callback) {
        io.execute(() -> {
            try {
                HttpUrl url = apiRoot().newBuilder()
                        .addPathSegment("api")
                        .addPathSegment("cart")
                        .build();
                String body = getBody(new Request.Builder().url(url).get().build());
                List<CartLine> lines = parseCartLines(body);
                main.post(() -> callback.onSuccess(lines));
            } catch (Exception e) {
                main.post(() -> callback.onError(e.getMessage() != null ? e.getMessage() : "error"));
            }
        });
    }

    public void checkout(String customerName, String address, ResultCallback<String> callback) {
        io.execute(() -> {
            try {
                HttpUrl cartUrl = apiRoot().newBuilder()
                        .addPathSegment("api")
                        .addPathSegment("cart")
                        .build();
                String cartBody = getBody(new Request.Builder().url(cartUrl).get().build());
                JSONArray cartArr = new JSONArray(cartBody);
                if (cartArr.length() == 0) {
                    throw new IOException("Корзина пуста");
                }
                JSONArray items = new JSONArray();
                for (int i = 0; i < cartArr.length(); i++) {
                    JSONObject line = cartArr.getJSONObject(i);
                    JSONObject it = new JSONObject();
                    it.put("product_id", line.getString("product_id"));
                    it.put("quantity", line.getInt("quantity"));
                    items.put(it);
                }
                JSONObject order = new JSONObject();
                order.put("customer_name", customerName);
                order.put("address", address);
                order.put("items", items);

                HttpUrl ordersUrl = apiRoot().newBuilder()
                        .addPathSegment("api")
                        .addPathSegment("orders")
                        .build();
                RequestBody rb = RequestBody.create(order.toString(), JSON);
                String orderResp = getBody(new Request.Builder().url(ordersUrl).post(rb).build());
                JSONObject created = new JSONObject(orderResp);
                String orderId = created.getString("id");

                Request deleteCart = new Request.Builder().url(cartUrl).delete().build();
                executeWithoutBody(deleteCart);

                syncCartCacheFromCartJson("[]");
                main.post(() -> callback.onSuccess(orderId));
            } catch (Exception e) {
                main.post(() -> callback.onError(e.getMessage() != null ? e.getMessage() : "error"));
            }
        });
    }

    public void loadSelectedAddress(ResultCallback<AddressInfo> callback) {
        io.execute(() -> {
            try {
                HttpUrl url = apiRoot().newBuilder()
                        .addPathSegment("api")
                        .addPathSegment("address")
                        .build();
                String body = getBody(new Request.Builder().url(url).get().build());
                if (body == null || body.isBlank() || "null".equals(body.trim())) {
                    main.post(() -> callback.onSuccess(null));
                    return;
                }
                JSONObject o = new JSONObject(body);
                AddressInfo info = new AddressInfo(
                        o.getString("label"),
                        o.getDouble("latitude"),
                        o.getDouble("longitude")
                );
                main.post(() -> callback.onSuccess(info));
            } catch (Exception e) {
                main.post(() -> callback.onError(e.getMessage() != null ? e.getMessage() : "error"));
            }
        });
    }

    public void saveSelectedAddress(AddressInfo info, VoidCallback callback) {
        io.execute(() -> {
            try {
                JSONObject json = new JSONObject();
                json.put("label", info.label);
                json.put("latitude", info.latitude);
                json.put("longitude", info.longitude);
                HttpUrl url = apiRoot().newBuilder()
                        .addPathSegment("api")
                        .addPathSegment("address")
                        .build();
                RequestBody rb = RequestBody.create(json.toString(), JSON);
                executeWithoutBody(new Request.Builder().url(url).put(rb).build());
                main.post(callback::onSuccess);
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : "error";
                main.post(() -> callback.onError(msg));
            }
        });
    }

    private HttpUrl apiRoot() {
        return Objects.requireNonNull(HttpUrl.parse(BuildConfig.API_BASE_URL), "Bad API_BASE_URL");
    }

    private String getBody(Request request) throws IOException {
        try (Response resp = client.newCall(request).execute()) {
            String b = resp.body() != null ? resp.body().string() : "";
            if (!resp.isSuccessful()) {
                throw new IOException("HTTP " + resp.code() + ": " + b);
            }
            return b;
        }
    }

    private void executeWithoutBody(Request request) throws IOException {
        try (Response resp = client.newCall(request).execute()) {
            if (!resp.isSuccessful()) {
                String b = resp.body() != null ? resp.body().string() : "";
                throw new IOException("HTTP " + resp.code() + ": " + b);
            }
        }
    }

    private List<Product> parseProducts(String json) throws JSONException {
        JSONArray arr = new JSONArray(json);
        List<Product> out = new ArrayList<>(arr.length());
        for (int i = 0; i < arr.length(); i++) {
            out.add(Product.fromJson(arr.getJSONObject(i)));
        }
        return out;
    }

    private List<CartLine> parseCartLines(String json) throws JSONException {
        JSONArray arr = new JSONArray(json);
        List<CartLine> out = new ArrayList<>(arr.length());
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            String pid = o.getString("product_id");
            String name = o.getString("name");
            int unitPrice = o.getInt("unit_price_rub");
            int qty = o.getInt("quantity");
            Product p = new Product(pid, name, unitPrice, "", R.drawable.ic_product);
            out.add(new CartLine(p, qty));
        }
        return out;
    }

    private void syncCartCacheFromCartJson(String json) throws JSONException {
        JSONArray arr = new JSONArray(json);
        Set<String> next = new HashSet<>();
        for (int i = 0; i < arr.length(); i++) {
            next.add(arr.getJSONObject(i).getString("product_id"));
        }
        synchronized (cartProductIds) {
            cartProductIds.clear();
            cartProductIds.addAll(next);
        }
    }
}
