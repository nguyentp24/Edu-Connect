package com.example.educonnect.api;

import com.example.educonnect.model.Course;
import com.example.educonnect.model.Parent;
import com.example.educonnect.model.Teacher;
import com.example.educonnect.model.response.LoginResponse;
import com.google.gson.JsonObject;
import com.example.educonnect.model.request.LoginRequest;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

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

        @GET("api/teacher/parent-profile/{studentId}")
        Call<Parent> getParentProfileForTeacher(
                @Header("Authorization") String authToken,
                @Path("studentId") String studentId
        );

        @GET("api/Teacher/{userId}")
        Call<Teacher> getTeacher(@Path("userId") String userId, @Header("Authorization") String bearerToken);

        // Courses by teacherId: GET /api/Course/teacher/{teacherId}
        @GET("api/Course/teacher/{teacherId}")
        Call<java.util.List<Course>> getCoursesByTeacher(@Path("teacherId") String teacherId, @Header("Authorization") String bearerToken);
    }
}


