package com.apps.fatima.sealocation.fragment;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.apps.fatima.sealocation.R;
import com.apps.fatima.sealocation.adapter.MyOrdersAdapter;
import com.apps.fatima.sealocation.manager.AppErrorsManager;
import com.apps.fatima.sealocation.manager.AppPreferences;
import com.apps.fatima.sealocation.manager.FontManager;
import com.apps.fatima.sealocation.manager.InternetAvailableCallback;
import com.apps.fatima.sealocation.manager.InternetConnectionUtils;
import com.apps.fatima.sealocation.model.Orders;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class MyOrdersFragment extends Fragment {

    private String token;
    private Handler handler;
    private List<Orders> ordersList = new ArrayList<>();
    private MyOrdersAdapter MyOrdersAdapter;
    private Orders order;
    private TextView noContentOrder;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_my_orders, container, false);
        token = AppPreferences.getString(getActivity(), "token");
        handler = new Handler(Looper.getMainLooper());
        init(view);
        getMyOrders();
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void init(View view) {
        RelativeLayout layout = view.findViewById(R.id.layout);
        Toolbar toolBar = view.findViewById(R.id.toolBar);
        toolBar.setVisibility(View.GONE);
        FontManager.applyFont(Objects.requireNonNull(getActivity()), layout);
        noContentOrder = view.findViewById(R.id.noContentOrder);
        RecyclerView recycleViewTrip = view.findViewById(R.id.recycleView);
        MyOrdersAdapter = new MyOrdersAdapter(getActivity(), ordersList);
        RecyclerView.LayoutManager mLayoutManager_ = new LinearLayoutManager(getActivity());
        recycleViewTrip.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        recycleViewTrip.setLayoutManager(mLayoutManager_);
        recycleViewTrip.setItemAnimator(new DefaultItemAnimator());
        recycleViewTrip.setNestedScrollingEnabled(false);

        recycleViewTrip.setAdapter(MyOrdersAdapter);
    }


    public void getMyOrders() {
        ordersList.clear();
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();

        InternetConnectionUtils.isInternetAvailable(getActivity(), new InternetAvailableCallback() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onInternetAvailable(boolean isAvailable) {
                if (isAvailable) {
                    OkHttpClient client;

                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                    builder.connectTimeout(5, TimeUnit.MINUTES)
                            .writeTimeout(5, TimeUnit.MINUTES)
                            .readTimeout(5, TimeUnit.MINUTES);
                    client = builder.build();

                    okhttp3.Request request = new okhttp3.Request.Builder().url(FontManager.URL
                            + "my_orders").get().header("Authorization", "Bearer " + token).build();
                    final okhttp3.Response response;
                    try {
                        response = client.newCall(request).execute();
                        assert response.body() != null;
                        String response_data = Objects.requireNonNull(response.body()).string();
                        Log.e("my_orders", response_data);
                        try {
                            JSONObject jsonObject = new JSONObject(response_data);
                            String success = jsonObject.getString("success");
                            JSONObject successObject = new JSONObject(success);
                            String orders = successObject.getString("orders");
                            String course = successObject.getString("course");
                            String diving_trip = successObject.getString("diving_trip");
                            String jetski = successObject.getString("jetski");
                            if (orders.equals("[]") && course.equals("[]")
                                    && diving_trip.equals("[]") && jetski.equals("[]")) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressDialog.hide();
                                        noContentOrder.setVisibility(View.VISIBLE);
                                    }
                                });
                            } else {
                                JSONArray ordersArray = new JSONArray(orders);
                                for (int i = 0; i < ordersArray.length(); i++) {
                                    JSONObject ordersObject = ordersArray.getJSONObject(i);
                                    String id = ordersObject.getString("id");
                                    String boat_id = ordersObject.getString("boat_id");
                                    String user_id = ordersObject.getString("user_id");
                                    String trip = ordersObject.getString("trip");
                                    String guid = ordersObject.getString("guid");
                                    String mobile = ordersObject.getString("mobile");
                                    String partner_id = ordersObject.getString("partner_id");
                                    String diver_id = ordersObject.optString("diver_id");
                                    String tank_id = ordersObject.optString("tank_id");
                                    String approved = ordersObject.optString("approved");

                                    order = new Orders(id, boat_id, user_id, trip, guid, mobile,
                                            partner_id, diver_id, tank_id, approved);
                                    ordersList.add(order);
                                }
                                JSONArray courseArray = new JSONArray(course);
                                for (int i = 0; i < courseArray.length(); i++) {
                                    JSONObject courseObject = courseArray.getJSONObject(i);
                                    String id = courseObject.getString("id");
                                    String boat_id = courseObject.optString("boat_id");
                                    String user_id = courseObject.getString("user_id");
//                                    String trip = courseObject.optString("trip");
                                    String guid = courseObject.getString("guid");
                                    String mobile = courseObject.getString("mobile");
                                    String partner_id = courseObject.getString("partner_id");
                                    String diver_id = courseObject.getString("diver_id");
                                    String tank_id = courseObject.optString("tank_id");
                                    String approved = courseObject.optString("approved");


                                    order = new Orders(id, boat_id, user_id,
                                            getString(R.string.course_driving), guid, mobile,
                                            partner_id, diver_id, tank_id, approved);
                                    ordersList.add(order);
                                }
                                JSONArray diving_tripArray = new JSONArray(diving_trip);
                                for (int i = 0; i < diving_tripArray.length(); i++) {
                                    JSONObject diving_tripObject = diving_tripArray.getJSONObject(i);
                                    String id = diving_tripObject.getString("id");
                                    String boat_id = diving_tripObject.optString("boat_id");
                                    String user_id = diving_tripObject.getString("user_id");
//                                    String trip = diving_tripObject.optString("trip");
                                    String guid = diving_tripObject.getString("guid");
                                    String mobile = diving_tripObject.getString("mobile");
                                    String partner_id = diving_tripObject.getString("partner_id");
                                    String diver_id = diving_tripObject.getString("diver_id");
                                    String tank_id = diving_tripObject.optString("tank_id");
                                    String approved = diving_tripObject.optString("approved");
                                    order = new Orders(id, boat_id, user_id,
                                            getString(R.string.trip_diving), guid, mobile,
                                            partner_id, diver_id, tank_id, approved);
                                    ordersList.add(order);
                                }
                                JSONArray jetskiArray = new JSONArray(jetski);
                                for (int i = 0; i < jetskiArray.length(); i++) {
                                    JSONObject jetskiObject = jetskiArray.getJSONObject(i);
                                    String id = jetskiObject.getString("id");
                                    String boat_id = jetskiObject.optString("boat_id");
                                    String user_id = jetskiObject.getString("user_id");
//                                    String trip = jetskiObject.optString("trip");
                                    String guid = jetskiObject.getString("guid");
                                    String mobile = jetskiObject.getString("mobile");
                                    String partner_id = jetskiObject.getString("partner_id");
                                    String diver_id = jetskiObject.optString("diver_id");
                                    String tank_id = jetskiObject.getString("tank_id");
                                    String approved = jetskiObject.getString("approved");
                                    order = new Orders(id, boat_id, user_id,
                                            getString(R.string.tank), guid, mobile, partner_id,
                                            diver_id, tank_id, approved);
                                    ordersList.add(order);
                                }


                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        noContentOrder.setVisibility(View.GONE);
                                        progressDialog.hide();
                                        MyOrdersAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        } catch (final JSONException e) {
                            e.printStackTrace();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.hide();
                                    AppErrorsManager.showErrorDialog(getActivity(), e.getMessage());
                                }
                            });
                        }
                        if (!response.isSuccessful()) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.hide();
                                    AppErrorsManager.showErrorDialog(getActivity(), response + "");
                                }
                            });
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.hide();
                                AppErrorsManager.showErrorDialog(getActivity(), e.getMessage());
                            }
                        });
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.hide();
                            AppErrorsManager.showErrorDialog(getActivity(), getString(R.string.error_network));
                        }
                    });
                }
            }
        });
    }
}
