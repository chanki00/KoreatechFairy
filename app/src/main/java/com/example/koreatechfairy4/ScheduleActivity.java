package com.example.koreatechfairy4;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.koreatechfairy4.adapter.LectureAdapter;
import com.example.koreatechfairy4.dto.LectureDto;
import com.example.koreatechfairy4.repository.LectureRepository;
import com.example.koreatechfairy4.util.ScheduleCrawler;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {
    private final String year = "2024";
    private final String semester = "1";
    private ActivityResultLauncher<Intent> getContentLauncher;
    private Button lecture_register;
    private LectureRepository repository;
    private RecyclerView recyclerView;
    private LectureAdapter lectureAdapter;
    private List<LectureDto> lectureList;
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

        lecture_register = findViewById(R.id.lecture_register);
        lecture_register.setOnClickListener(v -> openDocument());

        String reference = "KoreatechFairy4/" + "schedule" + year + "/" + semester;

        String userId = getIntent().getStringExtra("userId");
        repository = new LectureRepository(reference);

        getContentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        // 이 URI를 사용하여 파일 내용을 읽습니다.
//                        GradeDto userGrade = LectureCrawler.crawlLecture(getApplicationContext(), uri);
                        repository.remove();
                        List<LectureDto> lectures = ScheduleCrawler.crawlLecture(getApplicationContext(), uri);
//                        repository.save(lectures);
                    }
                });

        recyclerView = findViewById(R.id.schedule_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        lectureList = new ArrayList<>();

    }

    private void openDocument() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*"); // XLSX 파일 타입
        getContentLauncher.launch(intent);
    }
}