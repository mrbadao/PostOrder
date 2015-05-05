package tk.order_sys.Fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import tk.order_sys.HttpRequest.DeliveryLoginHttpRequest;
import tk.order_sys.HttpRequestInterface.DeliveryInterface;
import tk.order_sys.Postorder.MainActivity;
import tk.order_sys.Postorder.R;

/**
 * Created by HieuNguyen on 4/22/2015.
 */
public class LoginFragment extends Fragment implements View.OnClickListener, DeliveryInterface {
    public static String PREF_STAFF_TOKEN_TAG = "tk.order_sys.postorder.staff.token";
    public static String PREF_STAFF_ID_TAG = "tk.order_sys.postorder.staff.id";

    LoginInterface delegate;
    View rootView;
    EditText mUsername, mPassword;
    ProgressBar mProgressBar;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).onShowHideActionBar(false);
        delegate = (LoginInterface) getActivity();

        rootView = inflater.inflate(R.layout.fragment_login, container, false);
        mUsername = (EditText) rootView.findViewById(R.id.txtLoginID);
        mPassword = (EditText) rootView.findViewById(R.id.txtLoginPassword);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressbarLogin);
        Button btnLogin = (Button) rootView.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        int itemId = v.getId();
        switch (itemId) {
            case R.id.btnLogin:
                String username = String.valueOf(mUsername.getText());
                String password = String.valueOf(mPassword.getText());
                if (username.isEmpty() && password.isEmpty()) {
                    Toast.makeText(getActivity().getApplicationContext(), "Tài khoản và mật khẩu không được rỗng", Toast.LENGTH_SHORT).show();
                } else {
                    JSONObject jsonParams = new JSONObject();
                    try {
                        jsonParams.put("login_id", username);
                        jsonParams.put("password", password);
                        new DeliveryLoginHttpRequest(getActivity(), null, this).execute(jsonParams);
                        mProgressBar.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    @Override
    public void onUserLogin(JSONObject jsonObject) {
        if (jsonObject != null) {
            mProgressBar.setVisibility(View.INVISIBLE);
            try {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sharedPreferences.edit();

                if (!jsonObject.isNull("error")) {
                    Toast.makeText(getActivity().getApplicationContext(), "Tài khoản hoặc mật khẩu không đúng.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!jsonObject.isNull("token")) {
                    editor.putString(PREF_STAFF_TOKEN_TAG, jsonObject.getString("token"));
                } else return;

                if (!jsonObject.isNull("staff")) {
                    editor.putString(PREF_STAFF_ID_TAG, jsonObject.getJSONObject("staff").getString("id"));
                } else return;

                editor.commit();

                delegate.onLoginSuccess();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCheckToken(JSONObject jsonObject) {
        return;
    }

    public interface LoginInterface {
        void onLoginSuccess();
    }
}