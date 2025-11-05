package com.example.educonnect.api;

import com.example.educonnect.model.Classroom;
import com.example.educonnect.model.SchoolYear;
import com.example.educonnect.model.Semester;
import com.example.educonnect.model.Term;
import com.example.educonnect.model.TermRequest;
import com.example.educonnect.model.Report;
import com.example.educonnect.model.ReportBotRequest;

import com.google.gson.annotations.SerializedName;

import com.example.educonnect.model.Parent;
import com.example.educonnect.model.Teacher;
import com.example.educonnect.model.Course;
import com.example.educonnect.model.ClassroomStudent;
import com.example.educonnect.model.AttendanceItem;

import com.example.educonnect.model.request.CourseStatusRequest;
import com.example.educonnect.model.request.LoginRequest;
import com.example.educonnect.model.response.LoginResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public final class ApiClient {

    private static final String BASE_URL = "https://swd-backend-web-t2vw.onrender.com/";
    private static Retrofit retrofit;
    private ApiClient() {}

    public static synchronized Retrofit retrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static class ReportCreateRequest {
        @SerializedName("teacherId")
        private String teacherId;

        @SerializedName("title")
        private String title;

        @SerializedName("description")
        private String description;

        @SerializedName("classId")
        private String classId;

        @SerializedName("termID")
        private String termID;

        @SerializedName("teacherName")
        private String teacherName;

        @SerializedName("className")
        private String className;

        // Constructor
        public ReportCreateRequest(String teacherId, String title, String description, String classId, String termID, String teacherName, String className) {
            this.teacherId = teacherId;
            this.title = title;
            this.description = description;
            this.classId = classId;
            this.termID = termID;
            this.teacherName = teacherName;
            this.className = className;
        }
    }

    public static ApiService service() {
        return retrofit().create(ApiService.class);
    }

    public interface ApiService {
        @POST("api/Auth/Login")
        Call<LoginResponse> login(@Body LoginRequest body);

        @GET("api/Report")
        Call<List<Report>> getReportHistory(
                @Query("classId") String classId,
                @Header("Authorization") String authToken,
                @Query("t") long timestamp
        );

        @POST("api/report/generate")
        Call<String> generateReportDetails(
                @Body ReportBotRequest body,
                @Header("Authorization") String authToken
        );

        @POST("api/Report")
        Call<Report> createNewReport(
                @Body ReportCreateRequest reportRequest,
                @Header("Authorization") String authToken
        );

        @POST("api/Term")
        Call<Term> createTerm(
                @Body TermRequest termRequest,
                @Header("Authorization") String authToken
        );

        @GET("api/teacher/parent-profile/{studentId}")
        Call<Parent> getParentProfileForTeacher(
                @Header("Authorization") String authToken,
                @Path("studentId") String studentId
        );

        // Teacher detail by userId: GET /api/Teacher/{userId}
        @GET("api/Teacher/{userId}")
        Call<Teacher> getTeacher(@Path("userId") String userId, @Header("Authorization") String bearerToken);

        // Courses by teacherId: GET /api/Course/teacher/{teacherId}
        @GET("api/Course/teacher/{teacherId}")
        Call<java.util.List<Course>> getCoursesByTeacher(@Path("teacherId") String teacherId, @Header("Authorization") String bearerToken);

        // Classroom students by classId: GET /api/Classroom/{classId}/students
        @GET("api/Classroom/{classId}/students")
        Call<java.util.List<ClassroomStudent>> getClassroomStudents(@Path("classId") String classId, @Header("Authorization") String bearerToken);

        // Attendance by courseId: GET /api/Attendance/course/{courseId}
        @GET("api/Attendance/course/{courseId}")
        Call<java.util.List<AttendanceItem>> getAttendanceByCourse(@Path("courseId") String courseId, @Header("Authorization") String bearerToken);

        // Create/Update attendance: POST /api/Attendance
        @POST("api/Attendance")
        Call<ResponseBody> saveAttendance(@Body java.util.List<AttendanceItem> attendanceList, @Header("Authorization") String bearerToken);

        // Update course status: PUT /api/Course/status
        @PUT("api/Course/status")
        Call<ResponseBody> updateCourseStatus(@Body CourseStatusRequest request, @Header("Authorization") String bearerToken);

        // Delete attendance by courseId: DELETE /api/Attendance/course/{courseId}
        @DELETE("api/Attendance/course/{courseId}")
        Call<ResponseBody> deleteAttendanceByCourse(@Path("courseId") String courseId, @Header("Authorization") String bearerToken);

        // Lấy danh sách khóa học theo classId
        @GET("api/Course/class/{classId}")
        Call<List<Course>> getCourses(@Path("classId") String classId, @Header("Authorization") String bearerToken);

        // Lấy danh sách điểm danh theo classId: GET /api/Attendance/class/{classId}
        @GET("api/Attendance/class/{classId}")
        Call<List<AttendanceItem>> getAttendanceByClass(@Path("classId") String classId, @Header("Authorization") String bearerToken);

        // Lấy danh sách lớp học theo teacherId: GET /api/Classroom?teacherId={teacherId}
//        @GET("api/Classroom")
//        Call<java.util.List<Class>> getClasses(@Query("teacherId") String teacherId, @Header("Authorization") String bearerToken);
//
        @GET("api/Term/{TermId}")
        Call<Term> getTermDetails(
                @Path("TermId") String termId,
                @Header("Authorization") String authToken
        );

        @GET("api/Classroom")
        Call<List<Classroom>> getClassroomByTeacherId(
                @Query("teacherId") String teacherId,
                @Header("Authorization") String authToken
        );

        @GET("api/Classroom")
        Call<Classroom> getClassroom(
                @Query("classId") String classId,
                @Header("Authorization") String authToken
        );

        @GET("api/SchoolYear/{id}")
        Call<SchoolYear> getSchoolYear(
                @Path("id") String schoolYearId,
                @Header("Authorization") String authToken
        );

        @GET("api/Semester")
        Call<List<Semester>> getSemesters(
                @Query("schoolYearId") String schoolYearId,
                @Header("Authorization")String authToken
        );

        @GET("api/Teacher/{userId}")
        Call<Teacher> getTeacherInfo(
                @Path("userId") String userId,
                @Header("Authorization") String authToken
        );
    }
}