package com.example.educonnect.api;

import com.example.educonnect.model.Classroom;
import com.example.educonnect.model.LoginRequest;
import com.example.educonnect.model.SchoolYear;
import com.example.educonnect.model.Semester;
import com.example.educonnect.model.Term;
import com.example.educonnect.model.TermRequest;
import com.example.educonnect.model.Report;
import com.example.educonnect.model.Teacher;
import com.example.educonnect.model.ReportBotRequest;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
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
        Call<JsonObject> login(@Body LoginRequest body);

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