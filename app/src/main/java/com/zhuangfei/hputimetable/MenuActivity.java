package com.zhuangfei.hputimetable;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.aigestudio.wheelpicker.widgets.WheelDatePicker;
import com.tencent.bugly.beta.Beta;
import com.zhuangfei.classbox.activity.AuthActivity;
import com.zhuangfei.classbox.model.SuperLesson;
import com.zhuangfei.classbox.model.SuperResult;
import com.zhuangfei.classbox.utils.SuperUtils;
import com.zhuangfei.hputimetable.api.model.ScheduleName;
import com.zhuangfei.hputimetable.api.model.TimetableModel;
import com.zhuangfei.hputimetable.constants.ConfigConstants;
import com.zhuangfei.hputimetable.constants.ShareConstants;
import com.zhuangfei.hputimetable.fragment.ScheduleFragment;
import com.zhuangfei.hputimetable.model.ScheduleDao;
import com.zhuangfei.hputimetable.specialarea.SpecialAreaActivity;
import com.zhuangfei.hputimetable.tools.BroadcastUtils;
import com.zhuangfei.hputimetable.tools.TimetableTools;
import com.zhuangfei.hputimetable.tools.UpdateTools;
import com.zhuangfei.hputimetable.tools.WidgetConfig;
import com.zhuangfei.toolkit.model.BundleModel;
import com.zhuangfei.toolkit.tools.ActivityTools;
import com.zhuangfei.toolkit.tools.ShareTools;

import org.litepal.crud.DataSupport;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

public class MenuActivity extends AppCompatActivity {

    private static final String TAG = "MenuActivity";
    Activity context;

    LinearLayout backLayout;

    public static final int REQUEST_IMPORT = 1;

    @BindView(R.id.id_switch_hidenotcur)
    SwitchCompat hideNotCurSwitch;

    @BindView(R.id.id_switch_hideweekends)
    SwitchCompat hideWeekendsSwitch;

    @BindView(R.id.id_checkauto)
    SwitchCompat checkedAutoSwitch;

    @BindView(R.id.id_widget_max15)
    SwitchCompat max15Switch;

    @BindView(R.id.id_widget_hideweeks)
    SwitchCompat hideWeeksSwitch;

    @BindView(R.id.id_widget_hidedate)
    SwitchCompat hideDateSwitch;

    @BindView(R.id.id_switch_alone)
    SwitchCompat aloneSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
        inits();
    }

    private void inits() {
        context = this;
        backLayout = findViewById(R.id.id_back);
        backLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        int hide = ShareTools.getInt(this, "hidenotcur", 0);
        if (hide == 0) {
            hideNotCurSwitch.setChecked(false);
        } else {
            hideNotCurSwitch.setChecked(true);
        }

        int alpha = ShareTools.getInt(this, "hideweekends", 0);
        if (alpha == 0) {
            hideWeekendsSwitch.setChecked(false);
        } else {
            hideWeekendsSwitch.setChecked(true);
        }

        int isIgnoreUpdate = ShareTools.getInt(this, "isIgnoreUpdate", 0);
        if (isIgnoreUpdate == 0) {
            checkedAutoSwitch.setChecked(true);
        } else {
            checkedAutoSwitch.setChecked(false);
        }

        int isAlone = ShareTools.getInt(this, "isAlone", 0);
        if (isAlone == 0) {
            aloneSwitch.setChecked(false);
        } else {
            aloneSwitch.setChecked(true);
        }

        boolean maxItem= WidgetConfig.get(this,WidgetConfig.CONFIG_MAX_ITEM);
        max15Switch.setChecked(maxItem);

        boolean hideWeeks= WidgetConfig.get(this,WidgetConfig.CONFIG_HIDE_WEEKS);
        hideWeeksSwitch.setChecked(hideWeeks);

        boolean hideDate= WidgetConfig.get(this,WidgetConfig.CONFIG_HIDE_DATE);
        hideDateSwitch.setChecked(hideDate);
    }

    public Activity getContext() {
        return context;
    }

    public void clearData() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("清空数据")
                .setMessage("确认后将删除本地保存的所有课程数据且无法恢复！请谨慎操作")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ShareTools.clear(getContext());
                        DataSupport.deleteAll(TimetableModel.class);
                        Intent intent = new Intent(getContext(), ImportMajorActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getContext().startActivity(intent);
                        getContext().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);//动画
                        getContext().finish();
                    }
                })
                .setNegativeButton("取消", null);
        builder.create().show();
    }

    @OnClick(R.id.id_menu_about)
    public void about() {
        ActivityTools.toActivity(MenuActivity.this, AboutActivity.class);
        finish();
    }

    @OnClick(R.id.id_menu_update2)
    public void issues() {
        try {
            UpdateTools.checkUpdate(this,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void goBack() {
        ActivityTools.toBackActivityAnim(getContext(), MainActivity.class);
    }

    @Override
    public void onBackPressed() {
        goBack();
    }

    @OnClick(R.id.id_menu_update)
    public void onUpdateLayoutClick() {
        Beta.checkUpgrade();
    }

    @OnClick(R.id.id_menu_share)
    public void onShareLayoutClick() {
        String content = "怪兽课表是一款免费、开源的通用型课表软件，可以无缝从超级课程表的账户以及课程码中导入数据，并且有桌面小部件和学校专区，快来体验吧，感觉好用就分享给你的朋友吧~\n下载地址：https://www.coolapk.com/apk/com.zhuangfei.hputimetable";
        Intent share_intent = new Intent();
        share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
        share_intent.setType("text/plain");//设置分享内容的类型
        share_intent.putExtra(Intent.EXTRA_SUBJECT, "分享怪兽课表");
        share_intent.putExtra(Intent.EXTRA_TEXT, content);//添加分享内容
        share_intent = Intent.createChooser(share_intent, "分享怪兽课表");
        startActivity(share_intent);
    }


    @OnCheckedChanged(R.id.id_switch_hidenotcur)
    public void onHideNotCurSwitchClicked(boolean b) {
        if (b) {
            ShareTools.putInt(this, "hidenotcur", 1);
        } else {
            ShareTools.putInt(this, "hidenotcur", 0);
        }
        ScheduleDao.changeStatus(this,true);
    }

    @OnCheckedChanged(R.id.id_switch_hideweekends)
    public void onHideWeekendsSwitchClicked(boolean b) {
        if (b) {
            ShareTools.putInt(this, "hideweekends", 1);
        } else {
            ShareTools.putInt(this, "hideweekends", 0);
        }
        ScheduleDao.changeStatus(this,true);
    }

    @OnCheckedChanged(R.id.id_checkauto)
    public void onCheckedAutoSwitchClicked(boolean b) {
        if (b) {
            ShareTools.putInt(this, "isIgnoreUpdate", 0);
        } else {
            ShareTools.putInt(this, "isIgnoreUpdate", 1);
        }
    }

    @OnCheckedChanged(R.id.id_switch_alone)
    public void onAloneSwitchClicked(boolean b) {
        if (b) {
            ShareTools.putInt(this, "isAlone", 1);
        } else {
            ShareTools.putInt(this, "isAlone", 0);
        }
        ScheduleDao.changeFuncStatus(this,true);
    }

    @OnCheckedChanged(R.id.id_widget_hideweeks)
    public void onCheckedHideWeeksSwitchClicked(boolean b) {
        WidgetConfig.apply(this,WidgetConfig.CONFIG_HIDE_WEEKS,b);
        BroadcastUtils.refreshAppWidget(this);
    }

    @OnCheckedChanged(R.id.id_widget_max15)
    public void onCheckedMax15SwitchClicked(boolean b) {
        WidgetConfig.apply(this,WidgetConfig.CONFIG_MAX_ITEM,b);
        BroadcastUtils.refreshAppWidget(this);
    }

    @OnCheckedChanged(R.id.id_widget_hidedate)
    public void onCheckedHideDateSwitchClicked(boolean b) {
        WidgetConfig.apply(this,WidgetConfig.CONFIG_HIDE_DATE,b);
        BroadcastUtils.refreshAppWidget(this);
    }

    @OnClick(R.id.id_debug)
    public void toDebug(){
        ActivityTools.toActivity(this,AdapterDebugTipActivity.class);
    }
}
