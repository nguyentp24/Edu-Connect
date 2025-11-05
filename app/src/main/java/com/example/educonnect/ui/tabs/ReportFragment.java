package com.example.educonnect.ui.tabs;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.educonnect.R;
import com.example.educonnect.api.ApiClient;
import com.example.educonnect.api.ApiClient.ReportCreateRequest;
import com.example.educonnect.databinding.FragmentReportBinding;
import com.example.educonnect.model.Classroom;
import com.example.educonnect.model.Report;
import com.example.educonnect.model.SchoolYear;
import com.example.educonnect.model.Semester;
import com.example.educonnect.model.Term;
import com.example.educonnect.model.TermRequest;
import com.example.educonnect.ui.reports.ReportHistoryActivity;
import com.google.android.material.card.MaterialCardView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportFragment extends Fragment {
    private FragmentReportBinding vb;
    private MaterialCardView selectedCard = null;
    private List<MaterialCardView> allCards = new ArrayList<>();
    private List<SchoolYear> academicYears = new ArrayList<>();
    private List<Semester> semesters = new ArrayList<>();
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar selectedMonth = Calendar.getInstance();
    private Semester selectedSemester = null;
    private SchoolYear selectedYear = null;
    private String authToken;
    private String classId;

    private SimpleDateFormat isoFormat;
    private SimpleDateFormat dateOnlyFormat;

    private int selectedCardBgColor;
    private int unselectedCardBgColor;
    private int selectedIconTextColor;
    private int unselectedIconTextColor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        vb = FragmentReportBinding.inflate(inflater, container, false);
        return vb.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = getContext();
        if (context == null) return;

        isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        dateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateOnlyFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        SharedPreferences prefs = context.getSharedPreferences("EduConnectApp", Context.MODE_PRIVATE);
        authToken = prefs.getString("token", null);
        classId = prefs.getString("classId", null);

        if (authToken == null || classId == null) {
            Toast.makeText(context, "Lỗi: Phiên đăng nhập hết hạn.", Toast.LENGTH_LONG).show();
            return;
        }

        selectedCardBgColor = ContextCompat.getColor(context, R.color.card_selected_bg);
        unselectedCardBgColor = ContextCompat.getColor(context, R.color.card_unselected_bg);
        selectedIconTextColor = ContextCompat.getColor(context, R.color.card_selected_icon_text);
        unselectedIconTextColor = ContextCompat.getColor(context, R.color.card_unselected_icon_text);
        allCards.add(vb.cardWeek);
        allCards.add(vb.cardMonth);
        allCards.add(vb.cardSemester);
        allCards.add(vb.cardYear);
        hideAllSelectionLayouts();
        resetAllCardsState();
        vb.cardWeek.setOnClickListener(v -> handleCardSelection(vb.cardWeek, vb.tilWeek, vb.iconWeek, vb.labelWeek));
        vb.cardMonth.setOnClickListener(v -> handleCardSelection(vb.cardMonth, vb.tilMonth, vb.iconMonth, vb.labelMonth));
        vb.cardSemester.setOnClickListener(v -> handleCardSelection(vb.cardSemester, vb.tilSemester, vb.iconSemester, vb.labelSemester));
        vb.cardYear.setOnClickListener(v -> handleCardSelection(vb.cardYear, vb.tilYear, vb.iconYear, vb.labelYear));
        vb.edtWeek.setOnClickListener(v -> showDatePicker(vb.edtWeek, "week"));
        vb.edtMonth.setOnClickListener(v -> showDatePicker(vb.edtMonth, "month"));
        vb.btnCreateReport.setOnClickListener(v -> showConfirmationDialog());
        vb.btnReportHistory.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), ReportHistoryActivity.class));
            }
        });
        fetchDropdownData();
    }

    private void fetchDropdownData() {
        if (classId == null) {
            Toast.makeText(getContext(), "Không tìm thấy Class ID", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.service().getClassroom(classId, authToken).enqueue(new Callback<Classroom>() {
            @Override
            public void onResponse(Call<Classroom> call, Response<Classroom> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String schoolYearId = response.body().getSchoolYearId();
                    if (schoolYearId == null || schoolYearId.isEmpty()) {
                        Toast.makeText(getContext(), "Lỗi: Classroom trả về không có schoolYearId", Toast.LENGTH_LONG).show();
                        return;
                    }
                    fetchSchoolYear(schoolYearId);
                } else {
                    Toast.makeText(getContext(), "Lỗi tải Classroom: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Classroom> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng (Classroom): " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchSchoolYear(String schoolYearId) {
        ApiClient.service().getSchoolYear(schoolYearId, authToken).enqueue(new Callback<SchoolYear>() {
            @Override
            public void onResponse(Call<SchoolYear> call, Response<SchoolYear> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SchoolYear sy = response.body();

                    String startYear = parseYear(sy.getStartDate());
                    String endYear = parseYear(sy.getEndDate());

                    if (startYear.isEmpty() || endYear.isEmpty()) {
                        Toast.makeText(getContext(), "Lỗi: SchoolYear có ngày tháng bị null", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String name = "Năm học " + startYear + "-" + endYear;
                    sy.setLabel(name);
                    sy.setValue(startYear + "-" + endYear);

                    academicYears.clear();
                    academicYears.add(sy);
                    List<String> yearLabels = new ArrayList<>();
                    for(SchoolYear s : academicYears) yearLabels.add(s.getLabel());
                    ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, yearLabels);
                    vb.edtYear.setAdapter(yearAdapter);
                    if ("Actived".equals(sy.getStatus())) {
                        selectedYear = sy;
                        vb.edtYear.setText(sy.getLabel(), false);
                    }

                    fetchSemesters(schoolYearId);

                } else {
                    Toast.makeText(getContext(), "Lỗi tải SchoolYear: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<SchoolYear> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng (SchoolYear): " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void fetchSemesters(String schoolYearId) {
        ApiClient.service().getSemesters(schoolYearId, authToken).enqueue(new Callback<List<Semester>>() {
            @Override
            public void onResponse(Call<List<Semester>> call, Response<List<Semester>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isEmpty()) {
                        Toast.makeText(getContext(), "Lỗi: Không tìm thấy Semester nào với schoolYearId=" + schoolYearId, Toast.LENGTH_LONG).show();
                        return;
                    }
                    semesters.clear();
                    semesters.addAll(response.body());
                    if (academicYears.isEmpty() || academicYears.get(0).getLabel() == null) {
                        Toast.makeText(getContext(), "Lỗi: Tải Semester trước khi tải SchoolYear", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String academicYearName = " - " + academicYears.get(0).getLabel();
                    List<String> semesterLabels = new ArrayList<>();
                    for (Semester s : semesters) {
                        String displayName = s.getSemesterName() + academicYearName;
                        s.setDisplayName(displayName);
                        semesterLabels.add(displayName);
                        if ("Actived".equals(s.getStatus())) {
                            selectedSemester = s;
                            vb.edtSemester.setText(displayName, false);
                        }
                    }
                    ArrayAdapter<String> semesterAdapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_item, semesterLabels);
                    vb.edtSemester.setAdapter(semesterAdapter);
                    vb.edtSemester.setOnItemClickListener((parent, v, position, id) -> selectedSemester = semesters.get(position));
                    vb.edtYear.setOnItemClickListener((parent, v, position, id) -> selectedYear = academicYears.get(position));
                } else {
                    Toast.makeText(getContext(), "Lỗi tải Semesters: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<List<Semester>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng (Semesters): " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showConfirmationDialog() {
        if (selectedCard == null) {
            Toast.makeText(getContext(), "Vui lòng chọn loại báo cáo", Toast.LENGTH_SHORT).show();
            return;
        }
        String previewText = getReportPreview();
        if (previewText == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận tạo báo cáo")
                .setMessage("Bạn có muốn tạo " + previewText + "?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Tạo báo cáo", (dialog, which) -> {
                    generateReport();
                })
                .show();
    }

    private String getReportPreview() {
        if (selectedCard == null) return null;
        int selectedCardId = selectedCard.getId();
        if (selectedCardId == R.id.card_week) {
            return "Báo cáo tuần từ " + formatDisplayDate(getWeekStart(selectedDate)) + " đến " + formatDisplayDate(getWeekEnd(selectedDate));
        } else if (selectedCardId == R.id.card_month) {
            return "Báo cáo " + new SimpleDateFormat("'tháng' M, yyyy", new Locale("vi", "VN")).format(selectedMonth.getTime());
        } else if (selectedCardId == R.id.card_semester) {
            if (selectedSemester == null) {
                Toast.makeText(getContext(), "Vui lòng chọn học kỳ", Toast.LENGTH_SHORT).show();
                return null;
            }
            return "Báo cáo " + selectedSemester.getDisplayName();
        } else if (selectedCardId == R.id.card_year) {
            if (selectedYear == null) {
                Toast.makeText(getContext(), "Vui lòng chọn năm học", Toast.LENGTH_SHORT).show();
                return null;
            }
            return "Báo cáo " + selectedYear.getLabel();
        }
        return null;
    }

    private void generateReport() {
        if (selectedCard == null || classId == null) {
            Toast.makeText(getContext(), "Vui lòng chọn loại báo cáo và đảm bảo đã đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String reportTitle = getReportPreview();
        String reportDesc = "Báo cáo tự động cho lớp " + classId;

        SharedPreferences prefs = getContext().getSharedPreferences("EduConnectApp", Context.MODE_PRIVATE);
        String teacherId = prefs.getString("teacherId", null);
        String teacherName = prefs.getString("teacherName", null);
        String className = prefs.getString("className", null);

        if (teacherId == null || teacherName == null || className == null) {
            Toast.makeText(getContext(), "Lỗi: Thiếu thông tin teacher/class. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        String startTime = null;
        String endTime = null;
        String mode = null;

        try {
            int selectedCardId = selectedCard.getId();
            if (selectedCardId == R.id.card_week) {
                startTime = isoFormat.format(getWeekStart(selectedDate));
                endTime = isoFormat.format(getWeekEnd(selectedDate));
                mode = "Tuần";
            } else if (selectedCardId == R.id.card_month) {
                startTime = isoFormat.format(getMonthStart(selectedMonth));
                endTime = isoFormat.format(getMonthEnd(selectedMonth));
                mode = "Tháng";
            } else if (selectedCardId == R.id.card_semester) {
                if (selectedSemester == null) return;
                startTime = selectedSemester.getStartDate();
                endTime = selectedSemester.getEndDate();
                mode = "Kì";
            } else if (selectedCardId == R.id.card_year) {
                if (selectedYear == null) return;
                startTime = selectedYear.getStartDate();
                endTime = selectedYear.getEndDate();
                mode = "Năm";
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Lỗi định dạng ngày", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startTime == null || endTime == null || mode == null) {
            Toast.makeText(getContext(), "Lỗi: Không thể xác định thời gian báo cáo", Toast.LENGTH_SHORT).show();
            return;
        }

        TermRequest termRequest = new TermRequest(startTime, endTime, mode);
        Toast.makeText(getContext(), "Đang tạo báo cáo...", Toast.LENGTH_SHORT).show();

        ApiClient.service().createTerm(termRequest, authToken).enqueue(new Callback<Term>() {
            @Override
            public void onResponse(Call<Term> call, Response<Term> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String termId = response.body().getTermId();
                    saveReportToDatabase(termId, reportTitle, reportDesc, teacherId, teacherName, className);
                } else {
                    Toast.makeText(getContext(), "Tạo báo cáo thất bại: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<Term> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveReportToDatabase(String termId, String title, String description, String teacherId, String teacherName, String className) {
        if (classId == null || termId == null) {
            Toast.makeText(getContext(), "Lỗi: Thiếu classId hoặc termId để tạo báo cáo", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("ReportFragment", "Đang chạy...");
        Toast.makeText(getContext(), "Đang lưu báo cáo...", Toast.LENGTH_SHORT).show();

        ReportCreateRequest reportRequest = new ReportCreateRequest(
                teacherId,
                title,
                description,
                classId,
                termId,
                teacherName,
                className
        );

        ApiClient.service().createNewReport(reportRequest, authToken).enqueue(new Callback<Report>() {
            @Override
            public void onResponse(Call<Report> call, Response<Report> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Tạo báo cáo thành công!", Toast.LENGTH_LONG).show();
                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), ReportHistoryActivity.class);
                        intent.putExtra("JUST_CREATED", true);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(getContext(), "Lưu báo cáo thất bại: " + response.code() + " - " + response.message(), Toast.LENGTH_LONG).show();
                    Log.e("ReportFragment", "Lỗi lưu Report: " + response.code() + " - " + response.message());
                }
            }
            @Override
            public void onFailure(Call<Report> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("ReportFragment", "Lỗi mạng", t);
            }
        });
    }

    private String parseYear(String apiDate) {
        if (apiDate == null) return "";
        try {
            Date date = dateOnlyFormat.parse(apiDate);
            return new SimpleDateFormat("yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            try {
                Date date = isoFormat.parse(apiDate);
                return new SimpleDateFormat("yyyy", Locale.getDefault()).format(date);
            } catch (ParseException e2) {
                Log.e("ReportFragment", "Không thể parse ngày: " + apiDate, e2);
                return "";
            }
        }
    }

    private String formatDisplayDate(Date date) {
        return new SimpleDateFormat("d 'thg' M", new Locale("vi", "VN")).format(date);
    }

    private Calendar getUtcCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    private Date getWeekStart(Calendar cal) {
        Calendar start = (Calendar) cal.clone();
        start.setTimeZone(TimeZone.getTimeZone("UTC"));
        int day = start.get(Calendar.DAY_OF_WEEK);
        int diff = start.get(Calendar.DAY_OF_MONTH) - day + (day == Calendar.SUNDAY ? -6 : Calendar.MONDAY);
        start.set(Calendar.DAY_OF_MONTH, diff);
        start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0); start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0);
        return start.getTime();
    }

    private Date getWeekEnd(Calendar cal) {
        Calendar end = getUtcCalendar();
        end.setTime(getWeekStart(cal));
        end.add(Calendar.DAY_OF_MONTH, 6);
        end.set(Calendar.HOUR_OF_DAY, 23); end.set(Calendar.MINUTE, 59); end.set(Calendar.SECOND, 59); end.set(Calendar.MILLISECOND, 999);
        return end.getTime();
    }

    private Date getMonthStart(Calendar cal) {
        Calendar start = (Calendar) cal.clone();
        start.setTimeZone(TimeZone.getTimeZone("UTC"));
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0); start.set(Calendar.MINUTE, 0); start.set(Calendar.SECOND, 0); start.set(Calendar.MILLISECOND, 0);
        return start.getTime();
    }

    private Date getMonthEnd(Calendar cal) {
        Calendar end = getUtcCalendar();
        end.setTime(getMonthStart(cal));
        end.add(Calendar.MONTH, 1);
        end.add(Calendar.DAY_OF_MONTH, -1);
        end.set(Calendar.HOUR_OF_DAY, 23); end.set(Calendar.MINUTE, 59); end.set(Calendar.SECOND, 59); end.set(Calendar.MILLISECOND, 999);
        return end.getTime();
    }

    private void hideAllSelectionLayouts() {
        vb.tilWeek.setVisibility(View.GONE);
        vb.tilMonth.setVisibility(View.GONE);
        vb.tilSemester.setVisibility(View.GONE);
        vb.tilYear.setVisibility(View.GONE);
    }

    private void resetAllCardsState() {
        for (MaterialCardView card : allCards) {
            card.setCardBackgroundColor(unselectedCardBgColor);
            ImageView icon = null; TextView label = null;
            if (card.getId() == R.id.card_week) { icon = vb.iconWeek; label = vb.labelWeek; }
            else if (card.getId() == R.id.card_month) { icon = vb.iconMonth; label = vb.labelMonth; }
            else if (card.getId() == R.id.card_semester) { icon = vb.iconSemester; label = vb.labelSemester; }
            else if (card.getId() == R.id.card_year) { icon = vb.iconYear; label = vb.labelYear; }
            if (icon != null) icon.setColorFilter(unselectedIconTextColor);
            if (label != null) label.setTextColor(unselectedIconTextColor);
        }
        selectedCard = null;
    }

    private void handleCardSelection(MaterialCardView clickedCard, View selectionViewToShow, ImageView iconView, TextView labelView) {
        resetAllCardsState();
        selectedCard = clickedCard;
        selectedCard.setCardBackgroundColor(selectedCardBgColor);
        if (iconView != null) iconView.setColorFilter(selectedIconTextColor);
        if (labelView != null) labelView.setTextColor(selectedIconTextColor);
        hideAllSelectionLayouts();
        if (selectionViewToShow != null) {
            selectionViewToShow.setVisibility(View.VISIBLE);
        }
    }

    private void showDatePicker(com.google.android.material.textfield.TextInputEditText editText, String mode) {
        Calendar cal = (mode.equals("week")) ? selectedDate : selectedMonth;
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            if (mode.equals("week")) {
                vb.edtWeek.setText(formatDisplayDate(getWeekStart(cal)));
            } else {
                vb.edtMonth.setText(new SimpleDateFormat("'tháng' M, yyyy", new Locale("vi", "VN")).format(cal.getTime()));
            }
        };
        new DatePickerDialog(getContext(), dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }
}