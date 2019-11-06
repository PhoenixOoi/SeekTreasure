package com.inti.seektreasure;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Create by OCF on 2019/10/24
 */

public class NotificationService extends Service {

    private Context mContext;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, MessageRef, FriendRef, FriendReqRef, rootRef;
    private String currentUserID, name;
    Boolean your_date_is_outdated = false;




    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            currentUserID = mAuth.getCurrentUser().getUid();
        }
        //store user information in Users parent node
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        MessageRef = FirebaseDatabase.getInstance().getReference().child("Message");
        FriendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        FriendReqRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        rootRef = FirebaseDatabase.getInstance().getReference();



    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("开始获取最新消息" + new Date().toString());
                //获取最新消息数据逻辑
                if(!TextUtils.isEmpty(currentUserID)){
                    ComparedFriendReq();
                }

//                if (your_date_is_outdated == true)
//                {
//                    setNotfication("New Message: ", name + " has send u a friend request");
//                }
                stopSelf();
            }
        });

        AlarmManager manger = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, AlarmReceiver.class);//广播接收
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, i, 0);//意图为开启广播
        long triggerAtTime = SystemClock.elapsedRealtime();//开机至今的时间毫秒数
        triggerAtTime = triggerAtTime + 5 * 1000;//比开机至今的时间增长10秒
        manger.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);//设置为开机至今的模式，时间，PendingIntent
        return super.onStartCommand(intent, flags, startId);
    }

    private void ComparedFriendReq()
    {

        FriendReqRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String uid = ds.getKey();

                    FriendReqRef.child(currentUserID).child(uid)
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {

                                        String requesttype = dataSnapshot.child("request_type").getValue(String.class);
                                        if (requesttype != null){
                                            if (!requesttype.equals("sent")) {
                                                String date = dataSnapshot.child("date").getValue(String.class);
                                                name = dataSnapshot.child("fullname").getValue(String.class);

                                                // Get Current Date Time
                                                Calendar calForDate = Calendar.getInstance();
                                                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                                                String getCurrentDate = currentDate.format(calForDate.getTime());
                                                String getMyDate = date;
                                                Log.d("getCurrentDate", getCurrentDate);// getCurrentDateTime: 05/23/2016 18:49 PM
                                                if (getMyDate != null) {

                                                    if (getCurrentDate.compareTo(getMyDate) < 0 || getCurrentDate.compareTo(getMyDate) == 0) {
                                                        your_date_is_outdated = true;
                                                        setNotfication("New Message: ", name + " has send u a friend request.");
//                                                    setNotfication("New Message: ", name + "has send u a friend request.");
//                                                new Handler().post(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        setNotfication("New Message: ", name + "has send u a friend request.");
//                                                    }
//
//                                                });

                                                    }
//                                            else if (getCurrentDate.compareTo(getMyDate) == 0)
//                                            {
//                                                your_date_is_outdated = true;
//                                                new Handler().post(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        setNotfication("New Message: ", name + "has send u a friend request.");
//                                                    }
//
//                                                });
//                                            }
                                                    else {
                                                        your_date_is_outdated = false;
                                                        Log.d("Return", "getMyTime older than getCurrentDateTime ");
                                                    }

                                                }
                                            }
                                    }
                                        else
                                        {
                                            Log.d("request_type", "sent ");
                                        }

//
//
//
//                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//                                        Date strDate = null;
//                                        try
//                                        {
//                                            strDate = sdf.parse(date);
//                                        }
//                                        catch (ParseException e)
//                                        {
//                                            e.printStackTrace();
//                                        }
//                                        if (new Date().after(strDate)) {
//                                            your_date_is_outdated = true;
////                                                    setNotfication("New Message: ", name + "has send u a friend request.");
//                                            new Handler().post(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    setNotfication("New Message: ", name + "has send u a friend request.");
//                                                }
//                                            });
//                                        }
//                                        else{
//                                            your_date_is_outdated = false;
//                                            new Handler().post(new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    setNotfication("New Message: ", name + "has send u a friend request.");
//                                                }
//                                            });
//
//                                        }

                                    }
                                    else
                                    {
                                        Log.d("No data", "No data");
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void setNotfication(String title, String msg) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            //当sdk版本大于26
            String id = "channel_1";
            String description = "143";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(id, description, importance);
            manager.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(mContext, id)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setSmallIcon(R.mipmap.ic_app_icon)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
            manager.notify(1, notification);
        } else {
            //当sdk版本小于26
            Notification notification = new NotificationCompat.Builder(mContext)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_app_icon)
                    .build();
            manager.notify(1, notification);
        }
    }
}
