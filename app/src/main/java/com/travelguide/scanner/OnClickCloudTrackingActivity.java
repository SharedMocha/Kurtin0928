package com.travelguide.scanner;

/**
 * Created by htammare on 8/14/2016.
 */

import android.app.ProgressDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;
import com.travelguide.R;
import com.travelguide.adapters.QuestionsAdapter;
import com.travelguide.decorations.DividerItemDecoration;
import com.travelguide.helpers.UpdatePointsandLeaderBoard;
import com.travelguide.models.Day;
import com.travelguide.models.LeaderBoard;
import com.travelguide.models.Questions;
import com.wikitude.WikitudeSDK;
import com.wikitude.WikitudeSDKStartupConfiguration;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.common.tracking.RecognizedTarget;
import com.wikitude.rendering.ExternalRendering;
import com.wikitude.tracker.CloudTracker;
import com.wikitude.tracker.CloudTrackerEventListener;
import com.wikitude.tracker.Tracker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.widget.ImageView.ScaleType.FIT_XY;
import static com.facebook.FacebookSdk.getApplicationContext;
import static com.travelguide.R.id.on_click_cloud_tracking_info_field;
import static com.travelguide.R.id.q1;

public class OnClickCloudTrackingActivity extends Fragment implements CloudTrackerEventListener, ExternalRendering {

    private static final String TAG = "OnClickCloudTracking";

    private WikitudeSDK _wikitudeSDK;
    private CustomSurfaceView _customSurfaceView;
    private Driver _driver;
    private GLRenderer _glRenderer;
    private CloudTracker _cloudTracker;
    public ProgressDialog pDialog;
    LayoutInflater inflater;
    FrameLayout viewHolder;

    View controls;
    DisplayMetrics dm;
    int height;
    int width;
    WebView webViewYT;

    int webviewHeight;
    int quadheightsmall;

    String wikitudeTargetCollectionId;
    String wikitudeClientId;
    public static String selectedValueToSave;
    public static String questionIDToSave;

    private List<Questions> mQuestionsList;
    private QuestionsAdapter mQuestionsAdapter;
    private String mSelectedDayObjectId = "";
    Button recognizeButton;
    TextView completedTV;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        controls = inflater.inflate(R.layout.activity_on_click_cloud_tracking, container, false);

//        initUI();
        initData();
        return controls;

    }

    public void initData(){


        //First get fodler id
        try {
            Bundle bundle = this.getArguments();
            String id = bundle.getString("dayid");
            mSelectedDayObjectId = id;

            wikitudeTargetCollectionId = bundle.getString("wikitudeTargetCollectionId");
            wikitudeClientId =  bundle.getString("wikitudeClientId");


        } catch (Exception e) {
            e.printStackTrace();
        }


        _wikitudeSDK = new WikitudeSDK(this);
        WikitudeSDKStartupConfiguration startupConfiguration = new WikitudeSDKStartupConfiguration(WikitudeSDKConstants.WIKITUDE_SDK_KEY, CameraSettings.CameraPosition.BACK, CameraSettings.CameraFocusMode.CONTINUOUS);
        _wikitudeSDK.onCreate(getActivity(), startupConfiguration);
//        _cloudTracker = _wikitudeSDK.getTrackerManager().create2dCloudTracker("9d7455e2496e33864ca0ac3223be7d8e", "57c784b6ca93c49267be69c2");
        _cloudTracker = _wikitudeSDK.getTrackerManager().create2dCloudTracker(wikitudeClientId, wikitudeTargetCollectionId);
        _cloudTracker.registerTrackerEventListener(this);



        dm = getResources().getDisplayMetrics();
        height = dm.heightPixels;
        width = dm.widthPixels;

        webviewHeight = dm.heightPixels/2;

        double fheight = height/2.3;
        quadheightsmall=(int)fheight;

        mQuestionsList = new ArrayList<Questions>();
        mQuestionsAdapter = new QuestionsAdapter(mQuestionsList, getContext());



    }


    @Override
    public void onResume() {
        super.onResume();
        _wikitudeSDK.onResume();
        _customSurfaceView.onResume();
        _driver.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        _wikitudeSDK.onPause();
        _customSurfaceView.onPause();
        _driver.stop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _wikitudeSDK.onDestroy();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        destroyWebView();
    }

    @Override
    public void onRenderExtensionCreated(final RenderExtension renderExtension_) {


        _glRenderer = new GLRenderer(renderExtension_);
        _customSurfaceView = new CustomSurfaceView(getActivity(), _glRenderer);
        _driver = new Driver(_customSurfaceView, 30);

        viewHolder = (FrameLayout)controls.findViewById(R.id.track_frame) ;

        viewHolder.addView(_customSurfaceView);


        recognizeButton = (Button)controls. findViewById(R.id.on_click_cloud_tracking_recognize_button);
        recognizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view_) {
                _cloudTracker.recognize();

                Log.e(TAG, "onClick: recognizeButton" );

                controls.findViewById(q1).setVisibility(View.GONE);
                controls.findViewById(R.id.q2).setVisibility(View.GONE);
                controls.findViewById(R.id.q3).setVisibility(View.GONE);
                controls.findViewById(R.id.q4).setVisibility(View.GONE);
                controls.findViewById(on_click_cloud_tracking_info_field).setVisibility(View.GONE);

                destroyWebView();



            }
        });

    }
    @Override
    public void onTrackerFinishedLoading(final CloudTracker cloudTracker_) {

    }

    @Override
    public void onTrackerLoadingError(final CloudTracker cloudTracker_, final String errorMessage_) {
        Log.d(TAG, "onTrackerLoadingError: " + errorMessage_);
    }

    @Override
    public void onTargetRecognized(final Tracker cloudTracker_, final String targetName_) {

    }

    @Override
    public void onTracking(final Tracker cloudTracker_, final RecognizedTarget recognizedTarget_) {
        _glRenderer.setCurrentlyRecognizedTarget(recognizedTarget_);
    }

    @Override
    public void onTargetLost(final Tracker cloudTracker_, final String targetName_) {
        _glRenderer.setCurrentlyRecognizedTarget(null);
    }

    @Override
    public void onExtendedTrackingQualityUpdate(final Tracker tracker_, final String targetName_, final int oldTrackingQuality_, final int newTrackingQuality_) {

    }

    @Override
    public void onRecognitionFailed(final CloudTracker cloudTracker_, final int errorCode_, final String errorMessage_) {
        try {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        EditText targetInformationTextField = (EditText) controls.findViewById(on_click_cloud_tracking_info_field);
                        targetInformationTextField.setText("Recognition failed - Error code: " + errorCode_ + " Message: " + errorMessage_);
                        targetInformationTextField.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRecognitionSuccessful(final CloudTracker cloudTracker_, boolean recognized_, final JSONObject jsonObject_) {
        try {
            Log.e(TAG, "onRecognitionSuccessful:jsonObject_:  "+jsonObject_ );

            if (recognized_) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String q1Type = null;
                        String q2Type = null;
                        String q3Type = null;
                        String q4Type = null;
                        String levleID = null;
                        String q1DataSource = null;
                        String q2DataSource = null;
                        String q3DataSource = null;
                        String q4DataSource = null;
                        Uri q1DataSourceURI = null;
                        Uri q2DataSourceURI = null;
                        Uri q3DataSourceURI = null;
                        Uri q4DataSourceURI = null;
                        String nameToDisplay = null;
                        System.out.println(jsonObject_.toString());

                        try {
                            //Quadrant- Type
                            JSONObject metadata = jsonObject_.getJSONObject("metadata");
                            System.out.println(metadata.toString());
                            q1Type = metadata.getString("q1Type");
                            q2Type = metadata.getString("q2Type");
                            q3Type = metadata.getString("q3Type");
                            q4Type = metadata.getString("q4Type");
                            levleID = metadata.getString("level_id");
                            System.out.println(q4Type.toString());

                            //get name
                            nameToDisplay = metadata.getString("idetified_name");
                            //Quadrant- Source
                            q1DataSource = metadata.getString("q1DataSource");
                            q1DataSourceURI = Uri.parse(q1DataSource);
                            q2DataSource = metadata.getString("q2DataSource");
                            q2DataSourceURI = Uri.parse(q2DataSource);
                            q3DataSource = metadata.getString("q3DataSource");
                            q3DataSourceURI = Uri.parse(q3DataSource);
                            q4DataSource = metadata.getString("q4DataSource");
                            q4DataSourceURI = Uri.parse(q4DataSource);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(levleID.equals(mSelectedDayObjectId.toString())){

                            ArrayList QTypes = new ArrayList();
                            QTypes.add(0, q1Type);
                            QTypes.add(1, q2Type);
                            QTypes.add(2, q3Type);
                            QTypes.add(3, q4Type);


                            final RelativeLayout q1 = (RelativeLayout) controls.findViewById(R.id.q1);
                            final RelativeLayout q2 = (RelativeLayout) controls.findViewById(R.id.q2);
                            final RelativeLayout q3 = (RelativeLayout) controls.findViewById(R.id.q3);
                            final RelativeLayout q4 = (RelativeLayout) controls.findViewById(R.id.q4);

                            final Button submitbtn1 = new Button(getActivity());
                            final Button submitbtn2 = new Button(getActivity());
                            final Button submitbtn3 = new Button(getActivity());
                            final Button submitbtn4 = new Button(getActivity());

                            completedTV = new TextView(getActivity());



                            final EditText on_click_cloud_tracking_info = (EditText) controls.findViewById(on_click_cloud_tracking_info_field);

                            final String QuadHeadname = nameToDisplay;

                            setQ1Small(q1, q2, q3, q4,submitbtn1,on_click_cloud_tracking_info,QuadHeadname);
                            setQ2Small(q1, q2, q3, q4,submitbtn2,on_click_cloud_tracking_info,QuadHeadname);
                            setQ3Small(q1, q2, q3, q4,submitbtn3,on_click_cloud_tracking_info,QuadHeadname);
                            setQ4Small(q1, q2, q3, q4,submitbtn4,on_click_cloud_tracking_info,QuadHeadname);

                            setSubmitButton("0",submitbtn1);
                            setSubmitButton("0",submitbtn2);
                            setSubmitButton("0",submitbtn3);
                            setSubmitButton("0",submitbtn4);
                            setCompletedText("0",completedTV);



                            Log.e(TAG, "run: Display: " + height + "---" + width);

                            Log.e(TAG, "run: For loop starts");

                            for (int i = 0; i <= QTypes.size(); i++) {
                                if (i == 0) {
//                             LinearLayout q1 = (LinearLayout)controls. findViewById(R.id.q1); // get your WebView form your xml file
                                    q1.removeAllViews();
                                    String tempQType = QTypes.get(0).toString();
                                    switch (tempQType) {
                                        case "Video":
                                            final ImageView b1 = new ImageView(getActivity());
                                            final ImageView b2 = new ImageView(getActivity());

                                            b1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    b2.setVisibility(View.VISIBLE);
                                                    b1.setVisibility(View.GONE);
                                                    setQ1Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn1,completedTV);

                                                }
                                            });

                                            b2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    b2.setVisibility(View.GONE);
                                                    b1.setVisibility(View.VISIBLE);
                                                    setQ1Small(q1, q2, q3, q4,submitbtn1,on_click_cloud_tracking_info,QuadHeadname);
                                                }
                                            });
                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);
                                            System.out.println("Choice1 selected");
                                            VideoView videoView = new VideoView(getActivity());
                                            videoView.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
                                            videoView.setVideoURI(q1DataSourceURI);
                                            videoView.setMediaController(new MediaController(getActivity()));
                                            videoView.requestFocus();
                                            q1.setVisibility(View.VISIBLE);

                                            q1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {

                                                }
                                            });
                                            RecyclerView recyclerView1 = new RecyclerView(getActivity());
                                            setVideoviewAddview(q1 , videoView,recyclerView1,b1, b2);
                                            break;
                                        case "WebPage":
                                            final ImageView webb1 = new ImageView(getActivity());
                                            final ImageView webb2 = new ImageView(getActivity());
                                            webb1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    webb2.setVisibility(View.VISIBLE);
                                                    webb1.setVisibility(View.GONE);
                                                    setQ1Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn1,completedTV);
                                                }
                                            });
                                            webb2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    webb2.setVisibility(View.GONE);
                                                    webb2.setVisibility(View.VISIBLE);
                                                    setQ1Small(q1, q2, q3, q4,submitbtn1,on_click_cloud_tracking_info,QuadHeadname);
                                                }
                                            });

                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);
                                            System.out.println("Choice2 selected");
                                            WebView webView = new WebView(getActivity());
                                            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            webView.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.FILL_PARENT,
                                                    LinearLayout.LayoutParams.FILL_PARENT));
                                            webView.setWebViewClient(new WebViewClient()); // set the WebViewClient
                                            webView.loadUrl(q1DataSource); // Load your desired url
                                            webView.getSettings().setBuiltInZoomControls(true);
                                            if (Build.VERSION.SDK_INT >= 11) {
                                                webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                                            }
                                            webView.getSettings().setJavaScriptEnabled(true);
                                            webView.getSettings().setLoadWithOverviewMode(true);
                                            webView.getSettings().setUseWideViewPort(true);
                                            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                                            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

                                            RecyclerView recyclerView2 = new RecyclerView(getActivity());
                                            setWebviewAddview(q1 , webView,recyclerView2,webb1, webb2,submitbtn1);

                                            break;
                                        case "Image":
                                            final ImageView imgb1 = new ImageView(getActivity());
                                            final ImageView imgb2 = new ImageView(getActivity());


                                            imgb1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    imgb2.setVisibility(View.VISIBLE);
                                                    imgb1.setVisibility(View.GONE);
                                                    setQ1Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn1,completedTV);
                                                }
                                            });
                                            imgb2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    imgb2.setVisibility(View.GONE);
                                                    imgb2.setVisibility(View.VISIBLE);
                                                    setQ1Small(q1, q2, q3, q4,submitbtn1,on_click_cloud_tracking_info,QuadHeadname);
                                                }
                                            });

                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);
                                            System.out.println("Choice3 selected");
                                            ImageView imageView = new ImageView(getActivity());
                                            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.FILL_PARENT,
                                                    webviewHeight, Gravity.CENTER));
                                            Picasso.with(getActivity()).load(q1DataSourceURI).into(imageView);
                                            imageView.setScaleType(FIT_XY);
                                            imageView.setAdjustViewBounds(true);
                                            imageView.setBackgroundColor(Color.BLACK);
                                            RecyclerView recyclerView3 = new RecyclerView(getActivity());
                                            setImageAddview(q1 , imageView,recyclerView3,imgb1, imgb2,submitbtn1);

                                            break;
                                        case "YouTubeVideo":
                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);

                                            final ImageView youb1 = new ImageView(getActivity());
                                            final ImageView youb2 = new ImageView(getActivity());

                                            System.out.println("Choice3 selected");
                                            webViewYT = new WebView(getActivity());
                                            webViewYT.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                    webviewHeight));
                                            String frameString = "<html><body style='margin:0;padding:0;'><iframe src=\"" +
                                                    q1DataSourceURI +
                                                    "\" height=\"100%\" width=\"100%\" frameborder=\"0\" allowfullscreen></iframe></body></html>";
                                            webViewYT.setWebViewClient(new WebViewClient() {
                                                @Override
                                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                    return false;
                                                }
                                            });
                                            webViewYT.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            WebSettings webSettings = webViewYT.getSettings();
                                            webSettings.setJavaScriptEnabled(true);
                                            webViewYT.setBackgroundColor(Color.BLACK);
                                            webViewYT.loadData(frameString, "text/html", "utf-8");
                                            webViewYT.getSettings().setJavaScriptEnabled(true);
                                            webViewYT.getSettings().setLoadWithOverviewMode(true);
                                            webViewYT.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                                            webViewYT.setVerticalScrollBarEnabled(false);
                                            webViewYT.setVisibility(View.VISIBLE);

                                            q1.setVisibility(View.VISIBLE);

                                            youb1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    youb2.setVisibility(View.VISIBLE);
                                                    youb1.setVisibility(View.GONE);
                                                    setQ1Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn1,completedTV);
                                                }
                                            });

                                            youb2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    youb2.setVisibility(View.GONE);
                                                    youb1.setVisibility(View.VISIBLE);
                                                    setQ1Small(q1, q2, q3, q4,submitbtn1,on_click_cloud_tracking_info,QuadHeadname);
                                                }
                                            });


                                            RecyclerView recyclerView4 = new RecyclerView(getActivity());
                                            setWebviewAddview(q1 , webViewYT,recyclerView4,youb1, youb2,submitbtn1);

                                            break;
                                        default:
                                            Log.e(TAG, "run: Default00000");
                                            System.out.println("Choice2 selected");
                                            WebView webViewdefault = new WebView(getActivity());
                                            webViewdefault.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            webViewdefault.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.FILL_PARENT,
                                                    LinearLayout.LayoutParams.FILL_PARENT));
                                            webViewdefault.setWebViewClient(new WebViewClient()); // set the WebViewClient
                                            webViewdefault.loadUrl(q1DataSource); // Load your desired url
                                            webViewdefault.getSettings().setBuiltInZoomControls(true);
                                            if (Build.VERSION.SDK_INT >= 11) {
                                                webViewdefault.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                                            }
                                            webViewdefault.getSettings().setJavaScriptEnabled(true);
                                            webViewdefault.getSettings().setLoadWithOverviewMode(true);
                                            webViewdefault.getSettings().setUseWideViewPort(true);
                                            webViewdefault.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                                            webViewdefault.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            q1.addView(webViewdefault);
                                            q1.setVisibility(View.VISIBLE);
                                            break;
                                    }
                                } else if (i == 1) {
//                            LinearLayout q2 = (LinearLayout) controls.findViewById(R.id.q2); // get your WebView form your xml file
                                    String tempQType = QTypes.get(1).toString();
                                    q2.removeAllViews();
                                    q2.removeAllViewsInLayout();

                                    switch (tempQType) {
                                        case "Video":
                                            final ImageView vidb1 = new ImageView(getActivity());
                                            final ImageView vidb2 = new ImageView(getActivity());

                                            vidb1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    vidb2.setVisibility(View.VISIBLE);
                                                    vidb1.setVisibility(View.GONE);
                                                    setQ2Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn2);
                                                }
                                            });
                                            vidb2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    vidb2.setVisibility(View.GONE);
                                                    vidb1.setVisibility(View.VISIBLE);
                                                    setQ2Small(q1, q2, q3, q4,submitbtn2,on_click_cloud_tracking_info,QuadHeadname);
                                                }
                                            });

                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);
                                            System.out.println("Choice1 selected");
                                            VideoView videoView = new VideoView(getActivity());
                                            videoView.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.FILL_PARENT,
                                                    webviewHeight, Gravity.CENTER));
                                            videoView.setVideoURI(q2DataSourceURI);
                                            videoView.setMediaController(new MediaController(getActivity()));
                                            videoView.requestFocus();
                                            RecyclerView recyclerView1 = new RecyclerView(getActivity());
                                            setVideoviewAddview(q2 , videoView,recyclerView1,vidb1, vidb2);

                                            break;
                                        case "WebPage":
                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);

                                            final ImageView b1 = new ImageView(getActivity());
                                            final ImageView b2 = new ImageView(getActivity());

                                            System.out.println("Choice2 selected");
                                            WebView webView = new WebView(getActivity());
                                            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            webView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, webviewHeight));
                                            webView.setWebViewClient(new WebViewClient()); // set the WebViewClient
                                            webView.loadUrl(q2DataSource); // Load your desired url
                                            webView.getSettings().setBuiltInZoomControls(true);
                                            if (Build.VERSION.SDK_INT >= 11) {
                                                webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                                            }
                                            webView.getSettings().setJavaScriptEnabled(true);
                                            webView.getSettings().setLoadWithOverviewMode(true);
                                            webView.getSettings().setUseWideViewPort(true);
                                            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                                            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);


                                            b1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    b2.setVisibility(View.VISIBLE);
                                                    b1.setVisibility(View.GONE);
                                                    setQ2Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn2);
                                                }
                                            });

                                            b2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    b2.setVisibility(View.GONE);
                                                    b1.setVisibility(View.VISIBLE);
                                                    setQ2Small(q1, q2, q3, q4,submitbtn2,on_click_cloud_tracking_info,QuadHeadname);
                                                }
                                            });
                                            RecyclerView recyclerView2 = new RecyclerView(getActivity());
                                            setWebviewAddview(q2 , webView,recyclerView2,b1, b2,submitbtn2);
                                            break;
                                        case "Image":

                                            final ImageView imgb1 = new ImageView(getActivity());
                                            final ImageView imgb2 = new ImageView(getActivity());
                                            imgb1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    imgb2.setVisibility(View.VISIBLE);
                                                    imgb1.setVisibility(View.GONE);
                                                    setQ2Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn2);
                                                }
                                            });
                                            imgb2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    imgb2.setVisibility(View.GONE);
                                                    imgb1.setVisibility(View.VISIBLE);
                                                    setQ2Small(q1, q2, q3, q4,submitbtn2,on_click_cloud_tracking_info,QuadHeadname);
//                                                q1Click();
                                                }
                                            });

                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);
                                            System.out.println("Choice3 selected");
                                            ImageView imageView = new ImageView(getActivity());
                                            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.FILL_PARENT,
                                                    webviewHeight, Gravity.CENTER));
                                            Picasso.with(getActivity()).load(q2DataSourceURI).into(imageView);
                                            imageView.setScaleType(FIT_XY);
                                            imageView.setAdjustViewBounds(true);
                                            imageView.setBackgroundColor(Color.BLACK);
                                            RecyclerView recyclerView3 = new RecyclerView(getActivity());
                                            setImageAddview(q2 , imageView,recyclerView3,imgb1, imgb2,submitbtn2);

                                            break;
                                        case "YouTubeVideo":
                                            final ImageView youb1 = new ImageView(getActivity());
                                            final ImageView youb2 = new ImageView(getActivity());



                                            youb1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    youb2.setVisibility(View.VISIBLE);
                                                    youb1.setVisibility(View.GONE);
                                                    setQ2Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn2);
                                                }
                                            });
                                            youb2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    youb2.setVisibility(View.GONE);
                                                    youb1.setVisibility(View.VISIBLE);
                                                    setQ2Small(q1, q2, q3, q4,submitbtn2,on_click_cloud_tracking_info,QuadHeadname);
                                                }
                                            });
                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);
                                            System.out.println("Choice3 selected");
                                            webViewYT = new WebView(getActivity());
                                            webViewYT.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                    LinearLayout.LayoutParams.MATCH_PARENT));
                                            String frameString = "<html><body style='margin:0;padding:0;'><iframe src=\"" +
                                                    q2DataSourceURI +
                                                    "\" height=\"100%\" width=\"100%\" frameborder=\"0\" allowfullscreen></iframe></body></html>";
                                            webViewYT.setWebViewClient(new WebViewClient() {
                                                @Override
                                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                    return false;
                                                }
                                            });
                                            webViewYT.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            WebSettings webSettings = webViewYT.getSettings();
                                            webSettings.setJavaScriptEnabled(true);
                                            webViewYT.setBackgroundColor(Color.BLACK);
                                            webViewYT.loadData(frameString, "text/html", "utf-8");
                                            webViewYT.getSettings().setJavaScriptEnabled(true);
                                            webViewYT.getSettings().setLoadWithOverviewMode(true);
                                            webViewYT.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                                            if (Build.VERSION.SDK_INT >= 11) {
                                                webViewYT.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                                            }
                                            webViewYT.setVerticalScrollBarEnabled(false);
                                            RecyclerView recyclerView4 = new RecyclerView(getActivity());
                                            setWebviewAddview(q2 , webViewYT,recyclerView4,youb1, youb2,submitbtn2);

                                            break;
                                        default:
                                            Log.e(TAG, "run: Default11111");
                                            System.out.println("Choice2 selected");
                                            WebView webViewdefault = new WebView(getActivity());
                                            webViewdefault.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            webViewdefault.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.FILL_PARENT,
                                                    LinearLayout.LayoutParams.FILL_PARENT));
                                            webViewdefault.setWebViewClient(new WebViewClient()); // set the WebViewClient
                                            webViewdefault.loadUrl(q2DataSource); // Load your desired url
                                            webViewdefault.getSettings().setBuiltInZoomControls(true);
                                            if (Build.VERSION.SDK_INT >= 11) {
                                                webViewdefault.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                                            }
                                            webViewdefault.getSettings().setJavaScriptEnabled(true);
                                            webViewdefault.getSettings().setLoadWithOverviewMode(true);
                                            webViewdefault.getSettings().setUseWideViewPort(true);
                                            webViewdefault.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                                            webViewdefault.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            q2.addView(webViewdefault);
                                            q2.setVisibility(View.VISIBLE);
                                            break;
                                    }
                                } else if (i == 2) {
//                            LinearLayout q3 = (LinearLayout) controls.findViewById(R.id.q3); // get your WebView form your xml file
                                    String tempQType = QTypes.get(2).toString();
                                    q3.removeAllViews();
                                    switch (tempQType) {
                                        case "Video":

                                            final ImageView vidb1 = new ImageView(getActivity());
                                            final ImageView vidb2 = new ImageView(getActivity());
                                            vidb1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    vidb2.setVisibility(View.VISIBLE);
                                                    vidb1.setVisibility(View.GONE);
                                                    setQ3Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn3);
                                                }
                                            });
                                            vidb2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    vidb2.setVisibility(View.GONE);
                                                    vidb1.setVisibility(View.VISIBLE);
                                                    setQ3Small(q1, q2, q3, q4,submitbtn3,on_click_cloud_tracking_info,QuadHeadname);
                                                }
                                            });

                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);
                                            System.out.println("Choice1 selected");
                                            VideoView videoView = new VideoView(getActivity());
                                            videoView.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.FILL_PARENT,
                                                    webviewHeight, Gravity.CENTER));
                                            videoView.setVideoURI(q3DataSourceURI);
                                            videoView.setMediaController(new MediaController(getActivity()));
                                            videoView.requestFocus();
                                            //videoView.start();

                                            RecyclerView recyclerView1 = new RecyclerView(getActivity());
                                            setVideoviewAddview(q3 , videoView,recyclerView1,vidb1, vidb2);

                                            break;
                                        case "WebPage":

                                            final ImageView webb1 = new ImageView(getActivity());
                                            final ImageView webb2 = new ImageView(getActivity());
                                            webb1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    webb2.setVisibility(View.VISIBLE);
                                                    webb1.setVisibility(View.GONE);
                                                    setQ3Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn3);
                                                }
                                            });
                                            webb2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    webb2.setVisibility(View.GONE);
                                                    webb1.setVisibility(View.VISIBLE);
                                                    setQ3Small(q1, q2, q3, q4,submitbtn3,on_click_cloud_tracking_info,QuadHeadname);
                                                }
                                            });
                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);
                                            System.out.println("Choice2 selected");
                                            WebView webView = new WebView(getActivity());
                                            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            webView.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                    webviewHeight));
                                            webView.setWebViewClient(new WebViewClient()); // set the WebViewClient
                                            webView.loadUrl(q3DataSource); // Load your desired url
                                            webView.getSettings().setBuiltInZoomControls(true);
                                            if (Build.VERSION.SDK_INT >= 11) {
                                                webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                                            }
                                            webView.getSettings().setJavaScriptEnabled(true);
                                            webView.getSettings().setLoadWithOverviewMode(true);
                                            webView.getSettings().setUseWideViewPort(true);
                                            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                                            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

                                            RecyclerView recyclerView2 = new RecyclerView(getActivity());
                                            setWebviewAddview(q3 , webView,recyclerView2,webb1, webb2,submitbtn3);

                                            break;
                                        case "Image":
                                            final ImageView b1 = new ImageView(getActivity());
                                            final ImageView b2 = new ImageView(getActivity());
                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);
                                            System.out.println("Choice3 selected");
                                            ImageView imageView = new ImageView(getActivity());
                                            Picasso.with(getActivity()).load(q3DataSourceURI).into(imageView);
                                            imageView.setScaleType(FIT_XY);
                                            imageView.setAdjustViewBounds(true);
                                            imageView.setBackgroundColor(Color.BLACK);

                                            RecyclerView recyclerView3 = new RecyclerView(getActivity());
                                            setImageAddview(q3 , imageView,recyclerView3,b1, b2,submitbtn3);

                                            b1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    b2.setVisibility(View.VISIBLE);
                                                    b1.setVisibility(View.GONE);
                                                    setQ3Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn3);

                                                }
                                            });


                                            b2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    setQ3Small(q1, q2, q3, q4,submitbtn3,on_click_cloud_tracking_info,QuadHeadname);
                                                    b2.setVisibility(View.GONE);
                                                    b1.setVisibility(View.VISIBLE);
                                                }
                                            });

                                            break;
                                        case "YouTubeVideo":
                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);

                                            final ImageView youb1 = new ImageView(getActivity());
                                            final ImageView youb2 = new ImageView(getActivity());
                                            youb1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    youb2.setVisibility(View.VISIBLE);
                                                    youb1.setVisibility(View.GONE);
                                                    setQ3Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn3);
                                                }
                                            });
                                            youb2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    youb2.setVisibility(View.GONE);
                                                    youb1.setVisibility(View.VISIBLE);
                                                    setQ3Small(q1, q2, q3, q4,submitbtn3,on_click_cloud_tracking_info,QuadHeadname);
                                                }
                                            });

                                            System.out.println("Choice3 selected");
                                            webViewYT = new WebView(getActivity());
                                            webViewYT.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.FILL_PARENT,
                                                    webviewHeight));
                                            String frameString = "<html><body style='margin:0;padding:0;'><iframe src=\"" +
                                                    q3DataSourceURI +
                                                    "\" height=\"100%\" width=\"100%\" frameborder=\"0\" allowfullscreen></iframe></body></html>";
                                            webViewYT.setWebViewClient(new WebViewClient() {
                                                @Override
                                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                    return false;
                                                }
                                            });
                                            webViewYT.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            WebSettings webSettings = webViewYT.getSettings();
                                            webSettings.setJavaScriptEnabled(true);
                                            webViewYT.setBackgroundColor(Color.BLACK);
                                            webViewYT.loadData(frameString, "text/html", "utf-8");
                                            webViewYT.getSettings().setJavaScriptEnabled(true);
                                            webViewYT.getSettings().setLoadWithOverviewMode(true);
                                            webViewYT.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                                            if (Build.VERSION.SDK_INT >= 11) {
                                                webViewYT.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                                            }
                                            webViewYT.setVerticalScrollBarEnabled(false);
                                            RecyclerView recyclerView4 = new RecyclerView(getActivity());
                                            setWebviewAddview(q3 , webViewYT,recyclerView4,youb1, youb2,submitbtn3);

                                            break;
                                        default:
                                            Log.e(TAG, "run: Default222");
                                            System.out.println("Choice2 selected");
                                            WebView webViewdefault = new WebView(getActivity());
                                            webViewdefault.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            webViewdefault.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.FILL_PARENT,
                                                    LinearLayout.LayoutParams.FILL_PARENT));
                                            webViewdefault.setWebViewClient(new WebViewClient()); // set the WebViewClient
                                            webViewdefault.loadUrl(q3DataSource); // Load your desired url
                                            webViewdefault.getSettings().setBuiltInZoomControls(true);
                                            if (Build.VERSION.SDK_INT >= 11) {
                                                webViewdefault.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                                            }
                                            webViewdefault.getSettings().setJavaScriptEnabled(true);
                                            webViewdefault.getSettings().setLoadWithOverviewMode(true);
                                            webViewdefault.getSettings().setUseWideViewPort(true);
                                            webViewdefault.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                                            webViewdefault.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            q3.addView(webViewdefault);
                                            q3.setVisibility(View.VISIBLE);
                                            break;
                                    }
                                } else if (i == 3) {
//                            LinearLayout q4 = (LinearLayout) controls.findViewById(R.id.q4); // get your WebView form your xml file
                                    String tempQType = QTypes.get(3).toString();
                                    q4.removeAllViews();
                                    switch (tempQType) {
                                        case "Video":
                                            final ImageView vidb1 = new ImageView(getActivity());
                                            final ImageView vidb2 = new ImageView(getActivity());
                                            vidb1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    vidb2.setVisibility(View.VISIBLE);
                                                    vidb1.setVisibility(View.GONE);
                                                    setQ4Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn4);
                                                }
                                            });
                                            vidb2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    vidb2.setVisibility(View.GONE);
                                                    vidb1.setVisibility(View.VISIBLE);
                                                    setQ4Small(q1, q2, q3, q4,submitbtn4,on_click_cloud_tracking_info,QuadHeadname);
//                                                q1Click();
                                                }
                                            });
                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);
                                            System.out.println("Choice1 selected");
                                            VideoView videoView = new VideoView(getActivity());
                                            videoView.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.FILL_PARENT,
                                                    webviewHeight, Gravity.CENTER));
                                            videoView.setVideoURI(q4DataSourceURI);
                                            videoView.setMediaController(new MediaController(getActivity()));
                                            videoView.requestFocus();
                                            RecyclerView recyclerView1 = new RecyclerView(getActivity());
                                            setVideoviewAddview(q4 , videoView,recyclerView1,vidb1, vidb2);

                                            break;
                                        case "WebPage":
                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);
                                            final ImageView b1 = new ImageView(getActivity());
                                            final ImageView b2 = new ImageView(getActivity());

                                            System.out.println("Choice2 selected");
                                            WebView webView = new WebView(getActivity());
                                            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            webView.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                                    webviewHeight));
                                            webView.setWebViewClient(new WebViewClient()); // set the WebViewClient
                                            webView.loadUrl(q4DataSource); // Load your desired url
                                            webView.getSettings().setBuiltInZoomControls(true);
                                            if (Build.VERSION.SDK_INT >= 11) {
                                                webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                                            }
                                            webView.getSettings().setJavaScriptEnabled(true);
                                            webView.getSettings().setLoadWithOverviewMode(true);
                                            webView.getSettings().setUseWideViewPort(true);
                                            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                                            webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            RecyclerView recyclerView2 = new RecyclerView(getActivity());
                                            setWebviewAddview(q4 , webView,recyclerView2,b1, b2,submitbtn4);

                                            b1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    b2.setVisibility(View.VISIBLE);
                                                    b1.setVisibility(View.GONE);
                                                    setQ4Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn4);
                                                }
                                            });

                                            b2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    b2.setVisibility(View.GONE);
                                                    b1.setVisibility(View.VISIBLE);
                                                    setQ4Small(q1, q2, q3, q4,submitbtn4,on_click_cloud_tracking_info,QuadHeadname);
                                                }
                                            });

                                            break;
                                        case "Image":
                                            final ImageView imgb1 = new ImageView(getActivity());
                                            final ImageView imgb2 = new ImageView(getActivity());
                                            imgb1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    imgb2.setVisibility(View.VISIBLE);
                                                    imgb1.setVisibility(View.GONE);
                                                    setQ4Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn4);
                                                }
                                            });
                                            imgb2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    imgb2.setVisibility(View.GONE);
                                                    imgb1.setVisibility(View.VISIBLE);
                                                    setQ4Small(q1, q2, q3, q4,submitbtn4,on_click_cloud_tracking_info,QuadHeadname);
//                                                q1Click();
                                                }
                                            });

                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);
                                            System.out.println("Choice3 selected");
                                            ImageView imageView = new ImageView(getActivity());
                                            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.FILL_PARENT,
                                                    webviewHeight, Gravity.CENTER));
                                            Picasso.with(getActivity()).load(q4DataSourceURI).into(imageView);
                                            imageView.setScaleType(FIT_XY);
                                            imageView.setAdjustViewBounds(true);
                                            imageView.setBackgroundColor(Color.BLACK);
                                            RecyclerView recyclerView3 = new RecyclerView(getActivity());
                                            setImageAddview(q4 , imageView,recyclerView3,imgb1, imgb2,submitbtn4);

                                            break;
                                        case "YouTubeVideo":
                                            final ImageView youb1 = new ImageView(getActivity());
                                            final ImageView youb2 = new ImageView(getActivity());

                                            youb1.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    youb2.setVisibility(View.VISIBLE);
                                                    youb1.setVisibility(View.GONE);
                                                    setQ4Large(q1, q2, q3, q4, on_click_cloud_tracking_info,submitbtn4);
                                                }
                                            });
                                            youb2.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    youb2.setVisibility(View.GONE);
                                                    youb1.setVisibility(View.VISIBLE);
                                                    setQ4Small(q1, q2, q3, q4,submitbtn4,on_click_cloud_tracking_info,QuadHeadname);
                                                }
                                            });
                                            Log.e(TAG, "Case::-- " + i + "-----" + tempQType);
                                            System.out.println("Choice3 selected");
                                            webViewYT = new WebView(getActivity());
                                            webViewYT.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                                    LinearLayout.LayoutParams.MATCH_PARENT));
                                            String frameString = "<html><body style='margin:0;padding:0;'><iframe src=\"" +
                                                    q4DataSourceURI +
                                                    "\" height=\"100%\" width=\"100%\" frameborder=\"0\" allowfullscreen></iframe></body></html>";
                                            webViewYT.setWebViewClient(new WebViewClient() {
                                                @Override
                                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                    return false;
                                                }
                                            });
                                            webViewYT.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            WebSettings webSettings = webViewYT.getSettings();
                                            webSettings.setJavaScriptEnabled(true);
                                            webViewYT.setBackgroundColor(Color.BLACK);
                                            webViewYT.loadData(frameString, "text/html", "utf-8");
                                            webViewYT.getSettings().setJavaScriptEnabled(true);
                                            webViewYT.getSettings().setLoadWithOverviewMode(true);
                                            webViewYT.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                                            if (Build.VERSION.SDK_INT >= 11) {
                                                webViewYT.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                                            }
                                            webViewYT.setVerticalScrollBarEnabled(false);
                                            RecyclerView recyclerView4 = new RecyclerView(getActivity());
                                            setWebviewAddview(q4 , webViewYT,recyclerView4,youb1, youb2,submitbtn4);

                                            break;
                                        default:
                                            Log.e(TAG, "run: Default 3333");
                                            System.out.println("Choice2 selected");
                                            WebView webViewdefault = new WebView(getActivity());
                                            webViewdefault.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            webViewdefault.setLayoutParams(new LinearLayout.LayoutParams(
                                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                                            webViewdefault.setWebViewClient(new WebViewClient()); // set the WebViewClient
                                            webViewdefault.loadUrl(q4DataSource); // Load your desired url
                                            webViewdefault.getSettings().setBuiltInZoomControls(true);
                                            if (Build.VERSION.SDK_INT >= 11) {
                                                webViewdefault.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                                            }
                                            webViewdefault.getSettings().setJavaScriptEnabled(true);
                                            webViewdefault.getSettings().setLoadWithOverviewMode(true);
                                            webViewdefault.getSettings().setUseWideViewPort(true);
                                            webViewdefault.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                                            webViewdefault.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
                                            q4.addView(webViewdefault);
                                            q4.setVisibility(View.VISIBLE);
                                            break;
                                    }
                                    //FragmentManager fm = getSupportFragmentManager();
                                    //EditNameDialogFragment editNameDialogFragment = EditNameDialogFragment.newInstance("Some Title");
                                    //editNameDialogFragment.show(fm, "fragment_edit_name");

                                } else {
                                }
                            }
                            try {
                                EditText targetInformationTextField = (EditText) controls.findViewById(on_click_cloud_tracking_info_field);
                                targetInformationTextField.setText(nameToDisplay);
                                targetInformationTextField.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }else{

                            EditText targetInformationTextField = (EditText) controls.findViewById(on_click_cloud_tracking_info_field);
                            targetInformationTextField.setText("Incorrect Image recognized - Please try again", TextView.BufferType.NORMAL);
                            targetInformationTextField.setVisibility(View.VISIBLE);
                        }

                    }
                });
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            EditText targetInformationTextField = (EditText) controls.findViewById(on_click_cloud_tracking_info_field);
                            targetInformationTextField.setText("Recognition failed - Please try again", TextView.BufferType.NORMAL);
                            targetInformationTextField.setVisibility(View.VISIBLE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onRecognitionInterruption(final CloudTracker cloudTracker_, final double suggestedInterval_) {

    }



    public class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
    public void setQ1Large(RelativeLayout q1, RelativeLayout q2, RelativeLayout q3, RelativeLayout q4, EditText windText,Button submitbtn,TextView completedTV){

        q1.setVisibility(View.VISIBLE);
        q2.setVisibility(View.INVISIBLE);
        q3.setVisibility(View.INVISIBLE);
        q4.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(width, height-40);
        linearlayout.setMargins(0, 60, 0, 0);
        q1.setLayoutParams(linearlayout);

        windText.setText("YouTube");

        loadTripPlacesFromRemote(mSelectedDayObjectId,1,submitbtn,completedTV);
        recognizeButton.setVisibility(View.GONE);
        completedTV.setVisibility(View.VISIBLE);

    }
    public void setQ1Small(RelativeLayout q1, RelativeLayout q2, RelativeLayout q3, RelativeLayout q4,Button submitbtn ,EditText namedisp,String sname){

        q1.setVisibility(View.VISIBLE);
        q2.setVisibility(View.VISIBLE);
        q3.setVisibility(View.VISIBLE);
        q4.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(width/2-15, quadheightsmall-40);
        linearlayout.setMargins(20, 60, 20, 20);
        q1.setLayoutParams(linearlayout);

        clearRecyData(submitbtn);
        namedisp.setText(sname);
        recognizeButton.setVisibility(View.VISIBLE);


    }

    public void setQ2Large(RelativeLayout q1, RelativeLayout q2, RelativeLayout q3, RelativeLayout q4, EditText windText,Button submitbtn){

        q1.setVisibility(View.INVISIBLE);
        q2.setVisibility(View.VISIBLE);
        q3.setVisibility(View.INVISIBLE);
        q4.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(width, height-40);
        linearlayout.setMargins(0, 60, 0, 0);
        q2.setLayoutParams(linearlayout);
        windText.setText("Facebook");

        loadTripPlacesFromRemote(mSelectedDayObjectId,2,submitbtn,completedTV);
        recognizeButton.setVisibility(View.GONE);

    }
    public void setQ2Small(RelativeLayout q1, RelativeLayout q2, RelativeLayout q3, RelativeLayout q4,Button submit ,EditText namedisp,String sname){
        q1.setVisibility(View.VISIBLE);
        q2.setVisibility(View.VISIBLE);
        q3.setVisibility(View.VISIBLE);
        q4.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(width/2, quadheightsmall-40);
        linearlayout.setMargins(width/2+30, 60, 20, 20);
        q2.setLayoutParams(linearlayout);

        namedisp.setText(sname);
        clearRecyData(submit);
        recognizeButton.setVisibility(View.VISIBLE);
    }

    public void setQ3Large(RelativeLayout q1, RelativeLayout q2, RelativeLayout q3, RelativeLayout q4, EditText windText,Button submitbtn){
        q1.setVisibility(View.INVISIBLE);
        q2.setVisibility(View.INVISIBLE);
        q3.setVisibility(View.VISIBLE);
        q4.setVisibility(View.INVISIBLE);
        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(width, height-40);
        linearlayout.setMargins(0, 60, 0, 0);
        q3.setLayoutParams(linearlayout);
        windText.setText("Image");

        loadTripPlacesFromRemote(mSelectedDayObjectId,3,submitbtn,completedTV);
        recognizeButton.setVisibility(View.GONE);
    }


    public void setQ3Small(RelativeLayout q1, RelativeLayout q2, RelativeLayout q3, RelativeLayout q4,Button submit ,EditText namedisp,String sname){
        q1.setVisibility(View.VISIBLE);
        q2.setVisibility(View.VISIBLE);
        q3.setVisibility(View.VISIBLE);
        q4.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(width/2-15, quadheightsmall-40);
        linearlayout.setMargins(20,quadheightsmall+30, 20, 20);
        q3.setLayoutParams(linearlayout);
        namedisp.setText(sname);
        clearRecyData(submit);
        recognizeButton.setVisibility(View.VISIBLE);
    }

    public void setQ4Large(RelativeLayout q1, RelativeLayout q2, RelativeLayout q3, RelativeLayout q4, EditText windText,Button submitbtn){

        q1.setVisibility(View.INVISIBLE);
        q2.setVisibility(View.INVISIBLE);
        q3.setVisibility(View.INVISIBLE);
        q4.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(width, height-40);
        linearlayout.setMargins(0, 60, 0, 0);
        q4.setLayoutParams(linearlayout);
        windText.setText("WebPage");

        loadTripPlacesFromRemote(mSelectedDayObjectId,4,submitbtn,completedTV);
        recognizeButton.setVisibility(View.GONE);
    }
    public void setQ4Small(RelativeLayout q1, RelativeLayout q2, RelativeLayout q3, RelativeLayout q4,Button submit ,EditText namedisp,String sname){

        q1.setVisibility(View.VISIBLE);
        q2.setVisibility(View.VISIBLE);
        q3.setVisibility(View.VISIBLE);
        q4.setVisibility(View.VISIBLE);
        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(width/2-15, quadheightsmall-40);
        linearlayout.setMargins(width/2+30, quadheightsmall+30, 20, 20);
        q4.setLayoutParams(linearlayout);
        namedisp.setText(sname);
        clearRecyData(submit);
        recognizeButton.setVisibility(View.VISIBLE);

    }



    public ImageView setImgB1(ImageView b1){

        b1.setImageDrawable(getResources().getDrawable(R.drawable.expand));

        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(70 , 70);
        linearlayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        b1.setLayoutParams(linearlayout);
        b1.setScaleType(FIT_XY);

        return b1;
    }

    public ImageView setq2q4ImgB1(ImageView b1){

        b1.setImageDrawable(getResources().getDrawable(R.drawable.expand));
        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(70 , 70);
        linearlayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        b1.setLayoutParams(linearlayout);
        b1.setScaleType(FIT_XY);
        return b1;
    }

    public ImageView setImgB2(ImageView b2){

        b2.setImageDrawable(getResources().getDrawable(R.drawable.expand));

        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(70 , 70);
        linearlayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        b2.setLayoutParams(linearlayout);
        b2.setScaleType(FIT_XY);
        b2.setVisibility(View.GONE);
        return b2;
    }

    public void setWebviewAddview(RelativeLayout q2 , WebView webView,RecyclerView recyclerView,ImageView b1,ImageView b2,Button submitbtn){

        LinearLayout l = new LinearLayout(getActivity());
        l.setOrientation(LinearLayout.VERTICAL);
        l.addView(webView);
        l.addView(setRecyView(recyclerView));
        q2.addView(setSubmitButton("0",submitbtn));
        q2.addView(l);
        q2.addView(setq2q4ImgB1(b1));
        q2.addView(setImgB2(b2));
        q2.setVisibility(View.VISIBLE);
        //--Add
    }

    public void setImageAddview(RelativeLayout q2 , ImageView img,RecyclerView recyclerView,ImageView b1,ImageView b2,Button submit){
        LinearLayout l = new LinearLayout(getActivity());
        LinearLayout.LayoutParams linpa = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        l.setLayoutParams(linpa);

        RelativeLayout r = new RelativeLayout(getActivity());

        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , webviewHeight);
        r.setLayoutParams(linearlayout);

        r.addView(quadImg(img));

        l.setOrientation(LinearLayout.VERTICAL);
        l.addView(r);
        l.addView(setRecyView(recyclerView));
        q2.addView(setSubmitButton("0",submit));
        q2.addView(l);
        q2.addView(setq2q4ImgB1(b1));
        q2.addView(setImgB2(b2));
        q2.setVisibility(View.VISIBLE);
    }

    public ImageView quadImg(ImageView ivg){
        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT , ViewGroup.LayoutParams.WRAP_CONTENT);
        linearlayout.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        linearlayout.addRule(RelativeLayout.CENTER_HORIZONTAL);

        ivg.setLayoutParams(linearlayout);
        ivg.setScaleType(FIT_XY);
        return ivg;
    }

    public Button setSubmitButton(String s, final Button button){
        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT);
        linearlayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        linearlayout.addRule(RelativeLayout.CENTER_HORIZONTAL);
        linearlayout.setMargins(10,0,10,50);
        button.setLayoutParams(linearlayout);

        if(s.equals("1")){
            button.setVisibility(View.VISIBLE);
        }else if(s.equals("0")){
            button.setVisibility(View.GONE);
        }



        button.setBackgroundColor(getResources().getColor(R.color.blue));
        button.setTextColor(Color.WHITE);
        button.setText("Submit");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveValuesToParse();
                //Hide Everything......
                mQuestionsList.clear();
                mQuestionsAdapter.notifyDataSetChanged();

                button.setVisibility(View.INVISIBLE);
                //setCompletedText("1",completedTV);


            }
        });
        return button;
    }

    public TextView setCompletedText(String sTV,TextView txtView){
        RelativeLayout.LayoutParams linearlayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT);
        linearlayout.addRule(RelativeLayout.CENTER_HORIZONTAL);
        linearlayout.setMargins(10,0,10,50);
        txtView.setLayoutParams(linearlayout);

        if(sTV.equals("1")){
            txtView.setVisibility(View.VISIBLE);
        }else if(sTV.equals("0")){
            txtView.setVisibility(View.GONE);
        }



        txtView.setBackgroundColor(getResources().getColor(R.color.blue));
        txtView.setTextColor(Color.WHITE);
        txtView.setText("Completed");

        return txtView;
    }


    public void setVideoviewAddview(RelativeLayout q2 , VideoView videoview,RecyclerView recyclerView,ImageView b1,ImageView b2){

        Button submit = new Button(getActivity());

        LinearLayout l = new LinearLayout(getActivity());
        l.setOrientation(LinearLayout.VERTICAL);

        l.addView(videoview);
        l.addView(setRecyView(recyclerView));
        q2.addView(setSubmitButton("0",submit));
        q2.addView(l);
        q2.addView(setq2q4ImgB1(b1));
        q2.addView(setImgB2(b2));
        q2.setVisibility(View.VISIBLE);
    }

    public RecyclerView setRecyView(RecyclerView recy){

        LinearLayout.LayoutParams linearlayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , ViewGroup.LayoutParams.WRAP_CONTENT);
        linearlayout.setMargins(0,0,0,50);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL_LIST);
        LinearLayoutManager layoutManagerPlace = new LinearLayoutManager(getContext());
        layoutManagerPlace.setOrientation(LinearLayoutManager.VERTICAL);
        recy = new RecyclerView(getActivity());

        recy.setLayoutManager(layoutManagerPlace);

        recy.setItemAnimator(new DefaultItemAnimator());
        recy.addItemDecoration(itemDecoration);
        recy.setAdapter(mQuestionsAdapter);
        recy.setLayoutParams(linearlayout);

        return recy;
    }



    private void loadTripPlacesFromRemote(String s,int quadno ,Button submitbtn,TextView completedTV) {

        //Pre-Check if user has already completed this  -If Yes then stop this call

        LeaderBoard tempLeaderBoard = null;
        //First get user
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery<LeaderBoard> query1 = ParseQuery.getQuery(LeaderBoard.class);
        //query1.whereEqualTo("huntID", innerQuery);
        query1.whereEqualTo("levelID",s.toString());
        query1.whereEqualTo("quadrantNo",quadno);
        query1.whereEqualTo("userID", currentUser);
        try {
            tempLeaderBoard = query1.getFirst();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(tempLeaderBoard==null){
            setSubmitButton("1",submitbtn);

            ParseQuery<Day> innerQuery = ParseQuery.getQuery(Day.class);
            innerQuery.whereEqualTo("objectId", s);

            ParseQuery<Questions> query = ParseQuery.getQuery(Questions.class);
            query.whereMatchesQuery("parentId", innerQuery);
            query.whereEqualTo("quadrantNo",quadno);
            query.whereEqualTo("levelID",s.toString());
            query.orderByAscending("questionNo");
            query.findInBackground(new FindCallback<Questions>() {
                @Override
                public void done(List<Questions> questions, ParseException e) {
                    if (e == null) {
                        populateTripPlanPlaces(questions);
                    } else {
                        Log.d("ERROR", "Data not fetched");
                    }
                }
            });
        }else{
            /////Do Nothing
            setCompletedText("1",completedTV);
        }


    }


    public void clearRecyData(Button submitbtn){
        mQuestionsList.clear();
        mQuestionsAdapter.notifyDataSetChanged();

        setSubmitButton("0",submitbtn);

    }

    private void populateTripPlanPlaces(List<Questions> questionses) {


        mQuestionsList.clear();
        mQuestionsList.addAll(questionses);
        mQuestionsAdapter.notifyDataSetChanged();
    }




    public void destroyWebView() {
        try {
            if(webViewYT != null) {
                webViewYT.loadUrl("https://www.google.co.in/");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  static void UpdateSelectedValue(String questionID, String selectedValue){
        selectedValueToSave = "";
        questionIDToSave = "";
        selectedValueToSave = selectedValue;
        questionIDToSave = questionID;
    }
    private void SaveValuesToParse(){
        String[] paramsOptionOne = new String[2];
        paramsOptionOne[0] = questionIDToSave;
        paramsOptionOne[1] = selectedValueToSave;
        new UpdatePointsandLeaderBoard().execute(paramsOptionOne);
        Toast.makeText(getApplicationContext(), "Your answer "+paramsOptionOne[1]+" is saved", Toast.LENGTH_SHORT).show();



    }

}