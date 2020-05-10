package com.zxh.butterknife;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zxh.annotation.BindView;
import com.zxh.annotation.ClickView;
import com.zxh.library.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.btn_qq)
    Button btn;
    @BindView(R.id.tv_wechat)
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @ClickView({R.id.tv_wechat,R.id.btn_qq})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_qq:
                Toast.makeText(this, "btn_qq", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tv_wechat:
                startActivity(new Intent(MainActivity.this,TwoActivity.class));
                break;
        }

    }
}
