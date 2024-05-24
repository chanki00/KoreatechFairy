package com.example.koreatechfairy4;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.koreatechfairy4.adapter.LectureAdapter;
import com.example.koreatechfairy4.adapter.SearchLectureAdapter;
import com.example.koreatechfairy4.dto.LectureDto;
import com.example.koreatechfairy4.repository.LectureRepository;
import com.example.koreatechfairy4.util.DayAndTimes;
import com.example.koreatechfairy4.util.MyScheduleList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScheduleActivity extends AppCompatActivity {
    private String[][] timeTab = {
            {"01A", "0900"},
            {"01B", "0930"},
            {"02A", "1000"},
            {"02B", "1030"},
            {"03A", "1100"},
            {"03B", "1130"},
            {"04A", "1200"},
            {"04B", "1230"},
            {"05A", "1300"},
            {"05B", "1330"},
            {"06A", "1400"},
            {"06B", "1430"},
            {"07A", "1500"},
            {"07B", "1530"},
            {"08A", "1600"},
            {"08B", "1630"},
            {"09A", "1700"},
            {"09B", "1730"},
    };
    private final String year = "2024";
    private final String semester = "1";
    private ActivityResultLauncher<Intent> getContentLauncher;
    private Button lecture_register, my_page_button;
    private ImageButton schedule_back;
    private LectureRepository repository;
    private RecyclerView recyclerView;
    private RecyclerView myScheduleRecyclerView;
    private SearchLectureAdapter searchLectureAdapter;
    private LectureAdapter lectureAdapter;
    private List<LectureDto> lectureList;
    private List<LectureDto> myScheduleList = new ArrayList<>();
    private MyScheduleList myScheduleManager = MyScheduleList.getInstance();
    private TextView scheduleTextView;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        lecture_register = findViewById(R.id.lecture_register);
//        lecture_register.setOnClickListener(v -> openDocument());


        //상단 툴바 시작
        schedule_back = findViewById(R.id.imgBtn_schedule_back);
        schedule_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        my_page_button = findViewById(R.id.btn_schedule_mypage);
        my_page_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScheduleActivity.this, MyPageActivity.class);
                startActivity(intent);
            }
        });
        //상단 툴바 끝

        // 검색 입력 처리
        EditText searchLecture = findViewById(R.id.search_lecture);
        searchLecture.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchLectureAdapter.filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        String reference = "KoreatechFairy4/" + "schedule" + "/" + year + "/" + semester;

        String userId = getIntent().getStringExtra("userId");
        repository = new LectureRepository(reference);

/*        getContentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        // 이 URI를 사용하여 파일 내용을 읽습니다.
//                        GradeDto userGrade = LectureCrawler.crawlLecture(getApplicationContext(), uri);
                        repository.remove();
                        List<LectureDto> lectures = ScheduleCrawler.crawlLecture(getApplicationContext(), uri);
                        repository.save(lectures);
                    }
                });*/

        recyclerView = findViewById(R.id.schedule_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // DividerItemDecoration 추가
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        // 두 번째 RecyclerView 설정
        myScheduleRecyclerView = findViewById(R.id.my_schedule_list);
        myScheduleRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        lectureAdapter = new LectureAdapter(myScheduleList, lecture -> {
            // 여기다가 클릭했을 때 반응 MyScheduleList에 있는 거 없애고 myScheduleList에 있는 객체 없애고 recyclerView한테 말해주고
            String time = lecture.getTime();
            List<DayAndTimes> dayAndTimes = changeToDayAndTimes(time);
            myScheduleList.remove(lecture);
            myScheduleManager.removeLecture(lecture);
            myScheduleManager.removeTime(dayAndTimes);
            for (DayAndTimes dat : dayAndTimes) {
                String day = dat.getDays();
                String scheduleDay = day + "_";

                for (String t : dat.getTimeList()) {
                    String scheduleId = scheduleDay + t;
                    int resId = getResources().getIdentifier(scheduleId, "id", getPackageName());
                    scheduleTextView = findViewById(resId);
                    scheduleTextView.setText(null);
                    scheduleTextView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                }
            }
            lectureAdapter.notifyDataSetChanged();
        });
        myScheduleRecyclerView.setAdapter(lectureAdapter);

        repository.getLectureDtoList(new LectureRepository.DataCallback() {
            @Override
            public void onCallback(List<LectureDto> lectureList) {
                searchLectureAdapter = new SearchLectureAdapter(lectureList, lecture -> {
                    //객체를 클릭했을 시에 반응
                    // 이름 겹치는 경우
                    if (myScheduleManager.isDuplicateName(lecture.getName())) {
                        // 중복 메시지 출력
                    } else {
                        // time을 변환시키는 과정 필요
                        String time = lecture.getTime();
                        if (!time.isEmpty()) {
                            List<DayAndTimes> dayAndTimes = changeToDayAndTimes(time);

                            // dayAndTime내부에 것들 비교하면서 시간 중복 체크
                            if (myScheduleManager.isDuplicateTime(dayAndTimes)) {
                                // 시간 중복 메시지 출력
                            } else {
                                int minColorValue = 128;
                                int red = random.nextInt(128) + minColorValue;
                                int green = random.nextInt(128) + minColorValue;
                                int blue = random.nextInt(128) + minColorValue;
                                // 다 되는 경우
                                myScheduleManager.addLecture(lecture);
                                for (DayAndTimes dat : dayAndTimes) {
                                    String day = dat.getDays();
                                    String scheduleDay = day + "_";
                                    int randomColor = Color.rgb(red, green, blue);

                                    int textViewSize = dat.getTimeList().size();

                                    String lectureName = lecture.getName();
                                    String lectureClasses = lecture.getClasses();

                                    String resultName = lectureName + "  " + lectureClasses;
                                    String abbreviationName = "";

                                    if(lectureName.charAt(0) >= 'A' && lectureName.charAt(0) <= 'Z') {
                                        abbreviationName += lectureName.substring(0, 3);
                                    }
                                    else {
                                        abbreviationName += lectureName.substring(0, 2);
                                    }

                                    abbreviationName += " " + lectureClasses;

                                    List<String> splitResultName = splitStringByLength(resultName, 4);
                                    Log.d("splitSize", String.valueOf(splitResultName.size()));

                                    if(textViewSize >= splitResultName.size()) {
                                        for(int i = 0; i < splitResultName.size(); ++i) {
                                            String t = dat.getTimeList().get(i);
                                            String scheduleId = scheduleDay + t;
                                            int resId = getResources().getIdentifier(scheduleId, "id", getPackageName());
                                            setTextWithId(resId, splitResultName.get(i));
                                        }
                                    } // 전부 다 수용 되는 경우
                                    else {
                                        if(dat.getTimeList().size() == 1) {
                                            String t = dat.getTimeList().get(0);
                                            String scheduleId = scheduleDay + t;
                                            int resId = getResources().getIdentifier(scheduleId, "id", getPackageName());
                                            setTextWithId(resId, abbreviationName);
                                        }
                                        else {
                                            String t = dat.getTimeList().get(0);
                                            String scheduleId = scheduleDay + t;
                                            int resId = getResources().getIdentifier(scheduleId, "id", getPackageName());
                                            setTextWithId(resId, abbreviationName.substring(0, 4));

                                            t = dat.getTimeList().get(1);
                                            scheduleId = scheduleDay + t;
                                            resId = getResources().getIdentifier(scheduleId, "id", getPackageName());
                                            setTextWithId(resId, abbreviationName.substring(4, abbreviationName.length()));
                                        }
                                    }

                                    for (String t : dat.getTimeList()) {
                                        myScheduleManager.addTime(day, t);
                                        String scheduleId = scheduleDay + t;
                                        int resId = getResources().getIdentifier(scheduleId, "id", getPackageName());
                                        scheduleTextView = findViewById(resId);
                                        scheduleTextView.setBackgroundColor(randomColor);
                                    }
                                }
                                //2번 째 RecyclerView
                                myScheduleList.add(lecture);
                                lectureAdapter.notifyDataSetChanged();
                            }

                        } else { // 시간이 비어있음
                            // 시간이 없다 메시지 출력
                        }
                    }
                });
                recyclerView.setAdapter(searchLectureAdapter);
            }
        });
    }

    private void setTextWithId(int resId, String text) {
        scheduleTextView = findViewById(resId);
        scheduleTextView.setText(text);
        scheduleTextView.setTextSize(13f);
    }

    private void openDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // XLSX 파일 타입
        getContentLauncher.launch(intent);
    }

    private List<DayAndTimes> changeToDayAndTimes(String time) {
        String days = String.valueOf(time.charAt(0)); // 첫 요일
        String[] splitLectureTime = time.split(",");

        List<DayAndTimes> dayAndTimes = new ArrayList<>();

        for (String lectureTime : splitLectureTime) {
            if (!(lectureTime.charAt(0) >= '0' && lectureTime.charAt(0) <= '9')) {
                days = String.valueOf(lectureTime.charAt(0));
            }
            List<String> timeList = new ArrayList<>();

            // ~로 나눠서 시간 추출
            String[] splitTilde = lectureTime.split("~");
            int firstLength = splitTilde[0].length();
            String firstTime = splitTilde[0].substring(firstLength - 3, firstLength);
            String secondTime = splitTilde[1].substring(0, 3);

            boolean isChecked = false;
            for (int i = 0; i < timeTab.length; ++i) {
                if (firstTime.equals(timeTab[i][0])) {
                    isChecked = true;
                    for (int j = i; j < timeTab.length; ++j) {
                        timeList.add(timeTab[j][1]);
                        if (secondTime.equals(timeTab[j][0])) break;
                    }
                }
                if (isChecked) break;
            }
            if (!isChecked) {
                timeList.add("1800");
            }

            dayAndTimes.add(new DayAndTimes(days, timeList));
        }

        return dayAndTimes;
    }

    private static List<String> splitStringByLength(String input, int length) {
        List<String> parts = new ArrayList<>();

        for (int i = 0; i < input.length(); i += length) {
            parts.add(input.substring(i, Math.min(i + length, input.length())));
        }

        return parts;
    }
}