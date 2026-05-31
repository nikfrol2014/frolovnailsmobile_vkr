package com.example.frolovnails.network;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;
import com.example.frolovnails.network.models.response.*;
//import com.example.frolovnails.network.models.response.*;
import com.example.frolovnails.network.models.request.*;
import com.example.frolovnails.network.models.response.stats.DashboardStatsResponse;

public interface ApiService {

    // ========== Аутентификация ==========
    @POST("/api/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @POST("/api/auth/register")
    Call<ApiResponse<AuthResponse>> register(@Body RegisterRequest request);

    // ========== Услуги ==========
    @GET("/api/services")
    Call<ApiResponse<List<Service>>> getServices();

    @GET("/api/services/{id}")
    Call<ApiResponse<Service>> getServiceById(@Path("id") Long id);

    @GET("/api/services/categories")
    Call<ApiResponse<List<String>>> getCategories();

    // ========== Расписание (клиент) ==========
    @GET("/api/schedule/available-days")
    Call<ApiResponse<List<AvailableDay>>> getAvailableDays(@Query("daysCount") int daysCount);

    @GET("/api/schedule/availability")
    Call<ApiResponse<AvailableSlotsResponse>> getAvailableSlots(
            @Query("date") String date,
            @Query("serviceId") Long serviceId
    );

    // ========== Записи (клиент) ==========
    @POST("/api/appointments/client")
    Call<ApiResponse<Appointment>> createClientAppointment(@Body CreateAppointmentRequest request);

    @GET("/api/appointments/my")
    Call<ApiResponse<List<Appointment>>> getMyAppointments();

    @PATCH("/api/appointments/my/{id}/cancel")
    Call<ApiResponse<Appointment>> cancelAppointment(@Path("id") Long id);

    // ========== Записи (админ) ==========

    @DELETE("/api/appointments/{id}")
    Call<ApiResponse<Void>> deleteAppointment(@Path("id") Long id);

    @PATCH("/api/appointments/{id}/notes")
    Call<ApiResponse<Appointment>> updateMasterNotes(
            @Path("id") Long id,
            @Body Map<String, String> notes
    );
    @GET("/api/appointments/timeline")
    Call<ApiResponse<TimelineResponse>> getTimeline(
            @Query("startDate") String startDate,
            @Query("daysCount") int daysCount
    );

    @PATCH("/api/appointments/{id}/status")
    Call<ApiResponse<Appointment>> updateAppointmentStatus(
            @Path("id") Long id,
            @Body UpdateAppointmentStatusRequest request
    );

    @PATCH("/api/appointments/{id}/move")
    Call<ApiResponse<Appointment>> moveAppointment(
            @Path("id") Long id,
            @Query("newStartTime") String newStartTime,
            @Query("newServiceId") Long newServiceId
    );

    // ========== Профиль ==========
    @GET("/api/profile")
    Call<ApiResponse<ProfileResponse>> getProfile();

    @PUT("/api/profile")
    Call<ApiResponse<Void>> updateProfile(@Body UpdateProfileRequest request);

    @PATCH("/api/profile/password")
    Call<ApiResponse<Void>> changePassword(@Body ChangePasswordRequest request);

    // ========== Клиенты (админ) ==========
    @GET("/api/clients/{id}/details")
    Call<ApiResponse<ClientDetailsResponse>> getClientDetails(@Path("id") Long id);

    @PUT("/api/clients/{id}")
    Call<ApiResponse<Void>> updateClient(@Path("id") Long id, @Body UpdateClientRequest request);

    @POST("/api/clients/{id}/appointments")
    Call<ApiResponse<Appointment>> createAppointmentForClient(
            @Path("id") Long id,
            @Body CreateMasterAppointmentRequest request
    );

    @GET("/api/clients")
    Call<ApiResponse<ClientsListResponse>> getClients(
            @Query("page") int page,
            @Query("size") int size,
            @Query("search") String search
    );

    // ========== Управление услугами (админ) ==========

    // Получить все услуги (активные и неактивные)
    @GET("/api/services/all")
    Call<ApiResponse<List<Service>>> getAllServices();

    // Создать услугу
    @POST("/api/services")
    Call<ApiResponse<Service>> createService(@Body ServiceRequest request);

    // Обновить услугу (PUT)
    @PUT("/api/services/{id}")
    Call<ApiResponse<Service>> updateService(@Path("id") Long id, @Body ServiceRequest request);

    // Активировать услугу (PATCH)
    @PATCH("/api/services/{id}/activate")
    Call<ApiResponse<Service>> activateService(@Path("id") Long id);

    // Деактивировать услугу (DELETE)
    @DELETE("/api/services/{id}")
    Call<ApiResponse<Void>> deactivateService(@Path("id") Long id);

// ========== Управление расписанием (админ) ==========

    // Получить доступные дни
    @GET("/api/schedule/admin/available-days")
    Call<ApiResponse<AvailableDaysResponse>> getAvailableDaysForAdmin(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    // Добавить доступный день
    @POST("/api/schedule/admin/available-days")
    Call<ApiResponse<AvailableDay>> addAvailableDay(
            @Query("date") String date,
            @Query("workStart") String workStart,
            @Query("workEnd") String workEnd,
            @Query("notes") String notes
    );

    // Обновить доступный день
    @PUT("/api/schedule/available-days/{id}")
    Call<ApiResponse<AvailableDay>> updateAvailableDay(
            @Path("id") Long id,
            @Query("workStart") String workStart,
            @Query("workEnd") String workEnd,
            @Query("isAvailable") Boolean isAvailable,
            @Query("notes") String notes
    );

    // Удалить доступный день
    @DELETE("/api/schedule/available-days/{id}")
    Call<ApiResponse<Void>> deleteAvailableDay(@Path("id") Long id);

    // Получить блокировки
    @GET("/api/schedule/blocks")
    Call<ApiResponse<ScheduleBlocksResponse>> getScheduleBlocks(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    // Добавить блокировку
    @POST("/api/schedule/blocks")
    Call<ApiResponse<ScheduleBlock>> addScheduleBlock(@Body CreateScheduleBlockRequest request);

    // Удалить блокировку
    @DELETE("/api/schedule/blocks/{id}")
    Call<ApiResponse<Void>> deleteScheduleBlock(@Path("id") Long id);

    // ========== Статистика ==========

    @GET("/api/stats/dashboard")
    Call<ApiResponse<DashboardStatsResponse>> getDashboardStats(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    @GET("/api/stats/dashboard/period")
    Call<ApiResponse<DashboardStatsResponse>> getDashboardStatsByPeriod(
            @Query("period") String period,
            @Query("referenceDate") String referenceDate
    );

    @GET("/api/stats/services/top")
    Call<ApiResponse<List<DashboardStatsResponse.TopServiceStats>>> getTopServices(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("limit") int limit
    );

    @GET("/api/stats/clients/top")
    Call<ApiResponse<List<DashboardStatsResponse.TopClientStats>>> getTopClients(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("limit") int limit
    );

    @GET("/api/stats/peak-hours")
    Call<ApiResponse<List<DashboardStatsResponse.HourlyStats>>> getPeakHours(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );


}