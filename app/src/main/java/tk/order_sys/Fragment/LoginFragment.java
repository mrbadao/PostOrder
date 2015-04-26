package tk.order_sys.Fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import tk.order_sys.postorder.MainActivity;
import tk.order_sys.postorder.R;

/**
 * Created by HieuNguyen on 4/22/2015.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {
    LoginInterface delegate;
    View rootView;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).onShowHideActionBar(false);
        delegate = (LoginInterface) getActivity();

        rootView = inflater.inflate(R.layout.fragment_login, container, false);
        Button btnLogin = (Button) rootView.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
//        Check login here
        delegate.onLoginSuccess();
    }

    public static interface LoginInterface{
        public void onLoginSuccess();
    }
}