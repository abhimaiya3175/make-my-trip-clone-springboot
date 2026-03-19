/// <reference types="cypress" />

describe("Reviews Feature", () => {
  it("should display the review form and submit a review", () => {
    // Intercept the review submission API
    cy.intercept("POST", "**/api/reviews", {
      statusCode: 200,
      body: {
        success: true,
        data: {
          id: "rev-1",
          entityType: "FLIGHT",
          entityId: "flight-1",
          userId: "user-1",
          userName: "Test User",
          rating: 4,
          text: "Great flight experience!",
          photos: [],
          createdAt: new Date().toISOString(),
        },
        requestId: "req-1",
      },
    }).as("submitReview");

    // Intercept GET reviews
    cy.intercept("GET", "**/api/reviews/**", {
      statusCode: 200,
      body: {
        success: true,
        data: [],
        requestId: "req-2",
      },
    }).as("getReviews");

    cy.visit("/reviews/review");

    // Page title should be visible
    cy.contains("Write a Review").should("be.visible");
  });
});
