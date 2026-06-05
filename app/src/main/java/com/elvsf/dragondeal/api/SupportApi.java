package com.elvsf.dragondeal.api;

import com.elvsf.dragondeal.dto.SupportTicketRequest;
import com.elvsf.dragondeal.dto.SupportTicketResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SupportApi {

    @POST("support/tickets")
    Call<SupportTicketResponse> createSupportTicket(@Body SupportTicketRequest request);

    @GET("support/tickets/my")
    Call<List<SupportTicketResponse>> getMySupportTickets();

    @GET("support/tickets/{id}")
    Call<SupportTicketResponse> getSupportTicketById(@Path("id") Long id);
}
