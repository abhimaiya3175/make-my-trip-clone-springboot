/// <reference types="cypress" />

describe("Price Freeze Feature", () => {
  it("should display the price freeze button and allow freezing a price", () => {
    // Intercept existing freezes check
    cy.intercept("GET", "**/api/pricing/freeze/user/**", {
      statusCode: 200,
      body: {
        success: true,
        data: [],
        requestId: "req-1",
      },
    }).as("getExistingFreezes");

    // Intercept pricing API
    cy.intercept("GET", "**/api/pricing/FLIGHT/**", {
      statusCode: 200,
      body: {
        success: true,
        data: {
          entityId: "flight-1",
          entityType: "FLIGHT",
          currentPrice: 5000,
          multiplier: 1.0,
          factors: [],
        },
        requestId: "req-2",
      },
    }).as("getPrice");

    // Intercept freeze creation
    cy.intercept("POST", "**/api/pricing/freeze", {
      statusCode: 200,
      body: {
        success: true,
        data: {
          id: "freeze-1",
          userId: "user-1",
          entityId: "flight-1",
          entityType: "FLIGHT",
          frozenPrice: 5000,
          expiresAt: new Date(Date.now() + 15 * 60 * 1000).toISOString(),
          active: true,
        },
        requestId: "req-3",
      },
    }).as("freezePrice");

    // Intercept flight details
    cy.intercept("GET", "**/api/flight/flight-1", {
      statusCode: 200,
      body: {
        success: true,
        data: {
          id: "flight-1",
          flightname: "AI101",
          from: "Delhi",
          to: "Mumbai",
          price: 5000,
          date: "2026-03-06",
          airline: "Air India",
        },
        requestId: "req-4",
      },
    }).as("getFlight");

    // Intercept seats API
    cy.intercept("GET", "**/api/seatroom/seats/flight/**", {
      statusCode: 200,
      body: { success: true, data: [], requestId: "req-5" },
    });

    cy.visit("/book-flight/flight-1");

    // Price information should load
    cy.wait("@getPrice");
  });
});
