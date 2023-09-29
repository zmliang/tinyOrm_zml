package com.zml.skin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zml.skin.orm.ITestTableEntityDao;
import com.zml.skin.orm.TestTableEntity;
import com.zml.skin.orm.ZMLOrm;

import java.util.List;


public class TestActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);
        ITestTableEntityDao tableEntityDao = ZMLOrm.getInstance().getTestTableEntityDao();

        findViewById(R.id.insert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestTableEntity entity = new TestTableEntity();
                entity.age = 10;
                entity.avatar = "头像";
                entity.classRoom = "幼儿园-小班";
                entity.name = "张雨薇";
                tableEntityDao.insert(entity);
            }
        });

        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        findViewById(R.id.query).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<TestTableEntity> result = tableEntityDao.queryAll();
                for (TestTableEntity item:result){
                    Log.e("zml","查到的结果="+item);
                }
            }
        });


    }
}
