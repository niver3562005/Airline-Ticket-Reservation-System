package com.aeronest.web;

import com.aeronest.domain.Flight;
import com.aeronest.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAndPageIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired FlightRepository flights;

    @Test void homePageIsPublicAndRendersSearch() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("public/home"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("More than a destination")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Passengers & cabin")));
    }
    @Test void passengerAuthenticationPagesArePublic() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("public/login"));
        mvc.perform(get("/register")).andExpect(status().isOk()).andExpect(view().name("public/register"));
        mvc.perform(get("/forgot-password")).andExpect(status().isOk()).andExpect(view().name("public/forgot-password"));
    }
    @Test void travelHubIsPublicAndIncludesModernJourneyTools() throws Exception {
        mvc.perform(get("/travel-hub")).andExpect(status().isOk()).andExpect(view().name("public/travel-hub"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("document checker")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Curated AeroVista escapes")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("24/7 GLOBAL CARE")));
    }
    @Test void connectedTravelServicesAndCabinGuideArePublic() throws Exception {
        mvc.perform(get("/travel-services")).andExpect(status().isOk()).andExpect(view().name("public/travel-services"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AEROVISTA TRAVEL NETWORK")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AeroCab Colombo")));
        mvc.perform(get("/cabin-guide")).andExpect(status().isOk()).andExpect(view().name("public/cabin-guide"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Premium Economy")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("First")));
    }
    @Test void privacyCookieAndLegalPagesArePublicAndExposeConsentControls() throws Exception {
        mvc.perform(get("/privacy-policy")).andExpect(status().isOk()).andExpect(view().name("public/privacy-policy"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Privacy, with clarity")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Accept all")));
        mvc.perform(get("/cookie-policy")).andExpect(status().isOk()).andExpect(view().name("public/cookie-policy"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Cookies, on your terms")));
        mvc.perform(get("/legal-notice")).andExpect(status().isOk()).andExpect(view().name("public/legal-notice"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Educational project")));
    }
    @Test void visaCheckerRendersOfficialDestinationGuidance() throws Exception {
        mvc.perform(get("/travel-hub").param("visaCode", "MV"))
                .andExpect(status().isOk()).andExpect(view().name("public/travel-hub"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Maldives")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("VISA ON ARRIVAL")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("immigration.gov.mv")));
    }
    @Test void flightStatusLookupRendersOperationalDetails() throws Exception {
        Flight flight = flights.findAllByOrderByDepartureAtAsc().get(0);
        mvc.perform(get("/flight-status")
                        .param("flightNumber", flight.getFlightNumber())
                        .param("flightDate", flight.getDepartureAt().toLocalDate().toString()))
                .andExpect(status().isOk()).andExpect(view().name("public/travel-hub"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(flight.getFlightNumber())))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Gate " + flight.getDepartureGate())));
    }
    @Test void routeSearchRendersMatchingFlightAndLiveAvailability() throws Exception {
        Flight flight = flights.findAllByOrderByDepartureAtAsc().get(0);
        mvc.perform(get("/flights")
                        .param("tripType", "ONE_WAY")
                        .param("originId", flight.getOrigin().getId().toString())
                        .param("destinationId", flight.getDestination().getId().toString())
                        .param("departureDate", flight.getDepartureAt().toLocalDate().toString())
                        .param("adults", "1").param("children", "0").param("infants", "0")
                        .param("cabinClass", "ECONOMY"))
                .andExpect(status().isOk()).andExpect(view().name("public/search-results"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString(flight.getFlightNumber())))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("seat")));
    }
    @Test void publicScheduleShowsAirlineIdentityFaresAndDelayCare() throws Exception {
        mvc.perform(get("/flight-schedule"))
                .andExpect(status().isOk()).andExpect(view().name("public/flight-schedule"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ten-plus journeys")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("aerovista-flight-logo.svg")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Automatic disruption care")));
    }
    @Test void routeSearchRendersAValidatedOneStopItinerary() throws Exception {
        Flight firstLeg = flights.findFirstByFlightNumberIgnoreCase("AN310").orElseThrow();
        Flight secondLeg = flights.findFirstByFlightNumberIgnoreCase("AN845").orElseThrow();
        mvc.perform(get("/flights")
                        .param("tripType", "ONE_WAY")
                        .param("originId", firstLeg.getOrigin().getId().toString())
                        .param("destinationId", secondLeg.getDestination().getId().toString())
                        .param("departureDate", firstLeg.getDepartureAt().toLocalDate().toString())
                        .param("adults", "1").param("children", "0").param("infants", "0")
                        .param("cabinClass", "ECONOMY"))
                .andExpect(status().isOk()).andExpect(view().name("public/search-results"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Smart one-stop options")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AN310")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("AN845")));
    }
    @Test void bookingCheckoutRendersPassengerAndPaymentSections() throws Exception {
        Flight flight = flights.findAllByOrderByDepartureAtAsc().get(0);
        mvc.perform(get("/book/{flightId}", flight.getId()).param("travellers", "2").param("cabin", "ECONOMY"))
                .andExpect(status().isOk()).andExpect(view().name("public/book"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Traveller 2")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Confirm & pay securely")));
    }
    @Test void dealsAndPaymentGuidanceArePublic() throws Exception {
        mvc.perform(get("/deals")).andExpect(status().isOk()).andExpect(view().name("public/deals"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("WELCOME20")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("FAMILY12")));
        mvc.perform(get("/payment-options")).andExpect(status().isOk()).andExpect(view().name("public/payment-options"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No card details stored")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("FLEXIBLE CASH RETURNS")));
    }
    @Test void demoPassengerCanSignInAndReachesAccount() throws Exception {
        mvc.perform(SecurityMockMvcRequestBuilders.formLogin().user("traveller@aerovista.test").password("Travel@123"))
                .andExpect(status().is3xxRedirection()).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/account"));
    }
    @Test @WithMockUser(username = "traveller@aerovista.test", roles = "CUSTOMER")
    void passengerCanViewProtectedAccount() throws Exception {
        mvc.perform(get("/account")).andExpect(status().isOk()).andExpect(view().name("account/dashboard"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("My journeys")));
    }
    @Test @WithMockUser(username = "traveller@aerovista.test", roles = "CUSTOMER")
    void passengerCanViewNotificationsAndFeedback() throws Exception {
        mvc.perform(get("/account/notifications")).andExpect(status().isOk()).andExpect(view().name("account/notifications"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Journey notifications")));
        mvc.perform(get("/account/feedback")).andExpect(status().isOk()).andExpect(view().name("account/feedback"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Share your experience")));
    }
    @Test void anonymousUserIsRedirectedFromOperations() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
    }
    @Test @WithMockUser(username = "agent", roles = "AGENT")
    void agentCanViewOperationsDashboard() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isOk()).andExpect(view().name("admin/dashboard"));
    }
    @Test @WithMockUser(username = "agent", roles = "AGENT")
    void agentCanViewPassengerServiceCentre() throws Exception {
        mvc.perform(get("/admin/service-center")).andExpect(status().isOk()).andExpect(view().name("admin/service-center"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Service centre")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Delivery monitor")));
    }
    @Test @WithMockUser(username = "agent", roles = "AGENT")
    void agentCannotManageAircraft() throws Exception {
        mvc.perform(get("/admin/aircraft")).andExpect(status().isForbidden());
    }
    @Test @WithMockUser(username = "admin", roles = {"ADMIN", "AGENT"})
    void administratorCanManageAircraft() throws Exception {
        mvc.perform(get("/admin/aircraft")).andExpect(status().isOk()).andExpect(view().name("aircraft/list"));
    }
}
