package com.boredream.designrescollection.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.boredream.bdcodehelper.net.ObservableDecorator;
import com.boredream.bdcodehelper.utils.DateUtils;
import com.boredream.designrescollection.R;
import com.boredream.designrescollection.base.BaseActivity;
import com.boredream.designrescollection.entity.User;
import com.boredream.designrescollection.net.HttpRequest;
import com.boredream.designrescollection.net.SimpleSubscriber;
import com.boredream.designrescollection.utils.UserInfoKeeper;

import rx.Observable;

/**
 * 注册页面步骤二
 */
public class RegisterStep2Activity extends BaseActivity implements View.OnClickListener {

    // 总倒计时60秒
    private static final long TOTCAL_TIME = 60 * DateUtils.ONE_SECOND_MILLIONS;
    // 每次减少1秒
    private static final long COUNT_DOWN_INTERVAL = DateUtils.ONE_SECOND_MILLIONS;

    private EditText et_verification_code;
    private Button btn_code_info;
    private Button btn_next;

    private String phone;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_validate_step2);

        initExtras();
        initView();
        initData();
    }

    private void initExtras() {
        Intent intent = getIntent();
        phone = intent.getStringExtra("phone");
        password = intent.getStringExtra("password");
    }

    private void initView() {
        initBackTitle("手机号验证");

        et_verification_code = (EditText) findViewById(R.id.et_verification_code);
        btn_code_info = (Button) findViewById(R.id.btn_code_info);
        btn_next = (Button) findViewById(R.id.btn_next);

        btn_code_info.setOnClickListener(this);
        btn_next.setOnClickListener(this);
    }

    private void initData() {
        startCountDown();
    }

    /**
     * 开始倒计时
     */
    private void startCountDown() {
        showToast("短信验证码发送成功");

        btn_code_info.setText("60秒");
        btn_code_info.setEnabled(false);

        // 倒计时开始,共60秒,每次减少1秒
        CountDownTimer timer = new CountDownTimer(TOTCAL_TIME, COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long l) {
                // 重新获取(60)
                String restTime = (int) (l / COUNT_DOWN_INTERVAL) + "秒";
                btn_code_info.setText(restTime);
            }

            @Override
            public void onFinish() {
                // 倒计时结束,重置按钮
                btn_code_info.setText("重新获取");
                btn_code_info.setEnabled(true);
            }
        };
        timer.start();
    }

    /**
     * 根据类型提交注册或重置密码接口
     */
    private void submit() {
        // validate
        String code = et_verification_code.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            return;
        }

        register(code);
    }

    /**
     * 注册
     */
    private void register(String code) {
        User user = new User();
        user.setMobilePhoneNumber(phone);
        user.setUsername(phone);
        user.setPassword(password);
        user.setSmsCode(code);
        Observable<User> observable = HttpRequest.getApiService().userRegist(user);
        showProgressDialog();
        ObservableDecorator.decorate(observable)
                .subscribe(new SimpleSubscriber<User>(this) {
                    @Override
                    public void onNext(User user) {
                        // include token
                        dismissProgressDialog();
                        UserInfoKeeper.setCurrentUser(user);
                        // 注册成功,显示成功提示框
                        showRegistSuccessDialog();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        dismissProgressDialog();
                    }
                });

    }

    /**
     * 注册成功提示框,提醒用户完善资料
     */
    private void showRegistSuccessDialog() {
        new AlertDialog.Builder(this)
                .setMessage("注册成功，你可以在个人详情中修改或完善用户信息。")
                .setPositiveButton("确定", null)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // 确定或者返回键关闭对话框都走此
                        intent2Activity(MainActivity.class);
                    }
                })
                .setCancelable(false) // cancelable设为false,防止用户点击返回键关闭对话框停留在当前页面
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_code_info:
                // FIXME: 短信有限，所以直接模拟验证码发送成功，使用不验证的注册
                // resendSmsCode();
                startCountDown();
                break;
            case R.id.btn_next:
                submit();
                break;
        }
    }

}
