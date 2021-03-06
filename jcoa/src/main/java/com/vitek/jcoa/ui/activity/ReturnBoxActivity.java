package com.vitek.jcoa.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.misdk.ObserverListener;
import com.misdk.net.VolleyErrorManager;
import com.misdk.util.TimeUtil;
import com.misdk.util.ToastUtil;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.vitek.jcoa.JCOAApplication;
import com.vitek.jcoa.R;
import com.vitek.jcoa.base.GBaseNetAppCompatActivity;
import com.vitek.jcoa.net.CloudPlatformUtil;
import com.vitek.jcoa.net.JCOAApi;
import com.vitek.jcoa.net.entity.FindReturnLogEntity;
import com.vitek.jcoa.ui.adapter.ReturnBoxAdapter;
import com.vitek.jcoa.ui.view.MyCalendarView;
import com.vitek.jcoa.util.VLogUtil;

import java.util.Date;
import java.util.List;

/**
 * 退件箱
 */
public class ReturnBoxActivity extends GBaseNetAppCompatActivity<FindReturnLogEntity> {

    public static int DRAFT_STATE = 0;//0 all , 1 post ,2 no post
    private FrameLayout titleBack;
    private TextView tvTitle, tvTitleNowday;

    private RelativeLayout relaStartTime, relaEndTime;//开始时间&结束时间
    private TextView tvStartTime, tvEndTime;//日期
    private MyCalendarView myCalendarView;//日历控件
    private String startTime = TimeUtil.getStringDateyyyyMMdd2();
    private String endTime = TimeUtil.getStringDateyyyyMMdd2();
    private boolean isStart = true;
    private Date lastDate = new Date();//上一次的日期，默认今日防止null

    private List<FindReturnLogEntity.ModelsBean> datas;

    @Override
    protected void initVariables() {
        setTranslucentStatus(R.color.titleColor);
        pageSize = 1000;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_return_box;
    }


    @Override
    protected void findViews(Bundle savedInstanceState) {
        mPtrFrame.setBackgroundColor(getResources().getColor(R.color.white));

        titleBack = (FrameLayout) findViewById(R.id.titleBack);
        titleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText("退件箱");
        tvTitleNowday = (TextView) findViewById(R.id.tvTitleNowday);
        tvTitleNowday.setText(TimeUtil.getTitleDate());

        relaStartTime = (RelativeLayout) findViewById(R.id.relaStartTime);
        relaEndTime = (RelativeLayout) findViewById(R.id.relaEndTime);
        tvStartTime = (TextView) findViewById(R.id.tvStartTime);
        tvEndTime = (TextView) findViewById(R.id.tvEndTime);
        tvStartTime.setText(startTime);
        tvEndTime.setText(endTime);
        myCalendarView = new MyCalendarView(this);
        myCalendarView.initCalendarView(2017, 0, 1);
        relaStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStart = true;
                myCalendarView.showCalendarViewAsDropDown(v);
            }
        });
        relaEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStart = false;
                myCalendarView.showCalendarViewAsDropDown(v);
            }
        });
        myCalendarView.materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                clickDate(date.getDate(), isStart);
            }
        });
        // TODO: 2017/6/13 0013   接口暂时没有 按照日期查询
        relaStartTime.setVisibility(View.GONE);
        relaEndTime.setVisibility(View.GONE);
        getDialog().show();
        net(pageSize, true);
        JCOAApplication.getInstance().registerObserver(JCOAApplication.UPDATE_RETURNLOG, new ObserverListener() {
            @Override
            public void notifyChange(Bundle bundle, Object object) {
                getDialog().setCanceledOnTouchOutside(false);
                showDialog();
                net(pageSize, true);
            }
        });
    }


    private void clickDate(Date selectedDay, final boolean isStart) {
        if (myCalendarView.mPopupWindow != null && myCalendarView.mPopupWindow.isShowing()) {
            myCalendarView.mPopupWindow.dismiss();
            myCalendarView.mPopupWindow = null;
        }
        String date = TimeUtil.formatTime7(selectedDay);

        if (isStart) {
            if (!myCalendarView.verifyBigDate(date, endTime)) {
                ToastUtil.shortToast(this, "开始日期应小于截止日期");
                return;
            }
            startTime = date;
            tvStartTime.setText(startTime);
//            tvStartTime.setText(MyCalendarView.formatDateToyyyyNMMYddR(selectedDay));
        } else {
            if (!myCalendarView.verifyBigDate(startTime, date)) {
                ToastUtil.shortToast(this, "截止日期应大于开始日期");
                return;
            }
            endTime = date;
            tvEndTime.setText(endTime);
        }
        autoLoad(500); //设置完日期  ,  刷新
//        lastDate = selectedDay.getDate();//记录上一次日期
    }

    @Override
    protected void getNetData(int pageSize, boolean mode) {
        //查询退件箱  /jc_oa/findReturnLog  (POST))       FindReturnLogEntity   OK   2017-  6-7
        CloudPlatformUtil.getInstance().findReturnLog(JCOAApi.getFindReturnLogMap(), this);
    }

    @Override
    protected void onNetError(VolleyError error) {
        VLogUtil.e(VolleyErrorManager.getErrorMsg(error));
        dismissDialog();
    }

    @Override
    protected void onNetSuccess(FindReturnLogEntity entity) {
        if (entity.getModels() == null) return;
        datas = entity.getModels();
        if (mAdapter == null) {
            mAdapter = new ReturnBoxAdapter(this, datas, mInflater);
            mListView.setAdapter(mAdapter);
        } else {
            mAdapter.autoSetData(datas, mode);
        }
        dismissDialog();
    }

    @Override
    protected boolean hasMore(FindReturnLogEntity response) {
        return false;
    }

    @Override
    protected void onDestroy() {
//        JCOAApplication.getInstance().unRegisterObserver(JCOAApplication.UPDATE_DRAFT);
        super.onDestroy();
    }

}
