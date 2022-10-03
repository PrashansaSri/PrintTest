package in.acural.printtest.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import in.acural.printtest.MainActivity;
import in.acural.printtest.R;


public class SettingActivity extends AppCompatActivity {
    private TextView shuaxin,gengxin;
    private Switch lixian;
    private Toolbar settingbar;

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        init();
    }

    /**
     * init
     */
    private void init(){
        shuaxin = (TextView) findViewById(R.id.shuaxin);
        shuaxin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingActivity.this,
                        "Refresh to be developed",Toast.LENGTH_SHORT).show();
            }
        });
        gengxin = (TextView) findViewById(R.id.gengxin);
        gengxin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SettingActivity.this,"Update pending development",Toast.LENGTH_SHORT).show();
            }
        });
        lixian = (Switch) findViewById(R.id.lixian);
        lixian.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            }
        });
        settingbar = (Toolbar) findViewById(R.id.settingbar);
        settingbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this,MainActivity.class);
                startActivityForResult(intent,10010);
                SettingActivity.this.finish();
            }
        });
    }
}
