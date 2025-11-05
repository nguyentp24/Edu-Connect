package com.example.educonnect.api;

import com.example.educonnect.model.Class;
import com.example.educonnect.model.Student;
import com.example.educonnect.model.Teacher;
import com.example.educonnect.model.Course;
import com.example.educonnect.model.ClassroomStudent;
import com.example.educonnect.model.AttendanceItem;
import com.example.educonnect.model.request.LoginRequest;
import com.example.educonnect.model.response.LoginResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Cấu hình Retrofit tối thiểu cho gọi API.
 * - Không phụ thuộc thừa. Không thêm OkHttp logging hay interceptor lúc này.
 * - Dùng GsonConverter để parse JSON.
 * - Sử dụng BASE_URL bạn đã cung cấp.
 */
public final class ApiClient {

    private static final String BASE_URL = "https://swd-backend-web-t2vw.onrender.com/";

    private static Retrofit retrofit;

    private ApiClient() {}

    public static synchronized Retrofit retrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService service() {
        return retrofit().create(ApiService.class);
    }
    public interface ApiService {
        // Login: POST /api/Auth/Login
        @POST("api/Auth/Login")
        Call<LoginResponse> login(@Body LoginRequest body);

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

        // Lấy danh sách khóa học theo classId
        @GET("api/Course/class/{classId}")
        Call<List<Course>> getCourses(@Path("classId") String classId, @Header("Authorization") String bearerToken);

        // Lấy danh sách điểm danh theo classId: GET /api/Attendance/class/{classId}
        @GET("api/Attendance/class/{classId}")
        Call<List<AttendanceItem>> getAttendanceByClass(@Path("classId") String classId, @Header("Authorization") String bearerToken);

        // Lấy danh sách lớp học theo teacherId: GET /api/Classroom?teacherId={teacherId}
        @GET("api/Classroom")
        Call<List<Class>> getClasses(@Query("teacherId") String teacherId, @Header("Authorization") String bearerToken);

    }
}


