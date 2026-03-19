/// <reference types="cypress" />

describe("AI Recommendations Feature", () => {
  it("should display personalized recommendations for a user", () => {
    // Intercept recommendations API
    cy.intercept("GET", "**/api/recommendations/user/**", {
      statusCode: 200,
      body: {
        success: true,
        data: [
          {
            id: "rec-1",
            itemId: "flight-1",
            itemType: "FLIGHT",
            userId: "user-1",
            score: 0.95,
            title: "Delhi to Mumbai",
            description: "Recommended based on your past bookings",
            price: 5000,
            createdAt: new Date().toISOString(),
          },
          {
            id: "rec-2",
            itemId: "hotel-1",
            itemType: "HOTEL",
            userId: "user-1",
            score: 0.88,
            title: "Taj Hotel Mumbai",
            description: "Popular hotel near your destination",
            price: 8000,
            createdAt: new Date().toISOString(),
          },
        ],
        requestId: "req-1",
      },
    }).as("getRecommendations");

    // Intercept feedback API
    cy.intercept("POST", "**/api/recommendations/feedback", {
      statusCode: 200,
      body: { success: true, data: null, requestId: "req-2" },
    }).as("sendFeedback");

    cy.visit("/recommendations/suggestions");

    // Wait for recommendations to load
    cy.wait("@getRecommendations");

    // Recommendations should be visible
    cy.contains("Delhi to Mumbai").should("be.visible");
    cy.contains("Taj Hotel Mumbai").should("be.visible");
  });
});
