package com.zxh.butterknife;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zxh.annotation.BindView;
import com.zxh.annotation.ClickView;
import com.zxh.library.ButterKnife;

import androidx.appcompat.app.AppCompatActivity;

public class TwoActivity extends AppCompatActivity {
    @BindView(R.id.btn_sina)
    Button btn_sina;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);
        ButterKnife.bind(this);
        btn_sina.setText("微博");
    }

    @ClickView({R.id.btn_sina})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_sina:
                Toast.makeText(this, "微博", Toast.LENGTH_SHORT).show();
                break;
        }

    }
}
